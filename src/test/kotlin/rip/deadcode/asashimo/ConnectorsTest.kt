package rip.deadcode.asashimo

import com.google.common.truth.Truth.assertThat
import org.h2.jdbcx.JdbcDataSource
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.sql.ResultSet

class ConnectorsTest {

    private var connector: Connector? = null

    @Before
    fun setUp() {

        val dataSource = JdbcDataSource().apply {
            setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
            user = "sa"
            password = ""
        }
        connector = Connectors.newInstance(dataSource)
    }

    @After
    fun tearDown() {
        // コネクションごとにデータベースを閉じない設定にしているため、手動でクローズしてDBをリセットする
        connector?.exec("shutdown")
    }

    data class User(val id: Int, val name: String)

    private val userMapper = { rs: ResultSet -> User(rs.getInt("id"), rs.getString("name")) }

    @Test
    fun hello() {
        val message = connector!!.fetch("select 'hello, world' from dual", String::class)
        assertThat(message).isEqualTo("hello, world")
    }

    @Test
    fun genericTest1() {
        connector!!.exec("create table user(id int, name varchar)")
        connector!!.exec("insert into user values(1, 'John')")
        val user = connector!!.fetch(
                "select * from user", User::class, userMapper)

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

    @Test
    fun genericTest2() {
        val user = connector!!.use {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(1, 'John')")
            fetch("select * from user", User::class, userMapper)
        }

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

    @Test
    fun genericTest3() {
        try {
            connector!!.transactional {
                exec("create table user(id int, name varchar)")
                exec("insert into user values(1, 'John')")
                val user = fetch("select * from user", User::class, userMapper)

                assertThat(user.id).isEqualTo(1)
                assertThat(user.name).isEqualTo("John")

                throw RuntimeException()
            }
        } catch (e: AsashimoException) {

            val count = connector!!.fetch("select count(*) from user", Int::class)
            assertThat(count).isEqualTo(0)
            return
        }

        @Suppress("UNREACHABLE_CODE")
        fail()
    }

    @Test
    fun genericTest4() {
        val user = connector!!.transactional {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(1, 'John')")
            fetch("select * from user", User::class, userMapper)
        }
        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")

        connector!!.exec("rollback")

        // Assure "rollback" had no effect
        val user2 = connector!!.fetch("select * from user", User::class, userMapper)
        assertThat(user2.id).isEqualTo(1)
        assertThat(user2.name).isEqualTo("John")
    }

    @Test
    fun genericTest5() {
        connector!!.exec("create table user(id int, name varchar)")
        connector!!.exec("insert into user values(1, 'John')")
        val user = connector!!.fetch(
                "select * from user", User::class)

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

    @Test
    fun genericTest6() {
        val user = connector!!.use {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(1, 'John')")
            fetch("select * from user", User::class)
        }

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

    @Test
    fun genericTest7() {
        connector!!.exec("create table user(id int, name varchar)")
        connector!!.exec("insert into user values(1, 'John')")
        val user = connector!!
                .with(mapOf("id" to 1))
                .fetch("select * from user where id = :id", User::class)

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

    @Test
    fun genericTest8() {
        connector!!.exec("create table user(id int, name varchar)")
        connector!!.exec("insert into user values(1, 'John')")
        val user = connector!!.with {
            it["id"] = 1
        }.fetch("select * from user where id = :id", User::class)

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

    @Test
    fun genericTest9() {
        val user = connector!!
                .with(mapOf("id" to 1, "name" to "John"))
                .use {
                    exec("create table user(id int, name varchar)")
                    exec("insert into user values(:id, 'John')")
                    fetch("select * from user where name = :name", User::class)
                }

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

    @Test
    fun genericTest10() {
        val user = connector!!.with {
            it["id"] = 1
            it["name"] = "John"
        }.use {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(:id, 'John')")
            fetch("select * from user where name = :name", User::class)
        }

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

    @Test
    fun genericTest11() {
        val user = connector!!.with {
            it["id"] = 1
            it["name"] = "John"
        }.transactional {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(:id, 'John')")
            fetch("select * from user where name = :name", User::class)
        }

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

    @Test
    fun genericTest12() {
        val user = connector!!.with {
            it["ids"] = listOf(2, 3)
        }.use {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(1, 'John')")
            exec("insert into user values(2, 'Jack')")
            exec("insert into user values(3, 'Jane')")
            fetchAll("select * from user where id in (:ids)", User::class)
        }

        assertThat(user).hasSize(2)
        assertThat(user[0].id).isEqualTo(2)
        assertThat(user[0].name).isEqualTo("Jack")
        assertThat(user[1].id).isEqualTo(3)
        assertThat(user[1].name).isEqualTo("Jane")
    }

    /**
     * H2 doesn't support `largeUpdate()`.
     */
    @Ignore
    @Test
    fun genericTest13() {
        connector!!.exec("create table user(id int, name varchar)")
        val n: Long = connector!!.execLarge("insert into user values(1, 'John')")

        assertThat(n).isEqualTo(1)
    }

    @Test
    fun genericTest14() {
        connector!!.use {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(1, 'John')")
        }
        val user = connector!!.fetchLazy("select * from user", User::class).get()

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

    @Test
    fun genericTest15() {
        connector!!.use {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(1, 'John')")
        }

        val userFuture = connector!!.fetchAsync("select * from user", User::class)
        val user = userFuture.get()

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

}
