package com.github.movilepay.ktmapper.mappings

import com.github.movilepay.ktmapper.KtMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.UUID

class PropertyMapperTest {
    data class Object1(val source: String)
    data class Object2(val target: UUID)

    @Test
    fun `property mapper should succeed when types are inferred from the typesystem`() {
        with(PropertyMapper(Object2::target)) {
            whenSourceIs<Object1>().mapFrom { UUID.fromString(it.source) }.register()
        }

        val sourceUUID = UUID.randomUUID()
        val obj2: Object2 = KtMapper.map(Object1(sourceUUID.toString()))
        Assertions.assertEquals(sourceUUID, obj2.target)
    }
}