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
+--------------+          +-------------------+          +----------+
|              |          |                   |          |          |
| クライアント | <------> | java-oauth-server | <------> | Authlete |
|              |          |                   |          |          |
+--------------+          +-------------------+          +----------+
```

とはいえ、Authlete は**認可**に特化しており、エンドユーザーの**認証**に関することは何もしないので、
その部分に関する機能は java-oauth-server のソースツリー内に実装されています。

ですので、少なくとも、エンドユーザーの認証に関する部分についてはカスタマイズが必要です。
一方、認可画面の UI デザインなどの他の部分のカスタマイズは任意です。


全体の構成
----------

Authlete が提供する [Web API][4] を使い、認可サーバーを書くことができます。
[authlete-java-common][5] は、その Web API と直接通信をおこなうライブラリです。
[authlete-java-jaxrs][6] は、[authlete-java-common API][7]
をラッピングするユーティリティークラス群を含むライブラリで、それらのクラス群を使えば、
authlete-java-common API を直接使用するよりもかなり簡単に認可サーバーを書くことができます。
java-oauth-server は、それらのユーティリティークラス群によって構成される
[authlete-java-jaxrs API][8] を使用して書かれています。

名前が示唆するように、authlete-java-jaxrs ライブラリは JAX-RS 2.0 API に依存しています。
JAX-RS は _The Java API for RESTful Web Services_ の略称です。
JAX-RS 2.0 API は [JSR 339][9] で標準化され、Java EE 7 に含まれています。

次の図は、ここまでで言及したコンポーネント群の関係を示したものです。

```
+-------------------------------+
|          java-oauth-server    |
+----+--------------------------+
|    |     authlete-java-jaxrs  |
|    +---+----------------------+          +----------+
| JAX-RS | authlete-java-common | <------> | Authlete |
+--------+----------------------+          +----------+
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
