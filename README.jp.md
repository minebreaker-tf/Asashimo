# アサシモ

Kotlin向けJDBCラッパー

[English](README.md)

[![CircleCI](https://circleci.com/bb/minebreaker_tf/asashimo.svg?style=svg&circle-token=c3b0779aa16a3bcdb21e4e72ab8575f916ca2b5a)](https://circleci.com/bb/minebreaker_tf/asashimo)
![](https://img.shields.io/badge/maturity-experimental-green.svg)
![](https://img.shields.io/badge/license-MIT-green.svg)

* Kotlin向け
* 流暢なAPI
* DSLよりも通常のSQL
* 名前付きパラメーター
* リスト展開
* Google Guavaの`ListenableFuture`を使った非同期API

```kotlin
fun sample(dataSource: javax.sql.DataSource) {
    val connector: Connector = Connectors.newInstance(dataSource)
    val message = connector.fetch("select 'hello, world' from dual", String::class)
    assertThat(message).isEqualTo("hello, world")
}
```

[https://bitbucket.org/minebreaker_tf/asashimo](https://bitbucket.org/minebreaker_tf/asashimo)


## Gradle

```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/minebreaker/test"
    }
}
dependencies {
    compile 'rip.deadcode:asashimo:0.2.5'
}
```


## 使い方

*`rip.deadcode.asashimo.ConnectorsTest`を参照してください*

#### select

```kotlin
data class User(val id: Int, val name: String)
val user: User = connector.fetch("select id, name from user", User::class)
val users: List<User> = connector.fetchAll("select id, name from user", User::class)
```

#### execute

```kotlin
val updatedRows = connector.exec("update user set name = 'John'")
```

#### 複数の操作

デフォルトでは、`Connector`は呼ばれるたびに新しい`java.sql.Connection`を作成します。
これを避けるために、`use()`か`transactional()`を使ってください。

```kotlin
val user: User = connector!!.use {
    exec("create table user(id int, name varchar)")
    exec("insert into user values(1, 'John')")
    fetch("select * from user", User::class)
}
```

#### Transaction

`Connector.transactional { ... }`は同じトランザクション内で実行されます。

```kotlin
try {
    connector.transactional {
        exec("insert into user values(1, 'John')")
        throw RuntimeException()
    }
} catch (e: AsashimoException) {
    val count = connector.fetch("select count(*) from user", Int::class)
    assertThat(count).isEqualTo(0)
}
```

#### 名前付きパラメーター

`with()`メソッドから、名前付きパラメーターを使用できます。

##### DSL風シンタックス

```kotlin
val user = connector.with {
    it["id"] = 1
    it["name"] = "John"
}.fetch("select * from user where name = :name", User::class)
```

##### Map

```kotlin
val user = connector
        .with(mapOf("id" to 1, "name" to "John"))
        .fetch("select * from user where name = :name", User::class)
```

その代わりに、位置パラメーターは使えません。


#### コレクション展開

```kotlin
val users = connector.with {
    it["ids"] = listOf(1, 2, 3)
}.fetchAll("select * from user where id in (:ids)", User::class)
```

括弧は必須です。


#### 非同期API

アサシモは[Google Guava](https://github.com/google/guava)の
`ListenableFuture`を使って非同期APIを提供しています。

[ListenableFutureExplained](https://github.com/google/guava/wiki/ListenableFutureExplained)

```kotlin
val userFuture: ListenableFuture<User> = connector
        .with(mapOf("id" to 1))
        .useAsync(MoreExecutors.newDirectExecutorService()) {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(1, 'John')")
            fetch("select * from user where id = :id", User::class)
        }
```

引数の`ListeningExecutorService`が省略された場合、
[DirectExecutorService](http://google.github.io/guava/releases/snapshot-jre/api/docs/com/google/common/util/concurrent/MoreExecutors.html#newDirectExecutorService--)
が使用されます。すなわち、`async...`呼び出しは、デフォルトでは非同期に行われません。
`Connectors`に、アプリケーション固有の
[ExecutorService](https://docs.oracle.com/javase/jp/8/docs/api/java/util/concurrent/ExecutorService.html)
を設定する必要があります。


#### マップAPI

```kotlin
fun mapApi() {
    connector = Connectors.newInstance(dataSource = dataSource, defaultResultMapper = MapResultMapper)
    val result: Map<String, String> = connector.fetch(
            "select 'hello, world' as message from dual", Map::class)
    assertThat(result).containsExactly("message", "hello, world")
}
```


#### JPAアノテーション

アサシモはエンティティー簡単にフェッチ/永続化するため、JPAのアノテーションを利用することができます。

これらのAPIはJPAとの互換性を持たせることを意図しているものではないことに注意してください。

```kotlin
@Table(name = "user")
data class User(
        @Id
        @Column(name = "user_id")
        val id: Int = 0,
        val name: String = ""
)

fun find() {
    val id = 123
    val result = connector.find(id, User::class)

    assertThat(result).isInstanceOf(User::class.java)
}

fun persist() {
    val user = JpaUser(123, "John")
    connector.persist(user)
    val result = connector.fetch("select * from user", User::class)

    assertThat(result.id).isEqualTo(123)
    assertThat(result.name).isEqualTo("John")
}
```


## IntelliJ Language Injection

File -> Settings -> Editor -> Language Injections

Add ("+" icon) -> Generic Kotlin

ID: SQL

```
+ kotlinParameter().ofFunction(0, kotlinFunction().withName("fetch", "fetchAll", "exec").definedInClass("rip.deadcode.asashimo.Connector"))
+ kotlinParameter().ofFunction(0, kotlinFunction().withName("fetch", "fetchAll", "exec").definedInClass("rip.deadcode.asashimo.OfUse"))
+ kotlinParameter().ofFunction(0, kotlinFunction().withName("fetch", "fetchAll", "exec").definedInClass("rip.deadcode.asashimo.OfWith"))
```


## ライセンス

MIT
