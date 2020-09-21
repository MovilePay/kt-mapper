package com.github.movilepay.ktmapper.mappings

import com.github.movilepay.ktmapper.DeepKtMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DeepKtMapperTest {
    data class SourceFlatClass(val prop: String)
    data class TargetFlatClass(val prop: String)

    data class SourceDeepClass(val prop: SourceFlatClass)
    data class TargetDeepClass(val prop: TargetFlatClass)

    @Test
    fun `map should map flat objects with matching property types`() {
        val input = SourceFlatClass("value")
        val output: TargetFlatClass = DeepKtMapper.map(input)

        assertEquals(output.prop, input.prop)
    }

    @Test
    fun `map should map deep objects with mismatched property types`() {
        val input = SourceDeepClass(SourceFlatClass("value"))
        val output: TargetDeepClass = DeepKtMapper.map(input)

        assertEquals(output.prop.prop, input.prop.prop)
    }

}