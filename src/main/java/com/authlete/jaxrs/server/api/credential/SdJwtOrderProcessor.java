/*
 * Copyright (C) 2023 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package com.authlete.jaxrs.server.api.credential;


import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialRequestInfo;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.common.types.User;
import com.authlete.jaxrs.server.db.UserDao;
import com.authlete.jaxrs.server.util.ExceptionUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;


public class SdJwtOrderProcessor implements IOrderProcessor
{
    public CredentialIssuanceOrder toOrder(final IntrospectionResponse introspection,
                                           final CredentialRequestInfo info)
    {
        final String credentialType = getCredentialType(info.getDetails());

        // The subject (the identifier of the user) that is associated with the access token.
        final String subject = introspection.getSubject();

        // The information about the user identified by the subject.
        final User user = UserDao.getBySubject(subject);

        // Find credentialType
        final CredentialDefinitionType definitionType = CredentialDefinitionType.byId(credentialType);
        if(definitionType == null)
        {
            throw ExceptionUtil.badRequestException(String.format("Unknown credential type %s.", credentialType));
        }

        // Prepare the credential payload.
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", credentialType);
        jsonObject.addProperty("sub", subject);

        for (final String claim : definitionType.getClaims())
        {
            jsonObject.addProperty(claim, (String) user.getClaim(claim, null));
        }

        final String credentialPayload = jsonObject.toString();

        return new CredentialIssuanceOrder()
                .setRequestIdentifier(info.getIdentifier())
                .setCredentialPayload(credentialPayload);
    }


    private String getCredentialType(final String details)
    {
        final JsonElement request;
        try
        {
            request = JsonParser.parseString(details);
        }
        catch (JsonSyntaxException e)
        {
            throw ExceptionUtil.badRequestException("Unreadable credential request details.");
        }

        if(!(request instanceof JsonObject))
        {
            throw ExceptionUtil.badRequestException("Credential request details should be a JSON object.");
        }

        final JsonElement definition = ((JsonObject)request).get("credential_definition");
        if(!(definition instanceof JsonObject))
        {
            throw ExceptionUtil.badRequestException("Credential definition should be defined.");
        }

        final JsonElement type = ((JsonObject)definition).get("type");
        if(type == null)
        {
            throw ExceptionUtil.badRequestException("Credential type should be defined.");
        }

        return type.getAsString();
    }
}
