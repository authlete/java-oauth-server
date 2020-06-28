認可サーバー実装 (Java)
=======================

概要
----

[OAuth 2.0][1] と [OpenID Connect][2] をサポートする認可サーバーの Java による実装です。

この実装は JAX-RS 2.0 API と [authlete-java-jaxrs][3] ライブラリを用いて書かれています。
JAX-RS は _The Java API for RESTful Web Services_ です。 JAX-RS 2.0 API は
[JSR 339][4] で標準化され、Java EE 7 に含まれています。 一方、authlete-java-jaxrs
は、認可サーバーとリソースサーバーを実装するためのユーティリティークラス群を提供するオープンソースライブラリです。
authlete-java-jaxrs は [authlete-java-common][5] ライブラリを使用しており、こちらは
[Authlete Web API][6] とやりとりするためのオープンソースライブラリです。

この実装は「DB レス」です。 これの意味するところは、認可データ (アクセストークン等)
や認可サーバー自体の設定、クライアントアプリケーション群の設定を保持するためのデータベースを用意する必要がないということです。
これは、[Authlete][7] をバックエンドサービスとして利用することにより実現しています。

この認可サーバーにより発行されたアクセストークンは、Authlete
をバックエンドサービスとして利用しているリソースサーバーに対して使うことができます。
[java-resource-server][40] はそのようなリソースサーバーの実装です。
[OpenID Connect Core 1.0][13] で定義されている[ユーザー情報エンドポイント][41]をサポートし、
保護リソースエンドポイントの実装例も含んでいます。


ライセンス
----------

  Apache License, Version 2.0


ソースコード
------------

  <code>https://github.com/authlete/java-oauth-server</code>


Authlete について
-----------------

[Authlete][7] (オースリート) は、OAuth 2.0 & OpenID Connect
の実装をクラウドで提供するサービスです ([overview][8])。 Authlete
が提供するデフォルト実装を使うことにより、もしくはこの実装 (java-oauth-server)
でおこなっているように [Authlete Web API][6]
を用いて認可サーバーを自分で実装することにより、OAuth 2.0 と OpenID Connect
の機能を簡単に実現できます。

この認可サーバーの実装を使うには、Authlete から API
クレデンシャルズを取得し、`authlete.properties` に設定する必要があります。
API クレデンシャルズを取得する手順はとても簡単です。
単にアカウントを登録するだけで済みます ([サインアップ][9])。
詳細は [Getting Started][10] を参照してください。


実行方法
--------

1. この認可サーバーの実装をダウンロードします。

        $ git clone https://github.com/authlete/java-oauth-server.git
        $ cd java-oauth-server

2. 設定ファイルを編集して API クレデンシャルズをセットします。

        $ vi authlete.properties

3. [maven][42] がインストールされていること、 `JAVA_HOME` が適切に設定されていることを確認します。

4. [http://localhost:8080][38] で認可サーバーを起動します。

        $ mvn jetty:run &

#### Docker を利用する

Docker を利用する場合は, ステップ 2 の後に以下のコマンドを実行してください.

    $ docker-compose up

#### 設定ファイル

`java-oauth-server` は `authlete.properties` を設定ファイルとして参照します。
他のファイルを使用したい場合は、次のようにそのファイルの名前をシステムプロパティー
`authlete.configuration.file` で指定してください。

    $ mvn -Dauthlete.configuration.file=local.authlete.properties jetty:run &

エンドポイント
--------------

この実装は、下表に示すエンドポイントを公開します。

| エンドポイント                     | パス                                |
|:-----------------------------------|:------------------------------------|
| 認可エンドポイント                 | `/api/authorization`                |
| トークンエンドポイント             | `/api/token`                        |
| JWK Set エンドポイント             | `/api/jwks`                         |
| 設定エンドポイント                 | `/.well-known/openid-configuration` |
| 取り消しエンドポイント             | `/api/revocation`                   |
| イントロスペクションエンドポイント | `/api/introspection`                |
| 動的クライアント登録エンドポイント | `/api/register`                     |

認可エンドポイントとトークンエンドポイントは、[RFC 6749][1]、[OpenID Connect Core 1.0][13]、
[OAuth 2.0 Multiple Response Type Encoding Practices][33]、[RFC 7636][14] ([PKCE][15])、
その他の仕様で説明されているパラメーター群を受け付けます。

JWK Set エンドポイントは、クライアントアプリケーションが (1) この OpenID
プロバイダーによる署名を検証できるようにするため、また (2) この OpenID
へのリクエストを暗号化できるようにするため、JSON Web Key Set ドキュメント
(JWK Set) を公開します。

設定エンドポイントは、この OpenID プロバイダーの設定情報を
[OpenID Connect Discovery 1.0][35] で定義されている JSON フォーマットで公開します。

取り消しエンドポイントはアクセストークンやリフレッシュトークンを取り消すための
Web API です。 その動作は [RFC 7009][21] で定義されています。

イントロスペクションエンドポイントはアクセストークンやリフレッシュトークンの情報を取得するための
Web API です。 その動作は [RFC 7662][32] で定義されています。

動的クライアント登録エンドポイントは、クライアントアプリケーションの登録・更新をおこなうための
Web API です。 その動作は [RFC 7591][43] および [RFC 7592][44] で定義されています。


認可リクエストの例
------------------

次の例は [Implicit フロー][16]を用いて認可エンドポイントからアクセストークンを取得する例です。
`{クライアントID}` となっているところは、あなたのクライアントアプリケーションの実際のクライアント
ID で置き換えてください。 クライアントアプリケーションについては、[Getting Started][10]
および開発者コンソールの[ドキュメント][17]を参照してください。

    http://localhost:8080/api/authorization?client_id={クライアントID}&response_type=token

上記のリクエストにより、認可ページが表示されます。
認可ページでは、ログイン情報の入力と、"Authorize" ボタン (認可ボタン) もしくは "Deny" ボタン
(拒否ボタン) の押下が求められます。 ログイン情報として、下記のいずれかを使用してください。


| ログイン ID | パスワード |
|:-----------:|:----------:|
|     john    |    john    |
|     jane    |    jane    |
|     max     |    max     |

もちろんこれらのログイン情報はダミーデータですので、ユーザーデータベースの実装をあなたの実装で置き換える必要があります。

[OpenID Connect for Identity Assurance 1.0][IA10] をテストするためには `max`
を使用してください。他のユーザーアカウント (`john` と `jane`) 用の verified claims
はダミーデータベース内に存在しません。


カスタマイズ
------------

この実装をカスタマイズする方法については [CUSTOMIZATION.ja.md][39] に記述されています。
Authlete はユーザーアカウントを管理しないので、基本的には「ユーザー認証」に関わる部分についてプログラミングが必要となります。
これは設計によるものです。 ユーザー認証の仕組みを実装済みの既存の Web
サービスにもスムーズに OAuth 2.0 と OpenID Connect の機能を組み込めるようにするため、Authlete
のアーキテクチャーは認証と認可を慎重に分離しています。


実装に関する注意
----------------

この実装では、認可ページを実装するために `Viewable` クラスを使用しています。
このクラスは [Jersey][18] (JAX-RS の参照実装) に含まれているものですが、JAX-RS
2.0 API の一部ではありません。


関連仕様
--------

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
- [OAuth 2.0 Multiple Response Type Encoding Practices][33]
- [OAuth 2.0 Form Post Response Mode][34]
- [OpenID Connect Core 1.0][13]
- [OpenID Connect Discovery 1.0][35]
- [OpenID Connect Dynamic Client Registration 1.0][36]
- [OpenID Connect Session Management 1.0][37]


その他の情報
------------

- [Authlete][7] - Authlete ホームページ
- [authlete-java-common][5] - Java 用 Authlete 共通ライブラリ
- [authlete-java-jaxrs][3] - JAX-RS (Java) 用 Authlete ライブラリ
- [java-resource-server][40] - リソースサーバーの実装


コンタクト
----------

| 目的 | メールアドレス       |
|:-----|:---------------------|
| 一般 | info@authlete.com    |
| 営業 | sales@authlete.com   |
| 広報 | pr@authlete.com      |
| 技術 | support@authlete.com |


[1]: https://tools.ietf.org/html/rfc6749
[2]: https://openid.net/connect/
[3]: https://github.com/authlete/authlete-java-jaxrs
[4]: https://jcp.org/en/jsr/detail?id=339
[5]: https://github.com/authlete/authlete-java-common
[6]: https://docs.authlete.com/
[7]: https://www.authlete.com/
[8]: https://www.authlete.com/ja/developers/overview/
[9]: https://so.authlete.com/accounts/signup
[10]: https://www.authlete.com/ja/developers/getting_started/
[11]: https://tools.ietf.org/html/rfc6749#section-3.1
[12]: https://tools.ietf.org/html/rfc6749#section-3.2
[13]: https://openid.net/specs/openid-connect-core-1_0.html
[14]: https://tools.ietf.org/html/rfc7636
[15]: https://www.authlete.com/ja/developers/pkce/
[16]: https://tools.ietf.org/html/rfc6749#section-4.2
[17]: https://www.authlete.com/ja/developers/cd_console/
[18]: https://jersey.java.net/
[19]: https://tools.ietf.org/html/rfc6750
[20]: https://tools.ietf.org/html/rfc6819
[21]: https://tools.ietf.org/html/rfc7009
[22]: https://tools.ietf.org/html/rfc7033
[23]: https://tools.ietf.org/html/rfc7515
[24]: https://tools.ietf.org/html/rfc7516
[25]: https://tools.ietf.org/html/rfc7517
[26]: https://tools.ietf.org/html/rfc7518
[27]: https://tools.ietf.org/html/rfc7519
[28]: https://tools.ietf.org/html/rfc7521
[29]: https://tools.ietf.org/html/rfc7522
[30]: https://tools.ietf.org/html/rfc7523
[31]: https://tools.ietf.org/html/rfc7636
[32]: https://tools.ietf.org/html/rfc7662
[33]: https://openid.net/specs/oauth-v2-multiple-response-types-1_0.html
[34]: https://openid.net/specs/oauth-v2-form-post-response-mode-1_0.html
[35]: https://openid.net/specs/openid-connect-discovery-1_0.html
[36]: https://openid.net/specs/openid-connect-registration-1_0.html
[37]: https://openid.net/specs/openid-connect-session-1_0.html
[38]: http://localhost:8080
[39]: doc/CUSTOMIZATION.ja.md
[40]: https://github.com/authlete/java-resource-server
[41]: https://openid.net/specs/openid-connect-core-1_0.html#UserInfo
[42]: https://maven.apache.org/
[43]: https://tools.ietf.org/html/rfc7591
[44]: https://tools.ietf.org/html/rfc7592
[IA10]: https://openid.net/specs/openid-connect-4-identity-assurance-1_0.html
