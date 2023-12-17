package com.authlete.jaxrs.server.api;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jaxrs.BasePushedAuthReqEndpoint;
import com.authlete.jaxrs.PushedAuthReqHandler.Params;


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
            @Context HttpServletRequest request,
            MultivaluedMap<String, String> parameters)
    {
        // Authlete API
        AuthleteApi authleteApi = AuthleteApiFactory.getDefaultApi();

        // Parameters for Authlete's pushed_auth_req API.
        Params params = buildParams(request, parameters);

        // Handle the PAR request.
        return handle(authleteApi, params);
    }


    private Params buildParams(
            HttpServletRequest request, MultivaluedMap<String, String> parameters)
    {
        Params params = new Params();

        // RFC 6749
        // The OAuth 2.0 Authorization Framework
        params.setParameters(parameters)
              .setAuthorization(request.getHeader(HttpHeaders.AUTHORIZATION))
              ;

        // MTLS
        // RFC 8705 : OAuth 2.0 Mutual-TLS Client Authentication and Certificate-Bound Access Tokens
        params.setClientCertificatePath(extractClientCertificateChain(request));

        // DPoP
        // RFC 9449 : OAuth 2.0 Demonstrating Proof of Possession (DPoP)
        params.setDpop(request.getHeader("DPoP"))
              .setHtm("POST")
              //.setHtu(request.getRequestURL().toString())
              ;

        // We can reconstruct the URL of the PAR endpoint by calling
        // request.getRequestURL().toString() and set it to params by the
        // setHtu(String) method. However, the calculated URL may be invalid
        // behind proxies.
        //
        // If "htu" is not set here, the "pushedAuthReqEndpoint" property of
        // "Service" (which can be configured by using Authlete's web console)
        // is referred to as the default value. Therefore, we don't call the
        // setHtu(String) method here intentionally. Note that this means you
        // have to set "pushedAuthReqEndpoint" properly to support DPoP.

        // Even the call of the setHtm(String) method can be omitted, too.
        // When "htm" is not set, "POST" is used as the default value.

        return params;
    }
}
