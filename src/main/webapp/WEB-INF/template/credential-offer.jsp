<!doctype html>
<!--<%
/*
 * Copyright (C) 2016-2025 Authlete, Inc.
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
  <title>Credential Offer</title>
  <link rel="stylesheet" href="/css/authorization.css">
  <!-- <% /* //-->
  <link rel="stylesheet" href="../../css/authorization.css">
  <!-- */ %> //-->
  <style type="text/css">
  input, textarea {
    font-family: monospace;
    font-size: 110%;
    border: 1px solid #0c132a;
    padding: 5px;
  }
  .category {
    background-color: #106cb6;
    color: white;
    padding: 5px;
  }
  .subcategory {
    background-color: #c9d5e3;
    color: #0c132a;
    padding: 5px;
  }
  .border-top {
    border-top: 1px solid #0c132a;
  }
  .submit_button {
    padding: 8px;
    margin: 5px;
    font-size: 120%;
    width: 300px;
    border-width: 1px;
    border-radius: 10px;
    background-color: #fbab01;
  }
  .submit_button:hover {
    background-color: #ffcc33;
  }
  </style>

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
      cascadeDependency("preAuthorizedCodeGrantIncluded", "txCode");
      cascadeDependency("preAuthorizedCodeGrantIncluded", "txCodeInputMode");
      cascadeDependency("preAuthorizedCodeGrantIncluded", "txCodeDescription");
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
          <div>Input Login ID and Password.</div>
          <input type="text" id="loginId" name="loginId" placeholder="Login ID"
            autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
            value="${model.loginId}" ${model.loginIdReadOnly}>
          <input type="password" id="password" name="password" placeholder="Password">
          </div>
        </div>
      </div>
      </c:if>

      <c:choose>
        <c:when test="${model.info == null}">
          <div class="indent">
            <h4>Offer parameters</h4>

            <div class="indent">

            <table border="1" rules="cols" cellpadding="5" style="border-collapse: collapse;" class="indent">
              <tr class="category">
                <td colspan="3">Credentials</td>
              </tr>

              <tr>
                <td colspan="2"><nobr>Credential Configuration IDs</nobr></td>
                <td>
                  <textarea id="credentialConfigurationIds" name="credentialConfigurationIds"
                            rows="5" cols="40">${model.credentialConfigurationIds}</textarea>
                </td>
              </tr>

              <tr class="category">
                <td colspan="3">Grants</td>
              </tr>

              <tr>
                <td colspan="2"><nobr>Authorization Code Grant</nobr></td>
                <td>
                  <input type="checkbox" id="authorizationCodeGrantIncluded" name="authorizationCodeGrantIncluded"
                         <c:if test="${model.authorizationCodeGrantIncluded}">checked</c:if> >
                  <label for="authorizationCodeGrantIncluded"> include?</label>
                </td>
              </tr>

              <tr>
                <td width="20"></td>
                <td class="border-top subcategory"><nobr>Issue State</nobr></td>
                <td class="border-top">
                  <input type="checkbox" id="issuerStateIncluded" name="issuerStateIncluded"
                         <c:if test="${model.issuerStateIncluded}">checked</c:if> >
                  <label for="issuerStateIncluded"> include?</label>
                </td>
              </tr>

              <tr class="border-top">
                <td colspan="2"><nobr>Pre-Authorized Code Grant</nobr></td>
                <td>
                  <input type="checkbox" id="preAuthorizedCodeGrantIncluded" name="preAuthorizedCodeGrantIncluded"
                         <c:if test="${model.preAuthorizedCodeGrantIncluded}">checked</c:if> >
                  <label for="preAuthorizedCodeGrantIncluded"> include?</label>
                </td>
              </tr>

              <tr>
                <td width="20"></td>
                <td colspan="2" class="border-top subcategory">Transaction Code</td>
              </tr>

              <tr>
                <td width="20"></td>
                <td class="border-top"><nobr>Value</nobr></td>
                <td class="border-top">
                  <input type="text" id="txCode" name="txCode"
                         value="${model.txCode}" size="40"
                         placeholder="e.g. 493536">
                </td>
              </tr>

              <tr>
                <td width="20"></td>
                <td><nobr>Input Mode</nobr></td>
                <td>
                  <input type="text" id="txCodeInputMode" name="txCodeInputMode"
                         value="${model.txCodeInputMode}" size="40"
                         placeholder="numeric or text">
                </td>
              </tr>

              <tr>
                <td width="20"></td>
                <td><nobr>Description</nobr></td>
                <td>
                  <textarea id="txCodeDescription" name="txCodeDescription" rows="2" cols="40"
                            placeholder="e.g. Please provide the one-time code which was sent via e-mail"
                            >${model.txCodeDescription}</textarea>
                </td>
              </tr>

              <tr class="category">
                <td colspan="3">Transmission</td>
              </tr>

              <tr>
                <td colspan="2"><nobr>Credential Offer Endpoint</nobr></td>
                <td>
                  <input type="text" id="credentialOfferEndpoint" name="credentialOfferEndpoint"
                         value="${model.credentialOfferEndpoint}" size="40">
                </td>
              </tr>

              <tr>
                <td colspan="3" class="border-top" align="center">
                  <button type="submit" name="authorized" value="Submit" class="submit_button">Submit</button>
                </td>
              </tr>
            </table>

            </div>

          </div>
        </c:when>
        <c:otherwise>
          <div class="indent">
            <h4>Offer</h4>

            <c:if test="${model.info.txCode != null}">
            <p>Transaction Code: ${model.info.txCode}</p>
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

            <c:if test="${model.credentialOfferUri != null}">
            <div class="indent">
              <code><a href="${model.credentialOfferUri}">${model.credentialOfferUri}</a></code>
            </div>
            <br/>
            </c:if>
          </div>
        </c:otherwise>
      </c:choose>
    </form>
  </div>
</body>
</html>
