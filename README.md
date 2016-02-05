Authorization Server Implementation in Java
===========================================

Overview
--------

This is an authorization server implementation in Java which supports
[OAuth 2.0][1] and [OpenID Connect][2].

This implementation is written using JAX-RS 2.0 API and [authlete-java-jaxrs][3]
library. JAX-RS is _The Java API for RESTful Web Services_. JAX-RS 2.0 API has
been standardized by [JSR 339][4] and it is included in Java EE 7. On the
other hand, authlete-java-jaxrs library is an open source library which provides
utility classes for developers to implement an authorization server.
authlete-java-jaxrs in turn uses [authlete-java-common][5] library which is
another open source library to communicate with [Authlete Web APIs][6].

This implementation is _DB-less_. What this means is that you don't have to
have a database server that stores authorization data (e.g. access tokens),
settings of the authorization server itself and settings of client applications.
This is achieved by using [Authlete][7] as a backend service.


License
-------

  Apache License, Version 2.0


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

3. Start the authorization server on [http://localhost:8080][38].

        $ mvn jetty:run &


Endpoints
---------

This implementation exposes endpoints as listed in the table below.

| Endpoint               | Path                                |
|:-----------------------|:------------------------------------|
| Authorization Endpoint | `/api/authorization`                |
| Token Endpoint         | `/api/token`                        |
| JWK Set Endpoint       | `/api/jwks`                         |
| Configuration Endpoint | `/.well-known/openid-configuration` |
| Revocation Endpoint    | `/api/revocation`                   |

The authorization endpoint and the token point accept parameters described
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

Of course, these login credentials are dummy data, so you need to replace
the user database implementation with your own.


Customization
-------------

How to customize this implementation is described in [CUSTOMIZATION.md][39].
Basically, you need to do programming for _end-user authentication_ because
Authlete does not manage end-user accounts. This is by design. The
architecture of Authlete carefully seperates authorization from authentication
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
- [RFC 7636][31] - Proof Key for Code Exchange by OAuth Public Clients
- [RFC 7662][32] - OAuth 2.0 Token Introspection
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


Support
-------

[Authlete, Inc.](https://www.authlete.com/)<br/>
support@authlete.com


[1]: http://tools.ietf.org/html/rfc6749
[2]: http://openid.net/connect/
[3]: https://github.com/authlete/authlete-java-jaxrs
[4]: https://jcp.org/en/jsr/detail?id=339
[5]: https://github.com/authlete/authlete-java-common
[6]: https://www.authlete.com/documents/apis
[7]: https://www.authlete.com/
[8]: https://www.authlete.com/documents/overview
[9]: https://so.authlete.com/accounts/signup
[10]: https://www.authlete.com/documents/getting_started
[11]: http://tools.ietf.org/html/rfc6749#section-3.1
[12]: http://tools.ietf.org/html/rfc6749#section-3.2
[13]: http://openid.net/specs/openid-connect-core-1_0.html
[14]: http://tools.ietf.org/html/rfc7636
[15]: https://www.authlete.com/documents/article/pkce
[16]: http://tools.ietf.org/html/rfc6749#section-4.2
[17]: https://www.authlete.com/documents/cd_console
[18]: https://jersey.java.net/
[19]: http://tools.ietf.org/html/rfc6750
[20]: http://tools.ietf.org/html/rfc6819
[21]: http://tools.ietf.org/html/rfc7009
[22]: http://tools.ietf.org/html/rfc7033
[23]: http://tools.ietf.org/html/rfc7515
[24]: http://tools.ietf.org/html/rfc7516
[25]: http://tools.ietf.org/html/rfc7517
[26]: http://tools.ietf.org/html/rfc7518
[27]: http://tools.ietf.org/html/rfc7519
[28]: http://tools.ietf.org/html/rfc7521
[29]: http://tools.ietf.org/html/rfc7522
[30]: http://tools.ietf.org/html/rfc7523
[31]: http://tools.ietf.org/html/rfc7636
[32]: http://tools.ietf.org/html/rfc7662
[33]: http://openid.net/specs/oauth-v2-multiple-response-types-1_0.html
[34]: http://openid.net/specs/oauth-v2-form-post-response-mode-1_0.html
[35]: http://openid.net/specs/openid-connect-discovery-1_0.html
[36]: http://openid.net/specs/openid-connect-registration-1_0.html
[37]: http://openid.net/specs/openid-connect-session-1_0.html
[38]: http://localhost:8080
[39]: doc/CUSTOMIZATION.md
