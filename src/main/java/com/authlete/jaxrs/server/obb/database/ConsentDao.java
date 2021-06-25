/*
 * Copyright (C) 2021 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.authlete.jaxrs.server.obb.database;


import java.util.UUID;
import com.authlete.jaxrs.server.obb.model.Consent;
import com.authlete.jaxrs.server.obb.model.CreateConsent;
import com.authlete.jaxrs.server.obb.model.CreateConsentData;
import com.authlete.jaxrs.server.obb.util.ObbUtils;


public class ConsentDao
{
    private static final ConsentDao sInstance = new ConsentDao("example");


    private final String mNamespace;
    private final ConsentStore mStore;


    private ConsentDao(String namespace)
    {
        mNamespace = namespace;
        mStore     = new ConsentStore();
    }


    private String getNamespace()
    {
        return mNamespace;
    }


    private ConsentStore getStore()
    {
        return mStore;
    }


    private String generateConsentId()
    {
        // '^urn:[a-zA-Z0-9][a-zA-Z0-9-]{0,31}:[a-zA-Z0-9()+,\-.:=@;$_!*''%\/?#]+$'
        return String.format("urn:%s:%s", getNamespace(), UUID.randomUUID());
    }


    public Consent create(CreateConsent createConsent, long clientId)
    {
        CreateConsentData data = createConsent.getData();
        String consentId = generateConsentId();
        String now = ObbUtils.formatNow();

        Consent consent = new Consent()
                .setConsentId(consentId)
                .setPermissions(data.getPermissions())
                .setStatus("AWAITING_AUTHORISATION")
                .setCreationDateTime(now)
                .setExpirationDateTime(data.getExpirationDateTime())
                .setStatusUpdateDateTime(now)
                .setClientId(clientId)
                ;

        getStore().put(consentId, consent);

        return consent;
    }


    public synchronized Consent read(String consentId)
    {
        return getStore().get(consentId);
    }


    public synchronized void update(Consent consent)
    {
        consent.setStatusUpdateDateTime(ObbUtils.formatNow());

        getStore().put(consent.getConsentId(), consent);
    }


    public synchronized void delete(String consentId)
    {
        getStore().remove(consentId);
    }


    public static ConsentDao getInstance()
    {
        return sInstance;
    }
}
