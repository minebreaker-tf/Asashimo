package rip.deadcode.asashimo.resultmapper

import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.ListeningExecutorService
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import rip.deadcode.asashimo.AsashimoConfig
import rip.deadcode.asashimo.AsashimoRegistry
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import javax.persistence.Column
import javax.persistence.Id
import javax.persistence.Table
import javax.sql.DataSource

class JpaResultMapperTest {

    @Table(name = "user")
    data class User(
            @Id
            @Column(name = "user_id")
            val id: Int = 0,
            @Column(name = "user_name")
            val name: String = ""
    )

    @Test
    fun test() {

        val registry = AsashimoRegistry(
                { Mockito.mock(DataSource::class.java) },
                AsashimoConfig(),
                JpaResultMapper,
                Mockito.mock(ListeningExecutorService::class.java)
        )

        val rs = mock(ResultSet::class.java)
        val meta = mock(ResultSetMetaData::class.java)
        `when`(meta.columnCount).thenReturn(2)
        `when`(meta.getColumnName(1)).thenReturn("USER_ID")
        `when`(meta.getColumnName(2)).thenReturn("USER_NAME")
        `when`(rs.metaData).thenReturn(meta)
        `when`(rs.getInt(1)).thenReturn(123)
        `when`(rs.getString(2)).thenReturn("John")

        val result = JpaResultMapper.map(registry, User::class, rs)

        assertThat(result.id).isEqualTo(123)
        assertThat(result.name).isEqualTo("John")
    }

}
