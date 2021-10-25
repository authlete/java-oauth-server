package com.authlete.jaxrs.server.api;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


@Path("/api/test")
public class TestEndpoint
{
    /**
     * Returns HTTP headers that this endpoint received in JSON format.
     */
    @GET
    @Path("headers")
    public Response headers(@Context HttpServletRequest req) throws Exception
    {
        Map<String, Object> map = new TreeMap<>();

        Enumeration<String> headerNameEnumerator = req.getHeaderNames();

        while (headerNameEnumerator.hasMoreElements())
        {
            String headerName = headerNameEnumerator.nextElement();

            Enumeration<String> headerValueEnumerator = req.getHeaders(headerName);
            List<String> headerValues = new ArrayList<>();

            while (headerValueEnumerator.hasMoreElements())
            {
                headerValues.add(headerValueEnumerator.nextElement());
            }

            if (headerValues.size() == 1)
            {
                map.put(headerName, headerValues.get(0));
            }
            else
            {
                map.put(headerName, headerValues);
            }
        }

        return toResponse(map);
    }


    /**
     * Checks whether the root certificate of the certificate chain that
     * consists of the presented client certificate and intermediate
     * certificates is a certificate issued by the authority of Open
     * Banking Brasil. The result is returned in JSON format.
     *
     * <p>
     * Below is an example of API call, assuming <code>certificates.pem</code>
     * includes a client certificate and intermediate certificates.
     * </p>
     *
     * <pre>
     * $ curl -k --key private.pem --cert certificates.pem https://example/api/test/obb
     * </pre>
     */
    @GET
    @Path("obb")
    public Response obb(@Context HttpServletRequest req)
    {
        Map<String, Object> map = new TreeMap<>();

        try
        {
            OBBCertValidator.getInstance().validate(req);
            map.put("result", "succeeded");
        }
        catch (Exception e)
        {
            e.printStackTrace();

            map.put("result", "failed");
            map.put("error_message", e.getMessage());

            List<String> stacktrace = Arrays.stream(
                    e.getStackTrace()).map(st -> st.toString())
                    .collect(Collectors.toList());

            map.put("stacktrace", stacktrace);
        }

        return toResponse(map);
    }


    private static Response toResponse(Map<String, Object> map)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(map);

        return Response.ok(json).type(MediaType.APPLICATION_JSON).build();
    }
}
