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

The implementation of the authorization endpoint is in
<code>[AuthorizationEndpoint.java][14]</code>. The code is incredibly short
although it supports OpenID Connect in addition to OAuth 2.0. There is little
need to change this file.

The implementation uses <code>[AuthorizationRequestHandler][15]</code> class
and delegates the task to handle an authorization request to `handle()` method
of the class. Details about the class is written in the README file of
[authlete-java-jaxrs][6] library. What is important here is that the
constructor of the class requires an implementation of
<code>[AuthorizationRequestHandlerSpi][16]</code> interface and that the
implementation must be provided by you. In other words, the methods in
`AuthorizationRequestHandlerSpi` interface are customization points.

The interface has the methods listed below. See the [JavaDoc][8] of
authlete-java-jaxrs API for details about the requirements of these methods.

  1. `boolean isUserAuthenticated()`
  2. `long getUserAuthenticatedAt()`
  3. `String getUserSubject()`
  4. `String getAcr()`
  5. `Response generateAuthorizationPage(AuthorizationResponse)`

The most important method among the above is
<code>[generateAuthorizationPage()][17]</code>. The method is called to
generate an authorization page. In contrast, the other methods are not so
important because they are called only when an authorization request comes
with a special request parameter `prompt=none`. If you have no mind to
support `prompt=none`, you can leave your implementations of the methods
empty. Details about `prompt=none` is written in
"[3.1.2.1. Authorization Request][10]" of [OpenID Connect Core 1.0][11].

The implementation of `AuthorizationRequestHandlerSpi` interface in
java-oauth-server is written in
<code>[AuthorizationRequestHandlerSpiImpl.java][18]</code>. The implementation
class in the file, `AuthorizationRequestHandlerSpiImpl`, extends
<code>[AuthorizationRequestHandlerSpiAdapter][19]</code> class which is an
empty implementation of `AuthorizationRequestHandlerSpi` interface, and
overrides `generateAuthorizationPage()` method only. The code snippet below
shows the rough structure of the implementation.

```java
class AuthorizationRequestHandlerSpiImpl extends AuthorizationRequestHandlerSpiAdapter
{
    ......

    @Override
    public Response generateAuthorizationPage(AuthorizationResponse info)
    {
        ......
    }
}
```


Authorization Page
------------------

As mentioned, `generateAuthorizationPage()` in `AuthorizationRequestHandlerSpi`
interface is a method to generate an authorization page. The current
implementation of the method in java-oauth-server retrieves data from the
argument (an intance of <code>[AuthorizationResponse][20]</code> class which
represents a response from Authlete's `/api/auth/authorization` API) and
embeds them into an HTML template, <code>[authorization.jsp][21]</code>.
To achieve this, the implementation uses `Viewable` class. The class is
included in [Jersey][12] (the reference implementation of JAX-RS), but it
is not a part of JAX-RS 2.0 API.

If you want to customize the authorization page, change the implementation
of `generateAuthorizationPage()` method and/or the template of the
authorization page (`authorization.jsp`). See the [JavaDoc][7] of
authlete-java-common library for details about `AuthorizationResponse`
class.


#### Internationalization

For the internationalization of the authorization page, you may take
`ui_locales` parameter into consideration which may be contained in an
authorization request. It is a new request parameter defined in [OpenID
Connect Core 1.0][11]. The following is the description about the parameter
excerpted from the specification.

> OPTIONAL. End-User's preferred languages and scripts for the user interface,
> represented as a space-separated list of BCP47 [RFC5646] language tag values,
> ordered by preference. For instance, the value "fr-CA fr en" represents a
> preference for French as spoken in Canada, then French (without a region
> designation), followed by English (without a region designation). An error
> SHOULD NOT result if some or all of the requested locales are not supported
> by the OpenID Provider.

You can get the value of `ui_locales` request paremeter as a `String` array
by calling `getUiLocales()` method of `AuthorizationResponse` instance. Note
that, however, you have to explicitly specify which UI locales to support
using the management console ([Service Owner Console][13]) because
`getUiLocales()` method returns only supported UI locales. In other words,
it is ensured that the array returned by `getUiLocales()` never contains
unsupported UI locales whatever `ui_locales` request parameter contains.

It is up to you whether to honor `ui_locales` parameter or not. Of course,
you may use any means you like to internationalize the authorization page.


#### Display type

An authorization request may contain `display` request parameter to specify
how to display the authorization page. It is a new request parameter defined
in [OpenID Connect Core 1.0][11]. The predefined values of the request
parameter are as follows. The descriptions in the table are excerpts from
the specification.

| Value | Description |
|:------|:------------|
| page  | The Authorization Server SHOULD display the authentication and consent UI consistent with a full User Agent page view. If the display parameter is not specified, this is the default display mode. |
| popup | The Authorization Server SHOULD display the authentication and consent UI consistent with a popup User Agent window. The popup User Agent window should be of an appropriate size for a login-focused dialog and should not obscure the entire window that it is popping up over. |
| touch | The Authorization Server SHOULD display the authentication and consent UI consistent with a device that leverages a touch interface. |
| wap   | The Authorization Server SHOULD display the authentication and consent UI consistent with a "feature phone" type display. |

You can get the value of `display` request parameter as an instance of
<code>[Display][22]</code> enum by calling `getDisplay()` method of
`AuthorizationResponse` instance. By default, all the display types are
checked as supported in the management console ([Service Owner Console][13]),
but you can uncheck them to declare some values are not supported. If an
unsupported value is specified as the value of `display` request parameter,
it will result in returning an `invalid_request` error to the client
application that made the authorization request.

TBW


Authorization Decision Endpoint
-------------------------------

In an authorization page, an end-user decides either to grant permissions to
the client application which made the authorization request or to deny the
authorization request. An authorization server must be able to receive the
decision and return a proper response to the client application according to
the decision.

The current implementation of java-oauth-server receives the end-user's
decision at `/api/authorization/decision`. In this document, we call the
endpoint _authorization decision endpoint_. In java-oauth-server, the
implementation of the authorization decision endpoint is in
<code>[AuthorizationDecisionEndpoint.java][29]</code>.

The implementation uses <code>[AuthorizationDecisionHandler][30]</code> class
and delegates the task to handle an end-user's decision to `handle()` method
of the class. Details about the class is written in the README file of
[authlete-java-jaxrs][6] library. What is important here is that the
constructor of the class requires an implementation of
<code>[AuthorizationDecisionHandlerSpi][31]</code> interface and that the
implementation must be provided by you. In other words, the methods in
`AuthorizationDecisionHandlerSpi` interface are customization points.

The interface has the methods listed below. See the [JavaDoc][8] of
authlete-java-jaxrs API for details about the requirements of these methods.

  1. `boolean isClientAuthorized()`
  2. `long getUserAuthenticatedAt()`
  3. `String getUserSubject()`
  4. `String getAcr()`
  5. `getUserClaim(String claimName, String languageTag)`

The implementation of `AuthorizationDecisionHandlerSpi` interface in
java-oauth-server is written in
<code>[AuthorizationDecisionHandlerSpiImpl.java][32]</code>. The implementation
class in the file, `AuthorizationDecisionHandlerSpiImpl`, extends
<code>[AuthorizationDecisionHandlerSpiAdapter][33]</code> class which is an
empty implementation of `AuthorizationDecisionHandlerSpi` interface, and
overrides all the methods except `getAcr()`. The code snippet below shows
the rough structure of the implementation.

```java
class AuthorizationDecisionHandlerSpiImpl extends AuthorizationDecisionHandlerSpiAdapter
{
    ......

    @Override
    public boolean isClientAuthorized()
    {
        ......
    }

    @Override
    public long getUserAuthenticatedAt()
    {
        ......
    }

    @Override
    public String getUserSubject()
    {
        ......
    }

    @Override
    public Object getUserClaim(String claimName, String languageTag)
    {
        ......
    }
}
```


#### End-User Authentication

Authlete does not care about how to authenticate an end-user at all.
Instead, Authlete requires the subject of the authenticated end-user.

_Subject_ is a technical term in the area related to identity and it means
a unique identifier. In a typical case, subjects of end-users are values of
the primary key column or another unique column in a user database.

When an end-user grants permissions to a client application, you have
to let Authlete know the subject of the end-user. In the context of
`AuthorizationDecisionHandlerSpi` interface, this can be described as
follows: _"if `isClientAuthorized()` returns `true`, then `getUserSubject()`
must return the subject of the end-user."_

For end-user authentication, java-oauth-server has `UserDao` class and
`UserEntity` class. These two classes compose a dummy user database.
Of course, you have to replace them with your own implementation to
refer to your actual user database.


Token Endpoint
--------------

The implementation of the token endpoint is in <code>[TokenEndpoint.java][23]</code>.
The code is incredibly short and there is little need to change the content of
the file.

The implementation uses <code>[TokenRequestHandler][24]</code> class and delegates
the task to handle a token request to `handle()` method of the class. Details about
the class is written in the README file of [authlete-java-jaxrs][6] library. What
is important here is that the constructor of the class requires an implementation
of <code>[TokenRequestHandlerSpi][25]</code> interface and that the implementation
must be provided by you. In other words, the methods in `TokenRequestHandlerSpi`
interface are customization points.

The current definition of the interface has only one method named `authenticateUser`.
This method is used to authenticate an end-user. However, the method is called
only when the grant type of a token request is [Resource Owner Password
Credentials][26]. Therefore, if you have no mind to support the grant type,
you can leave your implementation of the method empty.

The implementation of `TokenRequestHandlerSpi` interface in java-oauth-server
is written in <code>[TokenRequestHandlerSpiImpl.java][27]</code>. The implmentation
class in the file, `TokenRequestHandlerSpiImpl`, extends
<code>[TokenRequestHandlerSpiAdapter][28]</code> class which is an empty
implementation of `TokenRequestHandlerSpi` interface, and overrides
`authenticateUser()` method. The code snippet below shows the rough structure of
the implementation.

```java
class TokenRequestHandlerSpiImpl extends TokenRequestHandlerSpiAdapter
{
    ......

    @Override
    public String authenticateUser(String username, String password)
    {
        ......
    }
}
```


Introspection Endpoint
----------------------

The implementation of the introspection endpoint is in
<code>[IntrospectionEndpoint.java][34]</code>.

[RFC 7662][35] (OAuth 2.0 Token Introspection) requires that the endpoint
be protected in some way or other. The implementation of the protection in
<code>IntrospectionEndpoint.java</code> is for demonstration purpose only,
and it is not suitable for commercial use. Therefore, modify the code
accordingly.


See Also
--------

- [Authlete][1] - Authlete Home Page
- [authlete-java-common][5] - Authlete Common Library for Java
- [authlete-java-common API][7] - JavaDoc of Authlete Common Library for Java
- [authlete-java-jaxrs][6] - Authlete Library for JAX-RS (Java)
- [authlete-java-jaxrs API][8] - JavaDoc of Authlete Library for JAX-RS (Java)


Contact
-------

| Purpose   | Email Address        |
|:----------|:---------------------|
| General   | info@authlete.com    |
| Sales     | sales@authlete.com   |
| PR        | pr@authlete.com      |
| Technical | support@authlete.com |


[1]: https://www.authlete.com/
[2]: https://tools.ietf.org/html/rfc6749
[3]: https://openid.net/connect/
[4]: https://docs.authlete.com/
[5]: https://github.com/authlete/authlete-java-common
[6]: https://github.com/authlete/authlete-java-jaxrs
[7]: https://authlete.github.io/authlete-java-common/
[8]: https://authlete.github.io/authlete-java-jaxrs/
[9]: https://jcp.org/en/jsr/detail?id=339
[10]: https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest
[11]: https://openid.net/specs/openid-connect-core-1_0.html
[12]: https://jersey.java.net/
[13]: https://www.authlete.com/documents/so_console/
[14]: ../src/main/java/com/authlete/jaxrs/server/api/AuthorizationEndpoint.java
[15]: https://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/AuthorizationRequestHandler.html
[16]: https://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/spi/AuthorizationRequestHandlerSpi.html
[17]: https://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/spi/AuthorizationRequestHandlerSpi.html#generateAuthorizationPage-com.authlete.common.dto.AuthorizationResponse-
[18]: ../src/main/java/com/authlete/jaxrs/server/api/AuthorizationRequestHandlerSpiImpl.java
[19]: https://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/spi/AuthorizationRequestHandlerSpiAdapter.html
[20]: https://authlete.github.io/authlete-java-common/com/authlete/common/dto/AuthorizationResponse.html
[21]: ../src/main/webapp/WEB-INF/template/authorization.jsp
[22]: https://authlete.github.io/authlete-java-common/com/authlete/common/types/Display.html
[23]: ../src/main/java/com/authlete/jaxrs/server/api/TokenEndpoint.java
[24]: https://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/TokenRequestHandler.html
[25]: https://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/spi/TokenRequestHandlerSpi.html
[26]: https://tools.ietf.org/html/rfc6749#section-4.3
[27]: ../src/main/java/com/authlete/jaxrs/server/api/TokenRequestHandlerSpiImpl.java
[28]: https://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/spi/TokenRequestHandlerSpiAdapter.html
[29]: ../src/main/java/com/authlete/jaxrs/server/api/AuthorizationDecisionEndpoint.java
[30]: https://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/AuthorizationDecisionHandler.html
[31]: https://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/spi/AuthorizationDecisionHandlerSpi.html
[32]: ../src/main/java/com/authlete/jaxrs/server/api/AuthorizationDecisionHandlerSpiImpl.java
[33]: https://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/spi/AuthorizationDecisionHandlerSpiAdapter.html
[34]: ../src/main/java/com/authlete/jaxrs/server/api/IntrospectionEndpoint.java
[35]: https://tools.ietf.org/html/rfc7662
