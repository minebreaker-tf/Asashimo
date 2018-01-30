package rip.deadcode.asashimo.jpa

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

class JpaIntrospectorTest {

    @Suppress("unused")
    @Entity(name = "user")
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

        assertThat(result.table).isEqualTo("user")
        assertThat(result.id).isEqualTo("user_id")
        assertThat(result.columns).containsExactly("user_name")
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

        assertThat(result.table).isEqualTo("UserNoAnnotation")
        assertThat(result.id).isEqualTo("id")
        assertThat(result.columns).containsExactly("name")
    }

}