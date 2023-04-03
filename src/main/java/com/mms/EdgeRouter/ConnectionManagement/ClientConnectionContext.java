package com.mms.EdgeRouter.ConnectionManagement;


import lombok.Getter;
import lombok.NonNull;
import net.maritimeconnectivity.pki.CertificateHandler;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.springframework.web.socket.WebSocketSession;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.UUID;

/**
 * The {@code ClientConnectionContext} class represents the state of a WebSocket connection.
 * It holds information about the session, the URI the connection was made to,
 * and the MCP-PKI identity of the connected user if available.
 */
public class ClientConnectionContext implements Comparable<ClientConnectionContext>
{
    @Getter
    private final WebSocketSession session;
    @Getter
    private final URI connectionURI;
    @Getter
    private final String agentID;
    @Getter
    private PKIIdentity identity;


    public ClientConnectionContext(@NonNull WebSocketSession session)
    {
        this.session = session;
        this.connectionURI = session.getUri();
        this.agentID = session.getId();
        checkAuthentication();
    }


    /**
     * Checks the authentication status of the session by looking for a
     * client certificate and, if present, extracts the MCP-PKI identity
     * and stores it in the instance variable {@code identity}.
     */
    private void checkAuthentication()
    {
        SSLSession sslSession = (SSLSession) session.getAttributes().get("javax.websocket.endpoint.ssl.session");

        if (sslSession != null)
        {
            try
            {
                X509Certificate[] clientCertificates = (X509Certificate[]) sslSession.getPeerCertificates();
                if (clientCertificates.length > 0)
                {
                    this.identity = CertificateHandler.getIdentityFromCert(clientCertificates[0]);
                }
            }

            catch (SSLPeerUnverifiedException e)
            {

            }
        }
    }


    /**
     * Used to compare two {@code ClientConnectionContext} objects.
     *
     * @param other the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(@NonNull ClientConnectionContext other)
    {
        return this.agentID.compareTo(other.getAgentID());
    }
}
