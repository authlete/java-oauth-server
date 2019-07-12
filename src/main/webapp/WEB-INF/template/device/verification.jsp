<!doctype html>
<!--<%
/*
 * Copyright (C) 2019 Authlete, Inc.
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
  <title>Verification</title>
  <link rel="stylesheet" href="/css/device/verification.css">
  <!-- <% /* //-->
  <link rel="stylesheet" href="../../css/device/verification.css">
  <!-- */ %> //-->
</head>
<body class="font-default">
  <div id="page_title">Device Flow Verification</div>

  <div id="content">
    <h4 id="verification">Verification</h4>
    <div class="indent">
      <c:if test="${model.notification != null}">
      <p id="notification">${model.notification}</p>
      </c:if>    
      <p>Enter required information below.</p>

      <form id="verification-form" action="/api/device/verification" method="POST">
        <c:if test="${model.user == null}">
        <div id="login-fields" class="indent">
          <div id="login-prompt">Input Login ID and password.</div>
          <input type="text" id="loginId" name="loginId" placeholder="Login ID"
                 class="font-default" required value="${model.loginId}">
          <input type="password" id="password" name="password" placeholder="Password"
                 class="font-default" required>
        </div>
        </c:if>

        <c:if test="${model.user != null}">
        <div id="login-user"><i>Logged in as <c:out value="${model.user.subject}" /></i></div>
        </c:if>

        <div id="usercode-field" class="indent">
          <div id="usercode-prompt">Input User Code.</div>
          <input type="text" id="userCode" name="userCode" placeholder="User Code"
                 class="font-default" required value="${model.userCode}">
        </div>

        <div id="verification-form-button">
          <input type="submit" name="send" id="send-button" value="Send" class="font-default"/>
        </div>
      </form>
    </div>
  </div>

</body>
</html>
