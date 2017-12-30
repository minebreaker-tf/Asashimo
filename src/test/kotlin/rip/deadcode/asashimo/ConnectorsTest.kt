package rip.deadcode.asashimo

import com.google.common.truth.Truth.assertThat
import org.h2.jdbcx.JdbcDataSource
import org.junit.Before
import org.junit.Test
import java.sql.ResultSet

class ConnectorsTest {

    var connector: Connector? = null

    @Before
    fun setUp() {

        val dataSource = JdbcDataSource().apply {
            setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
            user = "sa"
            password = ""
        }
        connector = Connectors.newInstance(dataSource)
    }

    data class User(val id: Int, val name: String)

    val userMapper = { rs: ResultSet -> User(rs.getInt("id"), rs.getString("name")) }

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

}
