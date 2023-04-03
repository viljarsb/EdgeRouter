package com.mms.EdgeRouter.WebSocket;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * RateLimiterWsHandler is a Spring WebSocket handler decorator that provides rate limiting functionality.
 * It limits the number of concurrent WebSocket connections, the rate of incoming binary messages, and blocks IPs that
 * exceed the limits. It also logs more details about rate limiting and errors.
 */
@Slf4j
@Component
public class RateLimiterWsHandler extends WebSocketHandlerDecorator
{
    private final Bucket connectionsBucket;
    private final ConcurrentHashMap<String, Bucket> messageRateLimiters;
    private final Cache<String, AtomicInteger> connectionAttempts;
    private final Cache<String, Boolean> blockedIPs;

    private final long maxConnectionsPerSecond;
    private final long maxBytesPerSecond;
    private final long maxConcurrentConnections;
    private final long blacklistTime;

    private final TaskExecutor workerPool;


    /**
     * Constructor for RateLimiterWsHandler.
     *
     * @param delegate                 the original WebSocket handler
     * @param workerPool               the thread pool used to process incoming messages
     * @param maxConnectionsPerSecond  the maximum number of WebSocket connections per second
     * @param maxBytesPerSecond        the maximum number of bytes per second for each connection
     * @param maxConcurrentConnections the maximum number of concurrent connections from a single IP
     * @param blacklistTime            the duration in minutes to blacklist an IP
     */
    @Autowired
    public RateLimiterWsHandler(WsHandler delegate,
                                @Qualifier("WorkerPool") TaskExecutor workerPool,
                                @Value("${edgerouter.maxConnectionsPerSecond:10000}") long maxConnectionsPerSecond,
                                @Value("${edgerouter.maxBytesPerSecond:100000}") long maxBytesPerSecond,
                                @Value("${edgerouter.maxConcurrentConnections:10000}") long maxConcurrentConnections,
                                @Value("${edgerouter.blacklistTime:10000}") long blacklistTime)
    {
        super(delegate);
        this.workerPool = workerPool;
        this.maxConnectionsPerSecond = maxConnectionsPerSecond;
        this.maxBytesPerSecond = maxBytesPerSecond;
        this.maxConcurrentConnections = maxConcurrentConnections;
        this.blacklistTime = blacklistTime;

        Bandwidth bandwidth = Bandwidth.simple(maxConnectionsPerSecond, Duration.ofSeconds(1));
        this.connectionsBucket = Bucket.builder().addLimit(bandwidth).build();
        this.messageRateLimiters = new ConcurrentHashMap<>();

        this.blockedIPs = CacheBuilder.newBuilder()
                .expireAfterWrite(blacklistTime, TimeUnit.MINUTES)
                .build();

        this.connectionAttempts = CacheBuilder.newBuilder()
                .expireAfterWrite(blacklistTime, TimeUnit.MINUTES)
                .build();


        log.info("RateLimiterWsHandler initialized with maxConnectionsPerSecond: {}, maxBytesPerSecond: {}, maxConcurrentConnections: {}, blacklistTime: {}",
                maxConnectionsPerSecond, maxBytesPerSecond, maxConcurrentConnections, blacklistTime);
    }


    /**
     * Handles a new WebSocket connection. Performs rate limiting checks and denies or accepts the connection
     * accordingly. Adds a message rate limiter to the session if the connection is accepted.
     *
     * @param session The WebSocket session object.
     * @throws Exception If an error occurs while handling the connection.
     */
    @Async("ConnectionPool")
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception
    {
        String remoteAddress = session.getRemoteAddress().getAddress().getHostAddress();
        if (connectionsBucket.tryConsume(1) && !isIPBlocked(remoteAddress))
        {
            AtomicInteger currentAttempts = connectionAttempts.getIfPresent(remoteAddress);
            if (currentAttempts == null)
            {
                currentAttempts = new AtomicInteger(0);
                connectionAttempts.put(remoteAddress, currentAttempts);
            }
            int attempts = currentAttempts.incrementAndGet();
            if (attempts <= maxConcurrentConnections)
            {
                Bandwidth bandwidth = Bandwidth.simple(maxBytesPerSecond, Duration.ofSeconds(1));
                Bucket messageBucket = Bucket.builder().addLimit(bandwidth).build();
                messageRateLimiters.put(session.getId(), messageBucket);
                super.afterConnectionEstablished(session);
            }
            else
            {
                log.warn("Connection attempt from IP: {} exceeded max concurrent connections: {}", remoteAddress, attempts);
                blockIP(remoteAddress);
                denyConnection(session, CloseStatus.SERVICE_OVERLOAD.getCode(), "Too many requests from this IP");
            }
        }
        else
        {
            log.warn("Connection attempt from blocked IP: {}", remoteAddress);
            denyConnection(session, CloseStatus.SERVICE_OVERLOAD.getCode(), "IP is blocked or too many connections");
        }
    }


    /**
     * Handles a closed WebSocket connection. Removes the message rate limiter associated with the session.
     *
     * @param session The WebSocket session object.
     * @param status  The close status.
     * @throws Exception If an error occurs while handling the close.
     */
    @Async("ConnectionPool")
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception
    {
        messageRateLimiters.remove(session.getId());
        String remoteAddress = session.getRemoteAddress().getAddress().getHostAddress();
        AtomicInteger currentAttempts = connectionAttempts.getIfPresent(remoteAddress);
        if (currentAttempts != null)
        {
            currentAttempts.decrementAndGet();
        }
        super.afterConnectionClosed(session, status);
    }


    /**
     * Handles a WebSocket message. Only binary messages are accepted. Applies rate limiting checks to the message and
     * executes the decorated WebSocket handler if the checks pass.
     *
     * @param session The WebSocket session object.
     * @param message The WebSocket message object.
     */
    @Async("WorkerPool")
    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message)
    {
        if (message instanceof TextMessage)
        {
            try
            {
                log.warn("Received text message from session: {}, terminating connection", session.getId());
                CloseStatus status = new CloseStatus(CloseStatus.BAD_DATA.getCode(), "Only binary messages are supported");
                session.close(status);
            }
            catch (IOException ex)
            {
                log.error("Error closing session: {}", session.getId(), ex);
            }
        }
        else if (message instanceof BinaryMessage)
        {
            applyRateLimit((BinaryMessage) message, session);
        }
    }


    /**
     * Applies rate limiting checks to a binary WebSocket message. Uses a bucket to enforce the maximum byte rate limit
     * and delays message processing if necessary.
     *
     * @param message The binary WebSocket message object.
     * @param session The WebSocket session object.
     */
    @Async("WorkerPool")
    protected void applyRateLimit(@NonNull BinaryMessage message, @NonNull WebSocketSession session)
    {
        try
        {
            Bucket rateLimiterBucket = messageRateLimiters.get(session.getId());
            long payloadSize = message.getPayloadLength();
            applyRateLimitWithDelay(message, session, rateLimiterBucket, payloadSize);
        }
        catch (Exception e)
        {
            log.error("Error processing message from session: {}", session.getId(), e);
        }
    }


    /**
     * Applies rate limiting checks to a binary WebSocket message, and delays processing if necessary.
     *
     * @param message           The binary WebSocket message object.
     * @param session           The WebSocket session object.
     * @param rateLimiterBucket The rate limiter bucket to use for checking message rates.
     * @param payloadSize       The size of the message payload in bytes.
     * @throws Exception If an error occurs while processing the message.
     */
    @Async("WorkerPool")
    protected void applyRateLimitWithDelay(@NonNull BinaryMessage message, @NonNull WebSocketSession session, @NonNull Bucket rateLimiterBucket, long payloadSize) throws Exception
    {
        ConsumptionProbe consumptionProbe = rateLimiterBucket.tryConsumeAndReturnRemaining(payloadSize);

        if (consumptionProbe.isConsumed())
        {
            super.handleMessage(session, message);
            return;
        }

        long waitTimeMillis = consumptionProbe.getNanosToWaitForRefill() / 1_000_000;
        log.debug("No tokens available for session: {}, waiting {}ms", session.getId(), waitTimeMillis);

        CompletableFuture.delayedExecutor(waitTimeMillis, TimeUnit.MILLISECONDS, workerPool)
                .execute(() -> applyRateLimitWithDelayAndHandleExceptions(message, session, rateLimiterBucket, payloadSize));
    }


    /**
     * Applies rate limiting checks to a binary WebSocket message, delays message processing if necessary, and handles
     * any exceptions that occur while processing the message.
     *
     * @param message           The binary WebSocket message object.
     * @param session           The WebSocket session object.
     * @param rateLimiterBucket The rate limiter bucket to use for checking message rates.
     * @param payloadSize       The size of the message payload in bytes.
     */
    private void applyRateLimitWithDelayAndHandleExceptions(@NonNull BinaryMessage message, @NonNull WebSocketSession session, @NonNull Bucket rateLimiterBucket, long payloadSize)
    {
        try
        {
            applyRateLimitWithDelay(message, session, rateLimiterBucket, payloadSize);
        }
        catch (Exception e)
        {
            log.error("Error applying rate limit with delay for session: {}", session.getId(), e);
        }
    }


    /**
     * Blocks an IP address from connecting to the WebSocket handler for the configured blacklist time.
     *
     * @param ip The IP address to block.
     */
    private void blockIP(@NonNull String ip)
    {
        blockedIPs.put(ip, true);
    }


    /**
     * Checks if an IP address is currently blocked from connecting to the WebSocket handler.
     *
     * @param ip The IP address to check.
     * @return True if the IP address is blocked, false otherwise.
     */
    private boolean isIPBlocked(@NonNull String ip)
    {
        return blockedIPs.getIfPresent(ip) != null;
    }


    /**
     * Closes a WebSocket session with a given close code and reason.
     *
     * @param session The WebSocket session to close.
     * @param code    The close code to use.
     * @param reason  The close reason.
     */
    private void denyConnection(@NonNull WebSocketSession session, int code, @NonNull String reason)
    {
        try
        {
            CloseStatus status = new CloseStatus(code, reason);
            session.close(status);
        }
        catch (IOException ex)
        {
            log.error("Error closing session: {}", session.getId(), ex);
        }
    }
}