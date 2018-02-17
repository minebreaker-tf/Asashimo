package rip.deadcode.asashimo

import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.MoreExecutors
import org.h2.jdbcx.JdbcDataSource
import org.hamcrest.CoreMatchers.isA
import org.junit.*
import org.junit.Assert.fail
import org.junit.rules.ExpectedException
import rip.deadcode.asashimo.resultmapper.MapResultMapper
import java.sql.ResultSet
import javax.persistence.Column
import javax.persistence.Id
import javax.persistence.Table

class ConnectorsTest {

    private var connector: Connector? = null

    @Suppress("RedundantVisibilityModifier")
    @JvmField
    @Rule
    public var expectedException: ExpectedException = ExpectedException.none()

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

    private data class User(val id: Int, val name: String)

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

    @Test
    fun genericTest16() {
        val userFuture = connector!!
                .with(mapOf("id" to 1))
                .useAsync(MoreExecutors.newDirectExecutorService()) {
                    exec("create table user(id int, name varchar)")
                    exec("insert into user values(1, 'John')")
                    fetch("select * from user where id = :id", User::class)
                }
        val user = userFuture.get()

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

    @Test
    fun genericTest17() {

        expectedException.expect(isA(AsashimoNoResultException::class.java))

        connector!!.use {
            exec("create table user(id int, name varchar)")
            fetch("select * from user", User::class)
        }
    }

    @Test
    fun genericTest18() {
        val users = connector!!.use {
            exec("create table user(id int, name varchar)")
            fetchAll("select * from user", User::class)
        }

        assertThat(users).isEmpty()
    }

    @Test
    fun genericTest19() {
        val user = connector!!.use {
            exec("create table user(id int, name varchar)")
            fetchMaybe("select * from user", User::class)
        }
        assertThat(user).isNull()
    }

    @Test
    fun genericTest20() {
        connector!!.exec("create table user(id int, name varchar)")
        val user = connector!!.fetchMaybe("select * from user", User::class)
        assertThat(user).isNull()
    }

    @Test
    fun genericTest33() {
        connector!!.use {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(1, 'John')")
        }
        val user = connector!!.fetchMaybe("select * from user", User::class)
        assertThat(user).isEqualTo(User(1, "John"))
    }

    @Test
    fun genericTest21() {
        val dataSource = JdbcDataSource().apply {
            setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
            user = "sa"
            password = ""
        }
        connector = Connectors.newInstance(dataSource = dataSource, defaultResultMapper = MapResultMapper)
        val result = connector!!.use {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(1, 'John')")
            fetch("select * from user", Map::class)
        }

        assertThat(result).containsExactly("id", "1", "name", "John")
    }

    @Test
    fun genericTest22() {
        val user = connector!!.with {
            it["id"] = 999
            it["name"] = "XXX"
        }.use {
                    exec("create table user(id int, name varchar)")
                    bind("id" to 1)
                    bind("name" to "John")
                    exec("insert into user values(:id, :name)")
                    fetch("select * from user", User::class, userMapper)
                }

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

    @Test
    fun genericTest23() {
        val user = connector!!.with {
            it["id"] = 999
            it["name"] = "XXX"
        }.use {
                    exec("create table user(id int, name varchar)")
                    bind {
                        it["id"] = 1
                        it["name"] = "John"
                    }
                    exec("insert into user values(:id, :name)")
                    fetch("select * from user", User::class, userMapper)
                }

        assertThat(user.id).isEqualTo(1)
        assertThat(user.name).isEqualTo("John")
    }

    @Test
    fun genericTest24() {

        expectedException.expect(isA(AsashimoNonUniqueResultException::class.java))

        connector!!.use {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(1, 'John'), (2, 'Jack')")
            fetch("select * from user", User::class)
        }
    }

    @Table(name = "user")
    data class JpaUser(
            @Id
            @Column(name = "user_id")
            val id: Int = 0,
            val name: String = ""
    )

    @Test
    fun genericTest25() {

        connector!!.exec("create table user(user_id int, name varchar)")

        val user = JpaUser(123, "John")
        connector!!.persist(user)

        val result = connector!!.fetch("select * from user", JpaUser::class)

        assertThat(result.id).isEqualTo(123)
        assertThat(result.name).isEqualTo("John")
    }

    @Test
    fun genericTest26() {

        connector!!.use {
            exec("create table user(user_id int, name varchar)")
            exec("insert into user values(123, 'John')")
        }

        val result = connector!!.find(123, JpaUser::class)

        assertThat(result.id).isEqualTo(123)
        assertThat(result.name).isEqualTo("John")
    }

    @Test
    fun genericTest27() {

        val entity = User(123, "John")
        val result = connector!!.with(entity).use {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(123, 'John')")
            fetch("select id, name from user where id = :id and name = :name", User::class)
        }

        assertThat(result.id).isEqualTo(123)
        assertThat(result.name).isEqualTo("John")
    }

    @Test
    fun genericTest28() {

        val entity = JpaUser(123, "John")
        val result = connector!!.with(entity).use {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(123, 'John')")
            fetch("select id, name from user where id = :user_id and name = :name", User::class)
        }

        assertThat(result.id).isEqualTo(123)
        assertThat(result.name).isEqualTo("John")
    }

    @Test
    fun genericTest29() {

        val users = connector!!.transactional {
            exec("create table user(id int, name varchar)")
            exec("insert into user values(1, 'John')")
            savepoint {
                exec("insert into user values(2, 'Jack')")
                throw RuntimeException()
            }
            fetchAll("select * from user", User::class)
        }

        assertThat(users).hasSize(1)
        assertThat(users[0].id).isEqualTo(1)
        assertThat(users[0].name).isEqualTo("John")
    }

    @Test
    fun genericTest30() {

        val users = connector!!.transactional {
            exec("create table user(id int, name varchar)")
            savepoint("a") {
                exec("insert into user values(1, 'John')")
                savepoint("b") {
                    exec("insert into user values(2, 'Jack')")
                    throw RuntimeException()
                }
            }
            fetchAll("select * from user", User::class)
        }

        assertThat(users).hasSize(1)
        assertThat(users[0].id).isEqualTo(1)
        assertThat(users[0].name).isEqualTo("John")
    }

    @Test
    fun genericTest31() {

        connector!!.exec("create table user(id int, name varchar)")

        val count = connector!!.batch {
            add("insert into user values(1, 'John')")
            add("insert into user values(2, 'Jack')")
        }

        assertThat(count[0]).isEqualTo(1)
        assertThat(count[1]).isEqualTo(1)

        val tableCount = connector!!.fetch("select count(*) from user", Int::class)
        assertThat(tableCount).isEqualTo(2)
    }

    @Test
    fun genericTest32() {

        connector!!.exec("create table user(id int, name varchar)")

        val count = connector!!.batch("insert into user values(:id, :name)")
                .with(mapOf("id" to listOf(1, 2)))
                .with(mapOf("name" to listOf("John", "Jack")))
                .exec()

        assertThat(count[0]).isEqualTo(1)
        assertThat(count[1]).isEqualTo(1)

        val users = connector!!.fetchAll("select * from user", User::class)
        assertThat(users).hasSize(2)
        assertThat(users).containsExactly(User(1, "John"), User(2, "Jack"))
    }

}
