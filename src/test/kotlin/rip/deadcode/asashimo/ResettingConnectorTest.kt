package rip.deadcode.asashimo

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource

class ResettingConnectorTest {

    @Test
    fun testDataSourceFactory1() {

        val dataSource = mock(DataSource::class.java)
        val conn = mock(Connection::class.java)
        `when`(dataSource.connection).thenReturn(conn)
        `when`(conn.prepareStatement(any())).thenReturn(mock(PreparedStatement::class.java))

        var timesDataSourceCreated = 0
        val connector = Connectors.newInstance({
            timesDataSourceCreated++
            dataSource
        })

        assertThat(timesDataSourceCreated).isEqualTo(1)

        try {
            connector.use {
                throw RuntimeException()
            }
            @Suppress("UNREACHABLE_CODE")
            fail("")
        } catch (e: AsashimoException) {
        }

        assertThat(timesDataSourceCreated).isEqualTo(1)
        connector.exec("")
        assertThat(timesDataSourceCreated).isEqualTo(2)
        connector.exec("")
        assertThat(timesDataSourceCreated).isEqualTo(2)

        try {
            connector.use {
                throw RuntimeException()
            }
            @Suppress("UNREACHABLE_CODE")
            fail("")
        } catch (e: AsashimoException) {
        }

        assertThat(timesDataSourceCreated).isEqualTo(2)
        connector.exec("")
        assertThat(timesDataSourceCreated).isEqualTo(3)
    }

    @Test
    fun testDataSourceFactory2() {

        val dataSource = mock(DataSource::class.java)
        val conn = mock(Connection::class.java)
        `when`(dataSource.connection).thenReturn(conn)
        `when`(conn.prepareStatement(any())).thenReturn(mock(PreparedStatement::class.java))

        var timesDataSourceCreated = 0
        val connector = Connectors.newInstance({
            timesDataSourceCreated++
            dataSource
        }, AsashimoConfig(resetDataSourceWhenExceptionOccurred = false))

        assertThat(timesDataSourceCreated).isEqualTo(1)

        try {
            connector.use {
                throw RuntimeException()
            }
            @Suppress("UNREACHABLE_CODE")
            fail("")
        } catch (e: AsashimoException) {
        }

        assertThat(timesDataSourceCreated).isEqualTo(1)
        connector.exec("")
        assertThat(timesDataSourceCreated).isEqualTo(1)
    }
}
