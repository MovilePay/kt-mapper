package com.github.movilepay.ktmapper.mappings

import kotlin.reflect.KProperty1

class PropertyMapper<TTarget, TProperty>(
    @PublishedApi
    @JvmSynthetic
    internal val property: KProperty1<TTarget, TProperty>
) {
    inline fun <reified TSource : Any> whenSourceIs(): DefaultMappingConfiguration<TSource, TProperty, TTarget> =
        DefaultMappingConfiguration(
            property,
            sourceClassName = TSource::class.java.name
        )
}
