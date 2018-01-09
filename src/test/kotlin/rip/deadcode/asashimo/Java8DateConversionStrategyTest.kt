package rip.deadcode.asashimo

import com.google.common.truth.Truth.assertThat
import org.h2.jdbcx.JdbcDataSource
import org.junit.After
import org.junit.Before
import org.junit.Test
import rip.deadcode.asashimo.Java8DateConversionStrategy.CONVERT
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class Java8DateConversionStrategyTest {

    private var connector: Connector? = null

    @Before
    fun setUp() {

        val dataSource = JdbcDataSource().apply {
            setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
            user = "sa"
            password = ""
        }
        connector = Connectors.newInstance(dataSource, AsashimoConfig(java8dateConversionStrategy = CONVERT))
    }

    @After
    fun tearDown() {
        // コネクションごとにデータベースを閉じない設定にしているため、手動でクローズしてDBをリセットする
        connector?.exec("shutdown")
    }

    private data class LocalDateTimeClass(val date: LocalDateTime)
    private data class ZonedDateTimeClass(val date: ZonedDateTime)

    @Test
    fun test1() {
        val base = LocalDateTime.of(2000, 1, 1, 10, 10, 10)
        val result = connector!!.with(mapOf("ts" to base)).use {
            exec("create table test(ts timestamp)")
            exec("insert into test values(:ts)")
            fetch("select * from test", LocalDateTimeClass::class)
        }

        assertThat(result.date).isEqualTo(LocalDateTime.of(2000, 1, 1, 10, 10, 10))
    }

    @Test
    fun test2() {
        val local = LocalDateTime.of(2000, 1, 1, 10, 10, 10)
        val base = ZonedDateTime.of(local, ZoneId.of("JST", ZoneId.SHORT_IDS))
        // 2000-01-01T10:10:10+09:00[Asia/Tokyo]

        val result = connector!!.with(mapOf("ts" to base)).use {
            exec("create table test(ts timestamp)")
            exec("insert into test values(:ts)")
            fetch("select * from test", LocalDateTimeClass::class)
        }

        assertThat(result.date).isEqualTo(LocalDateTime.of(2000, 1, 1, 1, 10, 10))
    }

    @Test
    fun test3() {

        val dataSource = JdbcDataSource().apply {
            setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
            user = "sa"
            password = ""
        }
        connector = Connectors.newInstance(dataSource, AsashimoConfig(
                java8dateConversionStrategy = CONVERT,
                databaseZoneOffset = ZoneOffset.of("+9")
        ))

        val local = LocalDateTime.of(2000, 1, 1, 10, 10, 10)

        val result = connector!!.with(mapOf("ts" to local)).use {
            exec("create table test(ts timestamp)")
            exec("insert into test values(:ts)")
            println(fetch("select * from test", String::class))
            fetch("select * from test", ZonedDateTimeClass::class)
        }

        val resultLocal = LocalDateTime.of(2000, 1, 1, 10, 10, 10)
        val resultZoned = ZonedDateTime.of(resultLocal, ZoneOffset.of("+9"))
        assertThat(result.date).isEqualTo(resultZoned)
    }

}
