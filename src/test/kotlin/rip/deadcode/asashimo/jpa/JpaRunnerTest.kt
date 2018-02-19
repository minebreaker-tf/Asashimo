package rip.deadcode.asashimo.jpa

import com.google.common.util.concurrent.ListeningExecutorService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import rip.deadcode.asashimo.AsashimoConfig
import rip.deadcode.asashimo.AsashimoRegistry
import rip.deadcode.asashimo.manipulation.BasicRetriever
import rip.deadcode.asashimo.manipulation.BasicSetter
import rip.deadcode.asashimo.resultmapper.JpaResultMapper
import java.sql.Connection
import java.sql.PreparedStatement
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.sql.DataSource

class JpaRunnerTest {

    @Entity
    @Table(name = "user")
    class User(
            @Id
            @Column(name = "user_id")
            var id: Int = 0,
            @Column(name = "user_name")
            var name: String = ""
    )

    @Test
    fun testPersist() {

        val registry = AsashimoRegistry({ mock(DataSource::class.java) },
                AsashimoConfig(),
                JpaResultMapper,
                mock(ListeningExecutorService::class.java),
                BasicRetriever,
                BasicSetter)
        val conn = mock(Connection::class.java)
        val stmt = mock(PreparedStatement::class.java)
        `when`(conn.prepareStatement("insert into user (user_id, user_name) values(?, ?)")).thenReturn(stmt)
        `when`(stmt.execute()).thenReturn(true)

        val user = User(123, "John")

        JpaRunner.persist(registry, conn, user)
    }

}
