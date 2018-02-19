package rip.deadcode.asashimo.jpa

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import javax.persistence.Column
import javax.persistence.Id
import javax.persistence.Table

class JpaIntrospectorTest {

    @Suppress("unused")
    @Table(name = "user")
    class User(
            @Id
            @Column(name = "user_id")
            var id: Int = 0,
            @Column(name = "user_name")
            var name: String = ""
    )

    @Test
    fun testAnnotated() {
        val result = JpaIntrospector.introspect(User())

        assertThat(result.tableName).isEqualTo("user")
        assertThat(result.idName).isEqualTo("user_id")
        assertThat(result.columnNames).containsExactly("user_name")
    }

    @Suppress("unused")
    class UserNoAnnotation(
            @Id  // @Id is always required
            var id: Int = 0,
            var name: String = ""
    )

    @Test
    fun testNotAnnotated() {
        val result = JpaIntrospector.introspect(UserNoAnnotation())

        assertThat(result.tableName).isEqualTo("UserNoAnnotation")
        assertThat(result.idName).isEqualTo("id")
        assertThat(result.columnNames).containsExactly("name")
    }

}