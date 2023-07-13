<!doctype html>
<!--<%
/*
 * Copyright (C) 2016-2023 Authlete, Inc.
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
%>-->
<!--
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% response.setHeader("Cache-Control", "no-store"); %>
-->
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes">
  <title>Authorization</title>
  <link rel="stylesheet" href="/css/authorization.css">
  <!-- <% /* //-->
  <link rel="stylesheet" href="../../css/authorization.css">
  <!-- */ %> //-->

  <script>
    function cascadeDependency(src, dst) {
      const srcElem = document.getElementById(src);
      const dstElem = document.getElementById(dst);

      dstElem.disabled = !srcElem.checked
    }

    function cascadeAuthorizationCodeGrantIncluded() {
      cascadeDependency("authorizationCodeGrantIncluded", "issuerStateIncluded");
    }

    function cascadePreAuthorizedCodeGrantIncluded() {
      cascadeDependency("preAuthorizedCodeGrantIncluded", "userPinRequired");
      cascadeDependency("preAuthorizedCodeGrantIncluded", "userPinLength");
    }

    window.onload = function() {
      cascadeAuthorizationCodeGrantIncluded();
      cascadePreAuthorizedCodeGrantIncluded();

      document.getElementById("authorizationCodeGrantIncluded")
              .addEventListener('click', cascadeAuthorizationCodeGrantIncluded);
      document.getElementById("preAuthorizedCodeGrantIncluded")
              .addEventListener('click', cascadePreAuthorizedCodeGrantIncluded);
    };
  </script>
</head>
<body class="font-default">
  <div id="page_title">Credential Offer</div>

  <div>
    <form id="credential-offer-form" action="/api/offer/issue" method="POST">
      <c:if test="${model.user == null}">
      <div class="indent">
        <h4 id="authorization">Login</h4>
        <div class="indent">
          <div id="login-fields" class="indent">
          <div id="login-prompt">Input Login ID and Password.</div>
          <input type="text" id="loginId" name="loginId" placeholder="Login ID"
            autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
            class="font-default" value="${model.loginId}" ${model.loginIdReadOnly}>
          <input type="password" id="password" name="password" placeholder="Password"
            class="font-default">
          </div>
        </div>
      </div>
      </c:if>

      <c:choose>
        <c:when test="${model.info == null}">
          <div class="indent">
            <h4>Offer parameters</h4>

            <table class="indent">
              <tr>
                <td><label for="authorizationCodeGrantIncluded">Authorization code grant included</label></td>
                <td>
                  <input type="checkbox" id="authorizationCodeGrantIncluded" name="authorizationCodeGrantIncluded"
                         <c:if test="${model.authorizationCodeGrantIncluded}">checked</c:if> class="font-default">
                </td>
              </tr>
              <tr>
                <td><div class="indent"><label for="issuerStateIncluded">Issuer state included</label></div></td>
                <td>
                  <input type="checkbox" id="issuerStateIncluded" name="issuerStateIncluded"
                         <c:if test="${model.issuerStateIncluded}">checked</c:if> class="font-default">
                </td>
              </tr>
              <tr></tr>
              <tr>
                <td><label for="preAuthorizedCodeGrantIncluded">Pre-authorized code grant included</label></td>
                <td>
                  <input type="checkbox" id="preAuthorizedCodeGrantIncluded" name="preAuthorizedCodeGrantIncluded"
                         <c:if test="${model.preAuthorizedCodeGrantIncluded}">checked</c:if> class="font-default">
                </td>
              </tr>
              <tr>
                <td><div class="indent"><label for="userPinRequired">User pin required</label></div></td>
                <td>
                  <input type="checkbox" id="userPinRequired" name="userPinRequired"
                         <c:if test="${model.userPinRequired}">checked</c:if> class="font-default">
                </td>
              </tr>
              <tr>
                <td><div class="indent"><label for="userPinLength">User pin length</label></div></td>
                <td>
                  <input type="number" id="userPinLength" name="userPinLength" value="${model.userPinLength}"
                         min="0" max="8" class="font-default">
                </td>
              </tr>
              <tr></tr>
              <tr>
                <td><label for="credentials">Credentials</label></td>
                <td>
                  <textarea id="credentials" name="credentials" rows="10" cols="40" class="font-default">${model.credentials}</textarea>
                </td>
              </tr>
              <tr>
                <td><label for="credentialOfferEndpoint">Credential offer endpoint</label></td>
                <td>
                  <input type="text" id="credentialOfferEndpoint" name="credentialOfferEndpoint"
                         value="${model.credentialOfferEndpoint}" size="40" class="font-default">
                </td>
              </tr>
            </table>
            <div id="authorization-form-buttons">
              <input type="submit" name="authorized" id="authorize-button" value="Submit" class="font-default"/>
            </div>
          </div>
        </c:when>
        <c:otherwise>
          <div class="indent">
            <h4>Offer</h4>

            <c:if test="${model.info.userPin != null}">
            <p>User pin: ${model.info.userPin}</p>
            </c:if>

            <c:if test="${model.credentialOfferQrCode != null}">
            <h5>Credential offer</h5>
            <a href="${model.credentialOfferLink}">
              <img src="data:image/png;base64,${model.credentialOfferQrCode}">
            </a>
            <br/>
            <div class="indent">
              <pre><code>${model.credentialOfferContent}</code></pre>
            </div>
            <br/>
            </c:if>

            <c:if test="${model.credentialOfferUriQrCode != null}">
            <h5>Credential offer URI</h5>
            <a href="${model.credentialOfferUriLink}">
              <img src="data:image/png;base64,${model.credentialOfferUriQrCode}"/>
            </a>
            <br/>
            </c:if>
          </div>
        </c:otherwise>
      </c:choose>
    </form>
  </div>
</body>
</html>
