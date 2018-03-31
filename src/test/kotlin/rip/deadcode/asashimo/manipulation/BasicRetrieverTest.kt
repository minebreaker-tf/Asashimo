package rip.deadcode.asashimo.manipulation

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.sql.ResultSet

class BasicRetrieverTest {


    @Test
    fun testBasicType1() {
        val rs = Mockito.mock(ResultSet::class.java)
        Mockito.`when`(rs.getInt(1)).thenReturn(123)
        val res = BasicRetriever.retrieveByClass(rs, Int::class, 1)

        assertThat(res).isEqualTo(123)
    }

    @Test
    fun testBasicType2() {
        val rs = Mockito.mock(ResultSet::class.java)
        Mockito.`when`(rs.getBytes(1)).thenReturn(byteArrayOf(62, 63, 64))
        val res = BasicRetriever.retrieveByClass(rs, ByteArray::class, 1)

        assertThat(res).isEqualTo(byteArrayOf(62, 63, 64))
    }
}
