# Asashimo

Thin JDBC wrapper for Kotlin

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
    compile 'rip.deadcode:asashimo:0.2.1'
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

You can use named parameters via `with()' method.

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

If `ListeningExecutorService` in arguments is omitted,
the default executor pooled by Asashimo is used.

(This will be configurable in the future versions)


## TODOs

* Java 8 Date and Time API
* API for Java
* Savepoint
* Batch
* Map API (fetch values with map interface)
* Fetch interface API
* JPA annotation compatibility (`@Id`, `@Column`)
* class to parameter binding, based on JPA annotations / field names
* Parameter binding in `use{}` clause
* `fetchAll()` and `fetchStream()` with lazy list, using cursor
* `persist(KClass)`, `find(id: Any, KClass)`
* Documentation
* More tests

```kotlin
fun jpaAnnotationApi() {
    @Entity
    data class User(
        @Id
        @Column(name="user_id")
        val id: Int,
        @Column(name="user_name")
        val name: String
    )
    var user = connector.find(123, User::class)
    user = User(456, "John")
    connector.persist(user)
}
```

```kotlin
fun mapApi() {
    val result: Map<String, SqlValue> = connector.fetch(
            "select 'hello, world' as message from dual", Map::class)
    assertThat(result["message"].asString()).isEqualTo("hello, world")
}
```

```kotlin
fun cursorApi() {

    class CustomCursor(private val rs: ResultSet) {
        fun getString(): String = rs.getString(1)
    }

    val connector = Connectors.newInstance(dataSource, config, createCursor)
    val data = connector.cursor()
                        .getString()
    val data = connector.cursor(CustomCusor::class)
                        .getString()
}
```
