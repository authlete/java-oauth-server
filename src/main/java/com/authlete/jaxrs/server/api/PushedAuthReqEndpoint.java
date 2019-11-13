package com.authlete.jaxrs.server.api;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jaxrs.BasePushedAuthReqEndpoint;


/**
 * An implementation of a pushed authorization endpoint.
 * 
 * @see <a href="https://tools.ietf.org/html/draft-lodderstedt-oauth-par"
 *      >OAuth 2.0 Pushed Authorization Requests</a>
 * 
 * @author Justin Richer
 *
 */
@Path("/api/par")
public class PushedAuthReqEndpoint extends BasePushedAuthReqEndpoint
{
    /**
     * The pushed authorization request endpoint. This uses the
     * {@code POST} method and the same client authentication as
     * is available on the Token Endpoint.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            MultivaluedMap<String, String> parameters,
            @Context HttpServletRequest request)
    {
        String[] clientCertificates = extractClientCertificateChain(request);

        return handle(AuthleteApiFactory.getDefaultApi(),
                parameters, authorization, clientCertificates);
    }
}
