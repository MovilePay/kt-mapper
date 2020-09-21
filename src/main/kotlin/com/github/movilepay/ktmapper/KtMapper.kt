package com.github.movilepay.ktmapper

import com.github.movilepay.ktmapper.helpers.KtMapperInternalHelper
import com.github.movilepay.ktmapper.mappings.MappingConfigurationBase
import com.github.movilepay.ktmapper.reflection.Reflection
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

object KtMapper {

    @PublishedApi
    @JvmSynthetic
    internal val configurationMap:
            MutableMap<String, MappingConfigurationBase<*, *, *, *>> = mutableMapOf()

    fun register(
        configRef: String,
        mapConfiguration: MappingConfigurationBase<*, *, *, *>
    ) {
        configurationMap.putIfAbsent(configRef, mapConfiguration)
    }

    inline fun <reified TSource : Any, reified TTarget : Any> map(src: TSource): TTarget {
        val targetCtor: KFunction<TTarget> =
            Reflection.Constructors.getConstructorWithMostParameters()
        val targetCtorParameters: Collection<Any?> =
            buildCtorParameters(targetCtor, src)
        return Reflection.Functions.invoke(targetCtor, targetCtorParameters)
    }

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    @JvmSynthetic
    internal inline fun <reified TSource : Any, reified TTarget : Any> buildCtorParameters(
        targetCtor: KFunction<TTarget>,
        src: TSource
    ): List<Any?> =
        Reflection.Params.getFunctionParameters(targetCtor).map { ctorParam: KParameter ->
            val configRef: String = KtMapperInternalHelper.buildMapConfigRef<TSource, TTarget>(ctorParam)
            if (configurationMap.containsKey(configRef)) {
                val mappingConfiguration: MappingConfigurationBase<*, TSource, *, TTarget> =
                    configurationMap.getValue(configRef) as MappingConfigurationBase<*, TSource, *, TTarget>
                processMappingConfig(mappingConfiguration, src, ctorParam)
            } else {
                processDefaultMapping(ctorParam, src)
            }
        }

    @PublishedApi
    @JvmSynthetic
    internal inline fun <reified TSource : Any, reified TTarget : Any> processMappingConfig(
        mappingConfiguration: MappingConfigurationBase<*, TSource, *, TTarget>,
        src: TSource,
        ctorParam: KParameter
    ): Any? =
        if (mappingConfiguration.mapCondition == null) {
            processAllowedMapping(mappingConfiguration, src, ctorParam)
        } else {
            if (mappingConfiguration.mapCondition!!.invoke(src)) {
                processAllowedMapping(mappingConfiguration, src, ctorParam)
            } else {
                mappingConfiguration.valueIfNotMappedDueCondition
            }
        }

    @PublishedApi
    @JvmSynthetic
    internal inline fun <reified TSource : Any> processAllowedMapping(
        mappingConfiguration: MappingConfigurationBase<*, TSource, *, *>,
        src: TSource,
        p: KParameter
    ): Any? =
        if (mappingConfiguration.mapFromFn == null) {
            processDefaultMapping(p, src) ?: mappingConfiguration.valueIfNull
        } else {
            val value: Any? = mappingConfiguration.mapFromFn!!.invoke(src)
            value ?: mappingConfiguration.valueIfNull
        }

    @PublishedApi
    @JvmSynthetic
    internal inline fun <reified TSource : Any> processDefaultMapping(
        p: KParameter,
        src: TSource
    ): Any? {
        val targetProperty: KProperty<*> =
            if (p.name == null) Reflection.Properties.getPropertyByIndex<TSource>(p.index)
            else Reflection.Properties.getPropertyByName<TSource>(p.name!!)
        return Reflection.Properties.getValue(targetProperty, src)
    }
}
