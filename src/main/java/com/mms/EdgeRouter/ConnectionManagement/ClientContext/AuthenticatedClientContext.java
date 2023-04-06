package com.mms.EdgeRouter.ConnectionManagement.ClientContext;

import lombok.NonNull;
import org.springframework.web.socket.WebSocketSession;

/**
 * The AuthenticatedClientContext class represents the context of an authenticated WebSocket connection with an agent.
 * It extends the {@link AnonymousClientContext} class, adding the Maritime Resource Name (MRN) of the agent.
 */
public class AuthenticatedClientContext extends AnonymousClientContext
{
    private final String maritimeResourceName;


    /**
     * Constructs a new {@link AuthenticatedClientContext} object with the given WebSocket session and MRN.
     *
     * @param session              The WebSocket session representing the connection with the agent.
     * @param maritimeResourceName The Maritime Resource Name (MRN) of the authenticated agent.
     */
    public AuthenticatedClientContext(@NonNull WebSocketSession session, @NonNull String maritimeResourceName)
    {
        super(session);
        this.maritimeResourceName = maritimeResourceName;
    }


    /**
     * Returns the Maritime Resource Name (MRN) associated with the authenticated agent.
     *
     * @return The MRN of the authenticated agent.
     */
    public String getMRN()
    {
        return maritimeResourceName;
    }
}
