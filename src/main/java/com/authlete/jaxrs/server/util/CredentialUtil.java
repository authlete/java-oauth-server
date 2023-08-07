package com.authlete.jaxrs.server.util;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialRequestInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minidev.json.JSONObject;


public class CredentialUtil
{

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static CredentialIssuanceOrder toOrder(final CredentialRequestInfo info)
    {
        final String format = info.getFormat();
        final String details = info.getDetails();
        final JSONObject detailsObj = gson.fromJson(details, JSONObject.class);
        final JSONObject payloadObj = new JSONObject()
                .appendField("format", format)
                .appendField("details", detailsObj);
        final String payload = gson.toJson(payloadObj);
        final String identifier = info.getIdentifier();

        return new CredentialIssuanceOrder()
                .setCredentialPayload(payload)
                .setRequestIdentifier(identifier);
    }


    public static CredentialIssuanceOrder[] toOrder(final CredentialRequestInfo[] info)
    {
        return Arrays.stream(info)
                .map(CredentialUtil::toOrder)
                .collect(Collectors.toList())
                .toArray(new CredentialIssuanceOrder[]{});
    }

}
