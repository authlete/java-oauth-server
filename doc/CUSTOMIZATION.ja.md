カスタマイズ
============

このドキュメントでは、この認可サーバーの実装をカスタマイズする方法を説明します。


概要
----

この認可サーバーの実装では、[Authlete][1] をバックエンドとして使用しています。
これは、(1) [OAuth 2.0][2] と [OpenID Connect][3] の実装の中心となる部分が
java-oauth-server のソースツリー内ではなくクラウド上の Authleteサーバー内にあること、そして
(2) アクセストークンなどの認可データ、認可サーバー自体の設定やクライアントアプリケーションの設定が、
ローカルデータベース内ではなくクラウド上のデータベースに保存されるということ、
を意味します。 そのため、非常に単純化して言うと、次の図が示すように、
この実装はクライアントアプリケーションと Authlete サーバーの間の仲介役でしかありません。

```
+--------+          +-------------------+          +----------+
|        |          |                   |          |          |
| Client | <------> | java-oauth-server | <------> | Authlete |
|        |          |                   |          |          |
+--------+          +-------------------+          +----------+
```

とはいえ、Authlete は**認可**に特化しており、エンドユーザーの**認証**に関することは何もしないので、
認証に関わる機能は java-oauth-server のソースツリー内に実装されています。

ですので、少なくとも、エンドユーザーの認証に関する部分についてはカスタマイズが必要です。
一方、認可画面の UI デザインなどの他の部分のカスタマイズは任意です。


全体の構成
----------

Authlete が提供する [Web API][4] を使い、認可サーバーを書くことができます。
[authlete-java-common][5] は、その Web API と直接通信をおこなうライブラリです。
[authlete-java-jaxrs][6] は、[authlete-java-common API][7]
をラッピングするユーティリティークラス群を含むライブラリで、それらのクラス群を使えば、
authlete-java-common API を直接使用するよりもかなり簡単に認可サーバーを書くことができます。
java-oauth-server は、authlete-java-jaxrs のユーティリティークラス群によって構成される
[authlete-java-jaxrs API][8] を使用して書かれています。

名前が示唆するように、authlete-java-jaxrs ライブラリは JAX-RS 2.0 API に依存しています。
JAX-RS は _The Java API for RESTful Web Services_ の略称です。
JAX-RS 2.0 API は [JSR 339][9] で標準化され、Java EE 7 に含まれています。

次の図は、これまでに言及したコンポーネント群の関係を示したものです。

```
+-------------------------------+
|          java-oauth-server    |
+----+--------------------------+
|    |     authlete-java-jaxrs  |
|    +---+----------------------+          +----------+
| JAX-RS | authlete-java-common | <------> | Authlete |
+--------+----------------------+          +----------+
```


認可エンドポイント
------------------

認可エンドポイントの実装は <code>[AuthorizationEndpoint.java][14]</code> 内にあります。
OAuth 2.0 に加えて OpenID Connect もサポートしているにもかかわらず、実装は信じられないほど短いです。
このファイルを変更する必要はほとんどないでしょう。

実装では、<code>[AuthorizationRequestHandler][15]</code>
クラスを使い、認可リクエストを処理する作業をそのクラスの `handle()` メソッドに委譲しています。
クラスの詳細については [authlete-java-jaxrs][6] ライブラリの README ファイルに書かれています。
ここで重要なのは、このクラスのコンストラクタが <code>[AuthorizationRequestHandlerSpi][16]</code>
インターフェースの実装を必要とし、その実装はあなたが提供しなければならないという点です。
別の言い方をすると、`AuthorizationRequestHandlerSpi`
インターフェースのメソッド群がカスタマイズポイントです。

当該インターフェースには、次のようなメソッド群が定義されています。
これらのメソッド群の要求事項の詳細については authlete-java-jaxrs API の
[JavaDoc][8] を参照してください。

  1. `boolean isUserAuthenticated()`
  2. `long etUserAuthenticatedAt()`
  3. `String getUserSubject()`
  4. `String getAcr()`
  5. `Response generateAuthorizationPage(AuthorizationResponse)`

これらの中で最も重要なメソッドは <code>[generateAuthorizationPage()][17]</code> です。
このメソッドは認可ページを生成するために呼ばれます。
対照的に、他のメソッド群は、認可リクエストが `prompt=none`
という特別なリクエストパラメーターを含んでいる場合しか呼ばれないので、それほど重要ではありません。
もしも `prompt=none` をサポートする気がないのであれば、それらのメソッド群の実装は空でかまいません。
`prompt=none` の詳細については [OpenID Connect Core 1.0][11] の
[3.1.2.1. Authorization Request][10] に記述されています。

java-oauth-server における `AuthorizationRequestHandlerSpi` インターフェースの実装は
<code>[AuthorizationRequestHandlerSpiImpl.java][18]</code> です。 ファイル内の
`AuthorizationRequestHandlerSpiImpl` という実装クラスは、`AuthorizationRequestHandlerSpi`
インターフェースの空実装である <code>[AuthorizationRequestHandlerSpiAdapter][19]</code>
クラスを拡張し、`generateAuthorizationPage()` メソッドのみをオーバーライドしています。
下記のコードは、実装のおおまかな構造を示してます。

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


認可ページ
----------

既に述べたとおり、`AuthorizationRequestHandlerSpi` インターフェースの
`generateAuthorizationPage()` メソッドは認可ページを生成するために呼ばれます。
java-oauth-server の現在の実装では、(Authlete の `/api/auth/authorization` API
からの応答を表す <code>[AuthorizationResponse][20]</code> クラスのインスタンスである)
引数からデータを取り出し、そのデータを <code>[authorization.jsp][21]</code>
という HTML テンプレートに埋め込みます。 これをおこなうため、実装では `Viewable`
というクラスを使用しています。 このクラスは [Jersey][12] (JAX-RS のレファレンス実装)
に含まれていますが、JAX-RS 2.0 API の一部ではありません。

認可ページをカスタマイズしたい場合は、`generateAuthorizationPage()`
メソッドと認可ページのテンプレート (`authorization.jsp`)
のどちらか、もしくは両方を変更してください。 `AuthorizationResponse`
クラスの詳細については authlete-java-common ライブラリの [JavaDoc][7]
を参照してください。


#### 国際化

認可ページの国際化に際して、認可リクエストに含まれる `ui_locales`
パラメーターを考慮に入れてもよいでしょう。 これは [OpenID Connect Core 1.0][11]
で新たに定義されたリクエストパラメーターです。
下記は、このパラメータに関する説明を仕様から抜粋したものです。

> OPTIONAL. End-User's preferred languages and scripts for the user interface,
> represented as a space-separated list of BCP47 [RFC5646] language tag values,
> ordered by preference. For instance, the value "fr-CA fr en" represents a
> preference for French as spoken in Canada, then French (without a region
> designation), followed by English (without a region designation). An error
> SHOULD NOT result if some or all of the requested locales are not supported
> by the OpenID Provider.

`AuthorizationResponse` インスタンスの `getUiLocales()` メソッドを呼ぶことで、`ui_locales`
リクエストパラメーターの値を `String` の配列として取得することができます。
ただし、`getUiLocales()` メソッドはサポートされている UI ロケールしか返さないので、
管理コーンソール ([Service Owner Console][13]) を使って明示的に UI
ロケールを指定する必要があることに注意してください。 別の言い方をすると、`ui_locales`
リクエストパラメーターがどのような値であろうとも、`getUiLocales()`
が返す配列にはサポートしている UI ロケールしか含まれていないことが保証されています。

`ui_locales` パラメーターを尊重するか否かはあなたの自由です。
もちろん、認可ページの国際化は好きな方法でおこなうことができます。


### 表示モード

認可リクエストには認可ページの表示方法を指定するための `display`
パラメーターが含まれることがあります。 これは [OpenID Connect Core 1.0][11]
で定義された新しいパラメーターです。
このリクエストパラメーターが取りうる定義済みの値は次のとおりです。
表中の説明は仕様からの抜粋です。

|   値  |  説明  |
|:------|:-------|
| page  | The Authorization Server SHOULD display the authentication and consent UI consistent with a full User Agent page view. If the display parameter is not specified, this is the default display mode. |
| popup | The Authorization Server SHOULD display the authentication and consent UI consistent with a popup User Agent window. The popup User Agent window should be of an appropriate size for a login-focused dialog and should not obscure the entire window that it is popping up over. |
| touch | The Authorization Server SHOULD display the authentication and consent UI consistent with a device that leverages a touch interface. |
| wap   | The Authorization Server SHOULD display the authentication and consent UI consistent with a "feature phone" type display. |

`AuthorizationResponse` インスタンスの `getDisplay()` メソッドで、`display`
リクエストパラメーターの値を列挙型 <code>[Display][22]</code>
のインスタンスとして取得することができます。
デフォルトでは、管理コンソール ([Service Owner Console][13])
では全ての表示タイプがチェックされており、サポートしていることを示していますが、
チェックをはずすことでサポートしないと宣言することもできます。
サポートしていない値が `display` リクエストパラメーターに指定された場合、
その認可リクエストを出したクライアントアプリケーションには `invalid_request`
エラーが返されることになります。

TBW


トークンエンドポイント
----------------------

トークンエンドポイントの実装は <code>[TokenEndpoint.java][23]</code> 内にあります。
実装は信じられないほど短く、ファイルの内容を変更する必要はほとんどないでしょう。

実装では、<code>[TokenRequestHandler][24]</code>
クラスを使い、トークンリクエストを処理する作業をそのクラスの `handle()` メソッドに委譲しています。
クラスの詳細については [authlete-java-jaxrs][6] ライブラリの README ファイルに書かれています。
ここで重要なのは、このクラスのコンストラクタが <code>[TokenRequestHandlerSpi][25]</code>
インターフェースの実装を必要とし、その実装はあなたが提供しなければならないという点です。
別の言い方をすると、`TokenRequestHandlerSpi` インターフェースのメソッド群がカスタマイズポイントです。

当該インターフェースの現在の定義には、`authenticateUser` というメソッドが一つだけ含まれています。
このメソッドは、エンドユーザーを認証するのに使用されます。
しかし、このメソッドが呼ばれるのはトークンリクエストの認可タイプが
[Resource Owner Password Credentials][26] の場合のみです。
そのため、この認可タイプをサポートする気が無いのであれば、メソッドの実装は空でかまいません。

java-oauth-server における `TokenRequestHandlerSpi` インターフェースの実装は
<code>[TokenRequestHandlerSpiImpl.java][27]</code> です。 ファイル内の
`TokenRequestHandlerSpiImpl` という実装クラスは、`TokenRequestHandlerSpi`
インターフェースの空実装である <code>[TokenRequestHandlerSpiAdapter][28]</code>
クラスを拡張し、`authenticateUser()` メソッドをオーバーライドしています。
下記のコードは、実装のおおまかな構造を示してます。

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

TBW


その他の情報
------------

- [Authlete][1] - Authlete ホームページ
- [authlete-java-common][5] - Java 用 Authlete 共通ライブラリ
- [authlete-java-common API][7] - Java 用 Authlete 共通ライブラリの JavaDoc
- [authlete-java-jaxrs][6] - JAX-RS (Java) 用 Authlete ライブラリ
- [authlete-java-jaxrs API][8] - JAX-RS (Java) 用 Authlete ライブラリの JavaDoc


サポート
--------

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
10]: http://openid.net/specs/openid-connect-core-1_0.html#AuthRequest
[11]: http://openid.net/specs/openid-connect-core-1_0.html
[12]: https://jersey.java.net/
[13]: https://www.authlete.com/documents/so_console/
[14]: ../src/main/java/com/authlete/jaxrs/server/api/AuthorizationEndpoint.java
[15]: http://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/AuthorizationRequestHandler.html
[16]: http://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/spi/AuthorizationRequestHandlerSpi.html
[17]: http://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/spi/AuthorizationRequestHandlerSpi.html#generateAuthorizationPage-com.authlete.common.dto.AuthorizationResponse-
[18]: ../src/main/java/com/authlete/jaxrs/server/api/AuthorizationRequestHandlerSpiImpl.java
[19]: http://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/spi/AuthorizationRequestHandlerSpiAdapter.html
[20]: http://authlete.github.io/authlete-java-common/com/authlete/common/dto/AuthorizationResponse.html
[21]: ../src/main/webapp/WEB-INF/template/authorization.jsp
[22]: http://authlete.github.io/authlete-java-common/com/authlete/common/types/Display.html
[23]: ../src/main/java/com/authlete/jaxrs/server/api/TokenEndpoint.java
[24]: http://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/TokenRequestHandler.html
[25]: http://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/spi/TokenRequestHandlerSpi.html
[26]: https://tools.ietf.org/html/rfc6749#section-4.3
[27]: ../src/main/java/com/authlete/jaxrs/server/api/TokenRequestHandlerSpiImpl.java
[28]: http://authlete.github.io/authlete-java-jaxrs/com/authlete/jaxrs/spi/TokenRequestHandlerSpiAdapter.html
