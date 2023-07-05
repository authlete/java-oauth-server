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
package com.authlete.jaxrs.server.api;


import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Map;
import javax.imageio.ImageIO;
import com.authlete.common.dto.CredentialOfferCreateRequest;
import com.authlete.common.dto.CredentialOfferInfo;
import com.authlete.common.types.User;
import com.authlete.jaxrs.AuthorizationPageModel;
import com.authlete.jaxrs.server.util.ExceptionUtil;
import com.authlete.jaxrs.server.util.ProcessingUtil;
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
    private static final long serialVersionUID = 1L;


    private static final String DEFAULT_ENDPOINT = "openid-credential-offer://";
    private static final String CREDENTIAL_OFFER_QR_PATTERN = "%s?credential_offer=%s";
    private static final String CREDENTIAL_OFFER_URI_QR_PATTERN = "%s?credential_offer_uri=%s";


    private static final String DEFAULT_CREDENTIALS = "[\n" +
            "  {\n" +
            "    \"format\": \"vc+sd-jwt\",\n" +
            "    \"type\": \"IdentityCredential\",\n" +
            "    \"credential_definition\": {\n" +
            "      \"given_name\": {},\n" +
            "      \"family_name\": {},\n" +
            "      \"birthdate\": {}\n" +
            "    }\n" +
            "  }\n" +
            "]";


    private String credentials;
    private boolean authorizationCodeGrantIncluded;
    private boolean issuerStateIncluded;
    private boolean preAuthorizedCodeGrantIncluded;
    private boolean userPinRequired;
    private int userPinLength;
    private int duration;
    private String credentialOfferEndpoint;
    private CredentialOfferInfo info;
    private String credentialOfferQrCode;
    private String credentialOfferUriQrCode;


    public CredentialOfferPageModel()
    {
        this.authorizationCodeGrantIncluded = false;
        this.issuerStateIncluded = true;
        this.preAuthorizedCodeGrantIncluded = true;
        this.userPinRequired = false;
        this.userPinLength = 0;
        this.duration = 0;
        this.credentials = DEFAULT_CREDENTIALS;
        this.credentialOfferEndpoint = DEFAULT_ENDPOINT;
    }


    public CredentialOfferPageModel setValues(final Map<String, String> values)
    {
        this.credentials = values.getOrDefault("credentials", this.credentials);
        this.authorizationCodeGrantIncluded = ProcessingUtil.fromFormCheckbox(values, "authorizationCodeGrantIncluded");
        this.issuerStateIncluded = ProcessingUtil.fromFormCheckbox(values, "issuerStateIncluded");
        this.preAuthorizedCodeGrantIncluded = ProcessingUtil.fromFormCheckbox(values, "preAuthorizedCodeGrantIncluded");
        this.userPinRequired = ProcessingUtil.fromFormCheckbox(values, "userPinRequired");
        this.credentialOfferEndpoint = values.getOrDefault("credentialOfferEndpoint", this.credentialOfferEndpoint);

        final String userPinLengthStr = values.getOrDefault("userPinLength", Integer.toString(this.userPinLength));
        final String durationStr = values.getOrDefault("duration", Integer.toString(this.duration));

        try
        {
            userPinLength = Integer.parseInt(userPinLengthStr);
        }
        catch (NumberFormatException e)
        {
            throw ExceptionUtil.badRequestException("User pin length should be a number");
        }

        try
        {
            duration = Integer.parseInt(durationStr);
        }
        catch (NumberFormatException e)
        {
            throw ExceptionUtil.badRequestException("Duration should be a number");
        }

        return this;
    }


    private String generateQrCode(final String text) throws IOException, WriterException
    {
        final QRCodeWriter qrCodeWriter = new QRCodeWriter();
        final BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300);

        final BufferedImage qrCode = MatrixToImageWriter.toBufferedImage(bitMatrix);

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(qrCode, "png", output);
        return Base64.getEncoder().encodeToString(output.toByteArray());
    }


    public CredentialOfferCreateRequest toRequest(final User user)
    {
        return new CredentialOfferCreateRequest()
                .setAuthorizationCodeGrantIncluded(this.authorizationCodeGrantIncluded)
                .setIssuerStateIncluded(this.issuerStateIncluded)
                .setPreAuthorizedCodeGrantIncluded(this.preAuthorizedCodeGrantIncluded)
                .setUserPinRequired(this.userPinRequired)
                .setUserPinLength(this.userPinLength)
                .setDuration(this.duration)
                .setCredentials(this.credentials)
                .setSubject(user.getSubject());
    }


    public String getCredentials()
    {
        return credentials;
    }


    public void setCredentials(String credentials)
    {
        this.credentials = credentials;
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


    public boolean isUserPinRequired()
    {
        return userPinRequired;
    }


    public void setUserPinRequired(boolean userPinRequired)
    {
        this.userPinRequired = userPinRequired;
    }


    public int getUserPinLength()
    {
        return userPinLength;
    }


    public void setUserPinLength(int userPinLength)
    {
        this.userPinLength = userPinLength;
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
            this.credentialOfferQrCode = generateQrCode(
                    String.format(CREDENTIAL_OFFER_QR_PATTERN, credentialOfferEndpoint,
                                  URLEncoder.encode(info.getCredentialOffer(), "UTF-8")));

            final String credentialOfferUri = String.format("%s/api/offer/%s",
                                                            info.getCredentialIssuer().toString(),
                                                            info.getIdentifier());
            this.credentialOfferUriQrCode = generateQrCode(
                    String.format(CREDENTIAL_OFFER_URI_QR_PATTERN, credentialOfferEndpoint,
                                  URLEncoder.encode(credentialOfferUri, "UTF-8")));
        }
        catch (IOException | WriterException e)
        {
            throw ExceptionUtil.internalServerErrorException("Can't generate QR code.");
        }
    }


    public String getCredentialOfferQrCode()
    {
        return credentialOfferQrCode;
    }


    public void setCredentialOfferQrCode(String credentialOfferQrCode)
    {
        this.credentialOfferQrCode = credentialOfferQrCode;
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
