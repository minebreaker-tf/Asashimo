# Asashimo

Thin JDBC wrapper for Kotlin

[日本語](README.jp.md)

[![CircleCI](https://circleci.com/bb/minebreaker_tf/asashimo.svg?style=svg&circle-token=c3b0779aa16a3bcdb21e4e72ab8575f916ca2b5a)](https://circleci.com/bb/minebreaker_tf/asashimo)
![](https://img.shields.io/badge/maturity-experimental-green.svg)
![](https://img.shields.io/badge/license-MIT-green.svg)

* Kotlin-friendly
* Fluent API
* Plain SQL rather than DSL
* Named parameter support
* List spreading
* Async API with Google Guava `ListenableFuture`

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


## How to use

*See `rip.deadcode.asashimo.ConnectorsTest` for more samples*

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

#### Multiple operations

By default, `Connector` creates a new `java.sql.Connection` every time called.
To prevent this behavior, call `use()` or `transactional()`.

```kotlin
val user: User = connector!!.use {
    exec("create table user(id int, name varchar)")
    exec("insert into user values(1, 'John')")
    fetch("select * from user", User::class)
}
```

#### Transaction

`Connector.transactional { ... }` is executed in a same transaction.

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

#### Named parameters

You can use named parameters via `with()` method.

DSL-like syntax

```kotlin
val user = connector.with {
    it["id"] = 1
    it["name"] = "John"
}.fetch("select * from user where name = :name", User::class)
```

Map

```kotlin
val user = connector
        .with(mapOf("id" to 1, "name" to "John"))
        .fetch("select * from user where name = :name", User::class)
```

Instead, you can't use positional parameters.


#### Collection spreading

```kotlin
val users = connector.with {
    it["ids"] = listOf(1, 2, 3)
}.fetchAll("select * from user where id in (:ids)", User::class)
```

Don't forget braces are mandatory.


#### Async API

Asashimo provides async API with `ListenableFuture` of
[Google Guava](https://github.com/google/guava).

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

If `ListeningExecutorService` in the arguments is omitted,
[DirectExecutorService](http://google.github.io/guava/releases/snapshot-jre/api/docs/com/google/common/util/concurrent/MoreExecutors.html#newDirectExecutorService--)
is used. This means `async...` calls are not actually async by default.
You must configure your own
[ExecutorService](https://docs.oracle.com/javase/jp/8/docs/api/java/util/concurrent/ExecutorService.html)
pool via `Connectors`.


#### Map API

```kotlin
fun mapApi() {
    connector = Connectors.newInstance(dataSource = dataSource, defaultResultMapper = MapResultMapper)
    val result: Map<String, String> = connector.fetch(
            "select 'hello, world' as message from dual", Map::class)
    assertThat(result).containsExactly("message", "hello, world")
}
```


#### JPA Annotation

Asashimo can use JPA annotations to easily fetch/persist entities.

Note that these APIs are **NOT** intended to be JPA-compatible.

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

TODO: Find better pattern


## TODOs

* API for Java
* Savepoint
* Batch
* Fetch interface API
* `fetchAll()` and `fetchStream()` with lazy list, using cursor
* Upsert support
* Registry builder
* Understandable error message
* Performance tests and cache things if necessary
* Documentation
* More tests
* Java 9 Module


## License

MIT
