Authorization Server Implementation in Java
===========================================

Overview
--------

This is an authorization server implementation in Java which supports
[OAuth 2.0][1] and [OpenID Connect][2].

This implementation is written using JAX-RS 2.0 API and [authlete-java-jaxrs][3]
library. JAX-RS is _The Java API for RESTful Web Services_. JAX-RS 2.0 API has
been standardized by [JSR 339][4] and it is included in Java EE 7. On the other
hand, authlete-java-jaxrs library is an open source library which provides utility
classes for developers to implement an authorization server and a resource server.
authlete-java-jaxrs in turn uses [authlete-java-common][5] library which is
another open source library to communicate with [Authlete Web APIs][6].

This implementation is _DB-less_. What this means is that you don't have to
have a database server that stores authorization data (e.g. access tokens),
settings of the authorization server itself and settings of client applications.
This is achieved by using [Authlete][7] as a backend service.

Access tokens issued by this authorization server can be used at a resource
server which uses Authlete as a backend service. [java-resource-server][40]
is such a resource server implementation. It includes an example implementation
of protected resource endpoint.


License
-------

  Apache License, Version 2.0

  JSON files under `src/main/resources/ekyc-ida` have been copied from
  https://bitbucket.org/openid/ekyc-ida/src/master/examples/response/ .
  Regarding their license, ask the eKYC-IDA WG of OpenID Foundation.


Source Code
-----------

  <code>https://github.com/authlete/java-oauth-server</code>


About Authlete
--------------

[Authlete][7] is a cloud service that provides an implementation of OAuth 2.0
& OpenID Connect ([overview][8]). You can easily get the functionalities of
OAuth 2.0 and OpenID Connect either by using the default implementation
provided by Authlete or by implementing your own authorization server using
[Authlete Web APIs][6] as this implementation (java-oauth-server) does.

To use this authorization server implementation, you need to get API credentials
from Authlete and set them in `authlete.properties`. The steps to get API
credentials are very easy. All you have to do is just to register your account
([sign up][9]). See [Getting Started][10] for details.


How To Run
----------

1. Download the source code of this authorization server implementation.

        $ git clone https://github.com/authlete/java-oauth-server.git
        $ cd java-oauth-server

2. Edit the configuration file to set the API credentials of yours.

        $ vi authlete.properties

3. Make sure that you have installed [maven][42] and set `JAVA_HOME` properly.

4. Start the authorization server on [http://localhost:8080][38].

        $ mvn jetty:run &

#### Run With Docker

If you would prefer to use Docker, just hit the following command after the step 2.

    $ docker-compose up

#### Configuration File

`java-oauth-server` refers to `authlete.properties` as a configuration file.
If you want to use another different file, specify the name of the file by
the system property `authlete.configuration.file` like the following.

    $ mvn -Dauthlete.configuration.file=local.authlete.properties jetty:run &


Endpoints
---------

This implementation exposes endpoints as listed in the table below.

| Endpoint                             | Path                                |
|:-------------------------------------|:------------------------------------|
| Authorization Endpoint               | `/api/authorization`                |
| Token Endpoint                       | `/api/token`                        |
| JWK Set Endpoint                     | `/api/jwks`                         |
| Configuration Endpoint               | `/.well-known/openid-configuration` |
| Revocation Endpoint                  | `/api/revocation`                   |
| Introspection Endpoint               | `/api/introspection`                |
| UserInfo Endpoint                    | `/api/userinfo`                     |
| Dynamic Client Registration Endpoint | `/api/register`                     |
| Pushed Authorization Request Endpoint| `/api/par`                          |
| Grant Management Endpoint            | `/api/gm/{grantId}`                 |

The authorization endpoint and the token endpoint accept parameters described
in [RFC 6749][1], [OpenID Connect Core 1.0][13],
[OAuth 2.0 Multiple Response Type Encoding Practices][33], [RFC 7636][14]
([PKCE][15]) and other specifications.

The JWK Set endpoint exposes a JSON Web Key Set document (JWK Set) so that
client applications can (1) verify signatures by this OpenID Provider and
(2) encrypt their requests to this OpenID Provider.

The configuration endpoint exposes the configuration information of this
OpenID Provider in the JSON format defined in [OpenID Connect Discovery 1.0][35].

The revocation endpoint is a Web API to revoke access tokens and refresh
tokens. Its behavior is defined in [RFC 7009][21].

The introspection endpoint is a Web API to get information about access
tokens and refresh tokens. Its behavior is defined in [RFC 7662][32].

The userinfo endpoint is a Web API to get information about an end-user.
Its behavior is defined in [Section 5.3. UserInfo Endpoint][41] of
[OpenID Connect Core 1.0][13].

The dynamic client registration endpoint is a Web API to register and update
client applications. Its behavior is defined in [RFC 7591][43] and [RFC 7592][44].

The pushed authorization request endpoint (a.k.a. PAR endpoint) is a Web API
to register an authorization request in advance and obtain a request URI.
Its behavior is defined in [RFC 9126][45].

The grant management endpoint is a Web API to get information about a grant ID
and revoke a grant ID. Its behavior is defined in [Grant Management for OAuth 2.0][46].


Authorization Request Example
-----------------------------

The following is an example to get an access token from the authorization
endpoint using [Implicit Flow][16]. Don't forget to replace `{client-id}` in
the URL with the real client ID of one of your client applications. As for
client applications, see [Getting Started][10] and the [document][17] of
_Developer Console_.

    http://localhost:8080/api/authorization?client_id={client-id}&response_type=token

The request above will show you an authorization page. The page asks you to
input login credentials and click "Authorize" button or "Deny" button. Use
one of the following as login credentials.

| Login ID | Password |
|:--------:|:--------:|
|   john   |   john   |
|   jane   |   jane   |
|   max    |   max    |
|   inga   |   inga   |

Of course, these login credentials are dummy data, so you need to replace
the user database implementation with your own.

The account `max` is for the old draft of
[OpenID Connect for Identity Assurance 1.0][IDA] (IDA). The account holds
_verified claims_ in the old format. Authlete 2.2 accepts the old format
but Authlete 2.3 onwards will reject it.

The account `inga` is for the third Implementer's Draft of [IDA][IDA] onwards.
Use `inga` for testing the latest IDA specification. However, note that
the third Implementer's Draft onwards is supported from Authlete 2.3.
Older Authlete versions do not support the latest IDA specification.


Customization
-------------

How to customize this implementation is described in [CUSTOMIZATION.md][39].
Basically, you need to do programming for _end-user authentication_ because
Authlete does not manage end-user accounts. This is by design. The
architecture of Authlete carefully separates authorization from authentication
so that you can add OAuth 2.0 and OpenID Connect functionalities seamlessly
into even an existing web service which may already have a mechanism for
end-user authentication.


Implementation Note
-------------------

This implementation uses `Viewable` class to implement the authorization page.
The class is included in [Jersey][18] (the reference implementation of JAX-RS),
but it is not a part of JAX-RS 2.0 API.


Related Specifications
----------------------

- [RFC 6749][1] - The OAuth 2.0 Authorization Framework
- [RFC 6750][19] - The OAuth 2.0 Authorization Framework: Bearer Token Usage
- [RFC 6819][20] - OAuth 2.0 Threat Model and Security Considerations
- [RFC 7009][21] - OAuth 2.0 Token Revocation
- [RFC 7033][22] - WebFinger
- [RFC 7515][23] - JSON Web Signature (JWS)
- [RFC 7516][24] - JSON Web Encryption (JWE)
- [RFC 7517][25] - JSON Web Key (JWK)
- [RFC 7518][26] - JSON Web Algorithms (JWA)
- [RFC 7519][27] - JSON Web Token (JWT)
- [RFC 7521][28] - Assertion Framework for OAuth 2.0 Client Authentication and Authorization Grants
- [RFC 7522][29] - Security Assertion Markup Language (SAML) 2.0 Profile for OAuth 2.0 Client Authentication and Authorization Grants
- [RFC 7523][30] - JSON Web Token (JWT) Profile for OAuth 2.0 Client Authentication and Authorization Grants
- [RFC 7591][43] - OAuth 2.0 Dynamic Client Registration Protocol
- [RFC 7592][44] - OAuth 2.0 Dynamic Client Registration Management Protocol
- [RFC 7636][31] - Proof Key for Code Exchange by OAuth Public Clients
- [RFC 7662][32] - OAuth 2.0 Token Introspection
- [RFC 9126][45] - OAuth 2.0 Pushed Authorization Requests
- [OAuth 2.0 Multiple Response Type Encoding Practices][33]
- [OAuth 2.0 Form Post Response Mode][34]
- [OpenID Connect Core 1.0][13]
- [OpenID Connect Discovery 1.0][35]
- [OpenID Connect Dynamic Client Registration 1.0][36]
- [OpenID Connect Session Management 1.0][37]


See Also
--------

- [Authlete][7] - Authlete Home Page
- [authlete-java-common][5] - Authlete Common Library for Java
- [authlete-java-jaxrs][3] - Authlete Library for JAX-RS (Java)
- [java-resource-server][40] - Resource Server Implementation


Contact
-------

| Purpose   | Email Address        |
|:----------|:---------------------|
| General   | info@authlete.com    |
| Sales     | sales@authlete.com   |
| PR        | pr@authlete.com      |
| Technical | support@authlete.com |


[1]: https://www.rfc-editor.org/rfc/rfc6749.html
[2]: https://openid.net/connect/
[3]: https://github.com/authlete/authlete-java-jaxrs
[4]: https://jcp.org/en/jsr/detail?id=339
[5]: https://github.com/authlete/authlete-java-common
[6]: https://docs.authlete.com/
[7]: https://www.authlete.com/
[8]: https://www.authlete.com/developers/overview/
[9]: https://so.authlete.com/accounts/signup
[10]: https://www.authlete.com/developers/getting_started/
[11]: https://www.rfc-editor.org/rfc/rfc6749.html#section-3.1
[12]: https://www.rfc-editor.org/rfc/rfc6749.html#section-3.2
[13]: https://openid.net/specs/openid-connect-core-1_0.html
[14]: https://www.rfc-editor.org/rfc/rfc7636.html
[15]: https://www.authlete.com/developers/pkce/
[16]: https://www.rfc-editor.org/rfc/rfc6749.html#section-4.2
[17]: https://www.authlete.com/developers/cd_console/
[18]: https://jersey.java.net/
[19]: https://www.rfc-editor.org/rfc/rfc6750.html
[20]: https://www.rfc-editor.org/rfc/rfc6819.html
[21]: https://www.rfc-editor.org/rfc/rfc7009.html
[22]: https://www.rfc-editor.org/rfc/rfc7033.html
[23]: https://www.rfc-editor.org/rfc/rfc7515.html
[24]: https://www.rfc-editor.org/rfc/rfc7516.html
[25]: https://www.rfc-editor.org/rfc/rfc7517.html
[26]: https://www.rfc-editor.org/rfc/rfc7518.html
[27]: https://www.rfc-editor.org/rfc/rfc7519.html
[28]: https://www.rfc-editor.org/rfc/rfc7521.html
[29]: https://www.rfc-editor.org/rfc/rfc7522.html
[30]: https://www.rfc-editor.org/rfc/rfc7523.html
[31]: https://www.rfc-editor.org/rfc/rfc7636.html
[32]: https://www.rfc-editor.org/rfc/rfc7662.html
[33]: https://openid.net/specs/oauth-v2-multiple-response-types-1_0.html
[34]: https://openid.net/specs/oauth-v2-form-post-response-mode-1_0.html
[35]: https://openid.net/specs/openid-connect-discovery-1_0.html
[36]: https://openid.net/specs/openid-connect-registration-1_0.html
[37]: https://openid.net/specs/openid-connect-session-1_0.html
[38]: http://localhost:8080
[39]: doc/CUSTOMIZATION.md
[40]: https://github.com/authlete/java-resource-server
[41]: https://openid.net/specs/openid-connect-core-1_0.html#UserInfo
[42]: https://maven.apache.org/
[43]: https://www.rfc-editor.org/rfc/rfc7591.html
[44]: https://www.rfc-editor.org/rfc/rfc7592.html
[45]: https://www.rfc-editor.org/rfc/rfc9126.html
[46]: https://openid.net/specs/fapi-grant-management.html
[IDA]: https://openid.net/specs/openid-connect-4-identity-assurance-1_0.html
