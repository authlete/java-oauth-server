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
  <title>${model.serviceName} | Authorization</title>
  <link rel="stylesheet" href="/css/authorization.css">
  <!-- <% /* //-->
  <link rel="stylesheet" href="../../css/authorization.css">
  <!-- */ %> //-->
</head>
<body class="font-default">
  <div id="page_title">${model.serviceName}</div>

  <div id="content">
    <h3 id="client-name">${model.clientName}</h3>
    <div class="indent">
      <img id="logo" src="${model.logoUri}" alt="[Logo] (150x150)">

      <div id="client-summary">
        <p>${model.description}</p>
        <ul id="client-link-list">
          <c:if test="${model.clientUri != null}">
          <li><a target="_blank" href="${model.clientUri}">Homepage</a>
          </c:if>

          <c:if test="${model.policyUri != null}">
          <li><a target="_blank" href="${model.policyUri}">Policy</a>
          </c:if>

          <c:if test="${model.tosUri != null}">
          <li><a target="_blank" href="${model.tosUri}">Terms of Service</a>
          </c:if>
        </ul>
      </div>

      <div style="clear: both;"></div>
    </div>

    <c:if test="${model.scopes != null}">
    <h4 id="permissions">Permissions</h4>
    <div class="indent">
      <p>The application is requesting the following permissions.</p>

      <dl id="scope-list">
        <c:forEach var="scope" items="${model.scopes}">
        <dt>${scope.name}</dt>
        <dd>${scope.description}</dd>
        </c:forEach>
      </dl>
    </div>
    </c:if>

    <c:if test="${model.claimsForIdToken != null}">
    <h4 id="claims-for-id_token">Claims for ID Token</h4>
    <div class="indent">
      <ul>
        <c:forEach var="claim" items="${model.claimsForIdToken}">
        <li>${claim}
        </c:forEach>
      </ul>
    </div>
    </c:if>

    <c:if test="${model.claimsForUserInfo != null}">
    <h4 id="claims-for-userinfo">Claims for UserInfo</h4>
    <div class="indent">
      <ul>
        <c:forEach var="claim" items="${model.claimsForUserInfo}">
        <li>${claim}
        </c:forEach>
      </ul>
    </div>
    </c:if>

    <c:if test="${model.identityAssuranceRequired}">
    <h4 id="identity-assurance">Identity Assurance</h4>
    <div class="indent">
      <c:if test="${model.purpose != null}">
      <h5>Purpose</h5>
      <div class="indent">
        <p>${model.purpose}</p>
      </div>
      </c:if>
      <c:if test="${model.allVerifiedClaimsForIdTokenRequested || model.verifiedClaimsForIdToken != null}">
      <h5>Verified claims requested for ID token</h5>
      <div class="indent">
        <c:if test="${model.allVerifiedClaimsForIdTokenRequested}">
        All
        </c:if>
        <c:if test="${model.verifiedClaimsForIdToken != null}">
        <table border="1" cellpadding="5" style="border-collapse: collapse;"class="verified-claims">
          <thead>
            <tr bgcolor="orange">
              <th>claim</th>
              <th>purpose</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="pair" items="${model.verifiedClaimsForIdToken}">
            <tr>
              <td>${pair.key}</td>
              <td>${pair.value}</td>
            </tr>
            </c:forEach>
          </tbody>
        </table>
        </c:if>
      </div>
      </c:if>
      <c:if test="${model.allVerifiedClaimsForUserInfoRequested || model.verifiedClaimsForUserInfo != null}">
      <h5>Verified claims requested for userinfo</h5>
      <div class="indent">
        <c:if test="${model.allVerifiedClaimsForUserInfoRequested}">
        All
        </c:if>
        <c:if test="${model.verifiedClaimsForUserInfo != null}">
        <table border="1" cellpadding="5" style="border-collapse: collapse;">
          <thead>
            <tr bgcolor="orange">
              <th>claim</th>
              <th>purpose</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="pair" items="${model.verifiedClaimsForUserInfo}">
            <tr>
              <td>${pair.key}</td>
              <td>${pair.value}</td>
            </tr>
            </c:forEach>
          </tbody>
        </table>
        </c:if>
      </div>
      </c:if>
    </div>
    </c:if>

    <c:if test="${model.authorizationDetails != null}">
    <h4 id="authorization-details">Authorization Details</h4>
    <div class="indent">
<pre>
${model.authorizationDetails}
</pre>
    </div>
    </c:if>

    <h4 id="authorization">Authorization</h4>
    <div class="indent">
      <p>Do you grant authorization to the application?</p>

      <form id="authorization-form" action="/api/authorization/decision" method="POST">
        <c:if test="${model.user == null}">
        <div id="login-fields" class="indent">
          <div id="login-prompt">Input Login ID and Password.</div>
          <input type="text" id="loginId" name="loginId" placeholder="Login ID"
                 autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                 class="font-default" value="${model.loginId}" ${model.loginIdReadOnly}>
          <input type="password" id="password" name="password" placeholder="Password"
                 class="font-default">
        </div>
        <c:if test="${model.federations != null}">
        <div id="federations" class="indent">
          <div id="federations-prompt">ID federation using an external OpenID Provider</div>
          <c:if test="${model.federationMessage != null}">
          <div id="federation-message">${model.federationMessage}</div>
          </c:if>
          <ul>
          <c:forEach var="federation" items="${model.federations}">
            <li><a href="/api/federation/initiation/${federation.id}">${federation.server.name}</a>
          </c:forEach>
          </ul>
        </div>
        </c:if>
        </c:if>
        <c:if test="${model.user != null}">
        <div id="login-user" class="indent">
          Logged in as <b><c:out value="${model.user.subject}" /></b>.
          If re-authentication is needed, append <code>&amp;prompt=login</code>
          to the authorization request.
        </div>
        </c:if>
        <div id="authorization-form-buttons">
          <input type="submit" name="authorized" id="authorize-button" value="Authorize" class="font-default"/>
          <input type="submit" name="denied"     id="deny-button"      value="Deny"      class="font-default"/>
        </div>
      </form>
    </div>
  </div>

</body>
</html>
