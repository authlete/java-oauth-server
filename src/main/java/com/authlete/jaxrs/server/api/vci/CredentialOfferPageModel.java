/*
 * Copyright (C) 2023 Authlete, Inc.
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
package com.authlete.jaxrs.server.api.vci;


import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import com.authlete.common.dto.CredentialOfferCreateRequest;
import com.authlete.common.dto.CredentialOfferInfo;
import com.authlete.common.types.User;
import com.authlete.jaxrs.AuthorizationPageModel;
import com.authlete.jaxrs.server.util.ExceptionUtil;
import com.authlete.jaxrs.server.util.ProcessingUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;


/**
 * Data used to render the credential offer page.
 */
public class CredentialOfferPageModel extends AuthorizationPageModel
{
    private static final long serialVersionUID = 2L;


    private static final String DEFAULT_ENDPOINT = "openid-credential-offer://";
    private static final String CREDENTIAL_OFFER_QR_PATTERN = "%s?credential_offer=%s";
    private static final String CREDENTIAL_OFFER_URI_QR_PATTERN = "%s?credential_offer_uri=%s";
    private static final int QR_CODE_WIDTH = 300;
    public static final int QR_CODE_HEIGHT = 300;


    private static final String DEFAULT_CREDENTIAL_CONFIGURATION_IDS =
            "[\n" +
            "  \"DigitalCredential\",\n" +
            "  \"IdentityCredential\",\n" +
            "  \"org.iso.18013.5.1.mDL\"\n" +
            "]";


    private String credentialConfigurationIds;
    private boolean authorizationCodeGrantIncluded;
    private boolean issuerStateIncluded;
    private boolean preAuthorizedCodeGrantIncluded;
    private String txCode;
    private String txCodeInputMode;
    private String txCodeDescription;
    private int duration;
    private String credentialOfferEndpoint;
    private CredentialOfferInfo info;
    private String credentialOfferLink;
    private String credentialOfferQrCode;
    private String credentialOfferContent;
    private String credentialOfferUri;
    private String credentialOfferUriLink;
    private String credentialOfferUriQrCode;


    public CredentialOfferPageModel()
    {
        this.authorizationCodeGrantIncluded = false;
        this.issuerStateIncluded            = true;
        this.preAuthorizedCodeGrantIncluded = true;
        this.duration                       = 0;
        this.credentialConfigurationIds     = DEFAULT_CREDENTIAL_CONFIGURATION_IDS;
        this.credentialOfferEndpoint        = DEFAULT_ENDPOINT;
    }


    public CredentialOfferPageModel setValues(final Map<String, String> values)
    {
        this.credentialConfigurationIds     = values.getOrDefault("credentialConfigurationIds", this.credentialConfigurationIds);
        this.authorizationCodeGrantIncluded = fromCheckBox(values, "authorizationCodeGrantIncluded");
        this.issuerStateIncluded            = fromCheckBox(values, "issuerStateIncluded");
        this.preAuthorizedCodeGrantIncluded = fromCheckBox(values, "preAuthorizedCodeGrantIncluded");
        this.txCode                         = values.getOrDefault("txCode",            this.txCode);
        this.txCodeInputMode                = values.getOrDefault("txCodeInputMode",   this.txCodeInputMode);
        this.txCodeDescription              = values.getOrDefault("txCodeDescription", this.txCodeDescription);
        this.duration                       = extractInt(values, "duration", this.duration);
        this.credentialOfferEndpoint        = values.getOrDefault("credentialOfferEndpoint", this.credentialOfferEndpoint);

        return this;
    }


    private static boolean fromCheckBox(Map<String, String> values, String key)
    {
        return ProcessingUtil.fromFormCheckbox(values, key);
    }


    private Integer extractInt(final Map<String, String> values,
                               final String key, final Integer def)
    {
        final String value = values.getOrDefault(key, Integer.toString(def));

        try
        {
            final Integer intVal = Integer.parseInt(value);

            if (intVal < 0)
            {
                throw ExceptionUtil.badRequestException(
                        String.format("%s should be positive.", key));
            }

            return intVal;
        }
        catch (NumberFormatException e)
        {
            throw ExceptionUtil.badRequestException(
                    String.format("%s should be a number.", key));
        }
    }


    private String asQrCode(final String text) throws IOException, WriterException
    {
        final QRCodeWriter qrCodeWriter = new QRCodeWriter();
        final BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE,
                                                        QR_CODE_WIDTH, QR_CODE_HEIGHT);

        final BufferedImage qrCode = MatrixToImageWriter.toBufferedImage(bitMatrix);

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(qrCode, "png", output);
        return Base64.getEncoder().encodeToString(output.toByteArray());
    }


    private String prettifyJson(final String json)
    {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final JsonElement je = JsonParser.parseString(json);
        return gson.toJson(je);
    }


    public CredentialOfferCreateRequest toRequest(final User user)
    {
        return new CredentialOfferCreateRequest()
                .setAuthorizationCodeGrantIncluded(this.authorizationCodeGrantIncluded)
                .setIssuerStateIncluded(this.issuerStateIncluded)
                .setPreAuthorizedCodeGrantIncluded(this.preAuthorizedCodeGrantIncluded)
                .setTxCode(this.txCode)
                .setTxCodeInputMode(this.txCodeInputMode)
                .setTxCodeDescription(this.txCodeDescription)
                .setDuration(this.duration)
                .setCredentialConfigurationIds(
                        parseAsStringArray("credentialConfigurationIds", this.credentialConfigurationIds))
                .setSubject(user.getSubject());
    }


    private String[] parseAsStringArray(String name, String json)
    {
        List<?> list = parseAsList(name, json);

        if (list == null)
        {
            return null;
        }

        int size = list.size();

        for (int i = 0; i < size; i++)
        {
            Object element = list.get(i);

            if (!(element instanceof String))
            {
                throw ExceptionUtil.badRequestException(String.format(
                        "All the elements in the '%s' array must be a string, but the element at the index %d is not.",
                        name, i));
            }
        }

        return list.stream().map(element -> (String)element).toArray(String[]::new);
    }


    private List<?> parseAsList(String name, String json)
    {
        try
        {
            // Parse as a JSON array.
            return new Gson().fromJson(json, List.class);
        }
        catch (Exception cause)
        {
            throw ExceptionUtil.badRequestException(String.format(
                    "The value of '%s' should be a JSON array.", name));
        }
    }


    public String getCredentialConfigurationIds()
    {
        return credentialConfigurationIds;
    }


    public void setCredentialConfigurationIds(String ids)
    {
        this.credentialConfigurationIds = ids;
    }


    public boolean isAuthorizationCodeGrantIncluded()
    {
        return authorizationCodeGrantIncluded;
    }


    public void setAuthorizationCodeGrantIncluded(boolean authorizationCodeGrantIncluded)
    {
        this.authorizationCodeGrantIncluded = authorizationCodeGrantIncluded;
    }


    public boolean isIssuerStateIncluded()
    {
        return issuerStateIncluded;
    }


    public void setIssuerStateIncluded(boolean issuerStateIncluded)
    {
        this.issuerStateIncluded = issuerStateIncluded;
    }


    public boolean isPreAuthorizedCodeGrantIncluded()
    {
        return preAuthorizedCodeGrantIncluded;
    }


    public void setPreAuthorizedCodeGrantIncluded(boolean preAuthorizedCodeGrantIncluded)
    {
        this.preAuthorizedCodeGrantIncluded = preAuthorizedCodeGrantIncluded;
    }


    public String getTxCode()
    {
        return txCode;
    }


    public void setTxCode(String txCode)
    {
        this.txCode = txCode;
    }


    public String getTxCodeInputMode()
    {
        return txCodeInputMode;
    }


    public void setTxCodeInputMode(String inputMode)
    {
        this.txCodeInputMode = inputMode;
    }


    public String getTxCodeDescription()
    {
        return txCodeDescription;
    }


    public void setTxCodeDescription(String description)
    {
        this.txCodeDescription = description;
    }


    public int getDuration()
    {
        return duration;
    }


    public void setDuration(int duration)
    {
        this.duration = duration;
    }


    public String getCredentialOfferEndpoint()
    {
        return credentialOfferEndpoint;
    }


    public void setCredentialOfferEndpoint(String credentialOfferEndpoint)
    {
        this.credentialOfferEndpoint = credentialOfferEndpoint;
    }


    public CredentialOfferInfo getInfo()
    {
        return info;
    }


    public void setInfo(CredentialOfferInfo info)
    {
        this.info = info;

        try
        {
            this.credentialOfferLink = String.format(CREDENTIAL_OFFER_QR_PATTERN, credentialOfferEndpoint,
                                                     URLEncoder.encode(info.getCredentialOffer(), "UTF-8"));
            this.credentialOfferQrCode = asQrCode(this.credentialOfferLink);

            this.credentialOfferUri = String.format("%s/api/offer/%s",
                                                            info.getCredentialIssuer().toString(),
                                                            info.getIdentifier());
            this.credentialOfferUriLink = String.format(CREDENTIAL_OFFER_URI_QR_PATTERN, credentialOfferEndpoint,
                                                        URLEncoder.encode(credentialOfferUri, "UTF-8"));
            this.credentialOfferUriQrCode = asQrCode(this.credentialOfferUriLink);
        }
        catch (IOException | WriterException e)
        {
            throw ExceptionUtil.internalServerErrorException("Can't generate QR code.");
        }

        this.credentialOfferContent = info.getCredentialOffer();

        try
        {
            this.credentialOfferContent = prettifyJson(this.credentialOfferContent);
        }
        catch (JsonParseException ignored)
        {}
    }


    public String getCredentialOfferLink()
    {
        return credentialOfferLink;
    }


    public void setCredentialOfferLink(String credentialOfferLink)
    {
        this.credentialOfferLink = credentialOfferLink;
    }


    public String getCredentialOfferQrCode()
    {
        return credentialOfferQrCode;
    }


    public void setCredentialOfferQrCode(String credentialOfferQrCode)
    {
        this.credentialOfferQrCode = credentialOfferQrCode;
    }


    public String getCredentialOfferContent()
    {
        return credentialOfferContent;
    }


    public void setCredentialOfferContent(String credentialOfferContent)
    {
        this.credentialOfferContent = credentialOfferContent;
    }


    public String getCredentialOfferUri()
    {
        return credentialOfferUri;
    }


    public void setCredentialOfferUri(String credentialOfferUri)
    {
        this.credentialOfferUri = credentialOfferUri;
    }


    public String getCredentialOfferUriLink()
    {
        return credentialOfferUriLink;
    }


    public void setCredentialOfferUriLink(String credentialOfferUriLink)
    {
        this.credentialOfferUriLink = credentialOfferUriLink;
    }


    public String getCredentialOfferUriQrCode()
    {
        return credentialOfferUriQrCode;
    }


    public void setCredentialOfferUriQrCode(String credentialOfferUriQrCode)
    {
        this.credentialOfferUriQrCode = credentialOfferUriQrCode;
    }
}
