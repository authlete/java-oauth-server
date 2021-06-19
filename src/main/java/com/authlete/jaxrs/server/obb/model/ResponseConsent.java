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
package com.authlete.jaxrs.server.obb.model;


import java.io.Serializable;
import com.authlete.jaxrs.server.obb.util.ObbUtils;


/**
 * ResponseConsent.
 *
 * @see <a href="https://openbanking-brasil.github.io/areadesenvolvedor/#tocS_ResponseConsent"
 *      >ResponseConsent</a>
 */
public class ResponseConsent implements Serializable
{
    private static final long serialVersionUID = 1L;


    private ResponseConsentData data;


    public ResponseConsent()
    {
    }


    public ResponseConsent(Consent consent, Links links, Meta meta)
    {
        data = new ResponseConsentData()
                .setConsentId(consent.getConsentId())
                .setCreationDateTime(consent.getCreationDateTime())
                .setStatus(consent.getStatus())
                .setStatusUpdateDateTime(consent.getStatusUpdateDateTime())
                .setPermissions(consent.getPermissions())
                .setExpirationDateTime(consent.getExpirationDateTime())
                .setLinks(links)
                .setMeta(meta)
                ;
    }


    public ResponseConsentData getData()
    {
        return data;
    }


    public ResponseConsent setData(ResponseConsentData data)
    {
        this.data = data;

        return this;
    }


    public static ResponseConsent create(Consent consent)
    {
        Links links = new Links().setSelf("/");
        Meta  meta  = new Meta(1, 1, ObbUtils.formatNow());

        return new ResponseConsent(consent, links, meta);
    }
}
