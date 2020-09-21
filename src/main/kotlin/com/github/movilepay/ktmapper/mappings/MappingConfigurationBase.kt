package com.github.movilepay.ktmapper.mappings

import com.github.movilepay.ktmapper.DeepKtMapper
import com.github.movilepay.ktmapper.KtMapper
import com.github.movilepay.ktmapper.helpers.KtMapperInternalHelper
import kotlin.reflect.KProperty

abstract class MappingConfigurationBase<
        TStrategy : MappingConfigurationBase<TStrategy, TSource, TProperty, TTarget>,
        TSource, TProperty, TTarget>(
    private val sourceClassName: String,
    private val property: KProperty<TProperty>
) {

    @PublishedApi
    @JvmSynthetic
    internal var valueIfNull: TProperty? = null

    @PublishedApi
    @JvmSynthetic
    internal var mapCondition: ((TSource) -> Boolean)? = null

    @PublishedApi
    @JvmSynthetic
    internal var valueIfNotMappedDueCondition: TProperty? = null

    @PublishedApi
    @JvmSynthetic
    internal var mapFromFn: ((TSource) -> Any?)? = null

    fun mapFrom(mapFromFn: (TSource) -> Any?): TStrategy =
        apply { this.mapFromFn = mapFromFn } as TStrategy

    fun ifNullReplaceWith(valueIfNull: TProperty): TStrategy =
        apply { this.valueIfNull = valueIfNull } as TStrategy

    fun onlyMapIf(condition: (TSource) -> Boolean): TStrategy =
        apply { this.mapCondition = condition } as TStrategy

    fun ifNotMappedReplaceWith(valueIfNotMappedDueCondition: TProperty): TStrategy =
        apply { this.valueIfNotMappedDueCondition = valueIfNotMappedDueCondition } as TStrategy

    fun register() {
        KtMapper.register(
            KtMapperInternalHelper.buildMapConfigRef(sourceClassName, property),
            mapConfiguration = this
        )
        DeepKtMapper.register(
            KtMapperInternalHelper.buildMapConfigRef(sourceClassName, property),
            mapConfiguration = this
        )
    }
}