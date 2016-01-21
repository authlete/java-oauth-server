Customization
=============

This document describes how to customize this authorization server
implementation.


Overview
--------

This authorization server implementation uses [Authlete][1] as its backend.
What this means are (1) that the core part of the implementation of [OAuth
2.0][2] and [OpenID Connect][3] is not in the source tree of java-oauth-server
but in the Authlete server on cloud, and (2) that authorization data such as
access tokens, settings of the authorization server itself and settings of
client applications are not stored in any local database but in the database
on cloud. Therefore, to put it very simply, this implementation is just an
intermediary between client applications and Authlete server as illustrated
below.

```
+--------+          +-------------------+          +----------+
|        |          |                   |          |          |
| Client | <------> | java-oauth-server | <------> | Authlete |
|        |          |                   |          |          |
+--------+          +-------------------+          +----------+
```

However, because Authlete focuses on **authorization** and does NOT do
anything about end-user **authentication**, functions related to
authentication are implemented in the source tree of java-oauth-server.

Therefore, at least, you must customize parts related to end-user authentication.
On the other hand, customization of other parts such as UI design of the
authorization page is optional.


Overall Structure
-----------------

Authlete provides [Web APIs][4] that can be used to write an authorization
server. [authlete-java-common][5] is a library which directly communicates
with the Web APIs, and [authlete-java-jaxrs][6] is a library which provides
utility classes wrapping the [authlete-java-common API][7] to make it much
easier for developers to implement an authorization server than using
authlete-java-common API directly. java-oauth-server is written using
[authlete-java-jaxrs API][8] exposed by the utility classes.

As its name implies, authlete-java-jaxrs library depends on JAX-RS 2.0 API.
JAX-RS means _The Java API for RESTful Web Services_. JAX-RS 2.0 API has
been standardized by [JSR 339][9] and it is included in Java EE 7.

The figure below illustrates the relationship among the components mentioned
so far.

```
+-------------------------------+
|          java-oauth-server    |
+----+--------------------------+
|    |     authlete-java-jaxrs  |
|    +---+----------------------+          +----------+
| JAX-RS | authlete-java-common | <------> | Authlete |
+--------+----------------------+          +----------+
```


Authorization Endpoint
----------------------

The implementation of the authorization endpoint is in `AuthorizationEndpoint.java`.
The code is incredibly short although it supports OpenID Connect in addition to
OAuth 2.0. There is little need to change this file.

The implementation uses `AuthorizationRequestHandler` class and delegates the
task to handle an authorization request to `handle()` method of the class.
Details about the class is written in the README file of authlete-java-jaxrs
library. What is important here is that the constructor of the class requires
an implementation of `AuthorizationRequestHandlerSpi` interface and that the
implementation must be provided by you. In other words, the methods in
`AuthorizationRequestHandlerSpi` interface are customization points.


TBW


See Also
--------

- [Authlete][1] - Authlete Home Page
- [authlete-java-common][5] - Authlete Common Library for Java
- [authlete-java-common API][7] - JavaDoc of Authlete Common Library for Java
- [authlete-java-jaxrs][6] - Authlete Library for JAX-RS (Java)
- [authlete-java-jaxrs API][8] - JavaDoc of Authlete Library for JAX-RS (Java)


Support
-------

[Authlete, Inc.][1]<br/>
support@authlete.com


[1]: https://www.authlete.com/
[2]: http://tools.ietf.org/html/rfc6749
[3]: http://openid.net/connect/
[4]: https://www.authlete.com/documents/apis
[5]: https://github.com/authlete/authlete-java-common
[6]: https://github.com/authlete/authlete-java-jaxrs
[7]: http://authlete.github.io/authlete-java-common/
[8]: http://authlete.github.io/authlete-java-jaxrs/
[9]: https://jcp.org/en/jsr/detail?id=339
