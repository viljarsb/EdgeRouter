package com.mms.EdgeRouter.WebSocket;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.cert.X509Certificate;
import java.util.Map;


/**
 * A class used to intercept the handshake request and extract the client certificate.
 * Needed to extract authenticated clients Maritime Resource Names. (MRN)
 */
@Slf4j
public class ClientCertificateInterceptor implements HandshakeInterceptor
{

    /**
     * Extracts the client certificate from the handshake request and adds it to the attributes.
     *
     * @param request    the request
     * @param response   the response
     * @param wsHandler  the target WebSocket handler
     * @param attributes the attributes from the HTTP handshake to associate with the WebSocket
     *                   session; the provided attributes are copied, the original map is not used.
     * @return default true
     */
    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes)
    {
        if (request instanceof ServletServerHttpRequest servletRequest)
        {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            X509Certificate[] certs = (X509Certificate[]) httpRequest.getAttribute("jakarta.servlet.request.X509Certificate");


            if (certs != null && certs.length > 0)
            {
                attributes.put("MMS-CERTIFICATE", certs[0]);
            }
        }
        return true;
    }


    /**
     * Invoked after the handshake is complete and the WebSocket session is ready to use, ignore, let default implementation handle.
     */
    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull  ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Exception exception) {}
}