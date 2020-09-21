package com.github.movilepay.ktmapper

import com.github.movilepay.ktmapper.helpers.KtMapperInternalHelper
import com.github.movilepay.ktmapper.mappings.MappingConfigurationBase
import com.github.movilepay.ktmapper.reflection.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.jvmErasure

object DeepKtMapper {

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

    @Suppress("UNCHECKED_CAST")
    inline fun <reified TSource : Any, reified TTarget : Any> map(source: TSource): TTarget =
        map(source, TSource::class, TTarget::class) as TTarget

    fun map(src: Any, srcType: KClass<*>, tgtType: KClass<*>): Any {
        val targetCtor = tgtType.constructors.maxBy { it.parameters.count() }!!
        val targetCtorParameters = buildCtorParameters(targetCtor, src, srcType, tgtType)
        return targetCtor.call(*targetCtorParameters.toTypedArray())
    }

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    @JvmSynthetic
    internal fun buildCtorParameters(
        tgtCtor: KFunction<*>,
        src: Any,
        srcType: KClass<*>,
        tgtType: KClass<*>
    ): List<Any?> =
        tgtCtor.parameters.map { ctorParam: KParameter ->
            val configRef: String = KtMapperInternalHelper.buildMapConfigRef(ctorParam, srcType, tgtType)
            if (configurationMap.containsKey(configRef)) {
                val mappingConfiguration = configurationMap.getValue(configRef) as MappingConfigurationBase<*, Any, *, Any>
                processMappingConfig(mappingConfiguration, src, srcType, ctorParam)
            } else {
                processDefaultMapping(ctorParam, src, srcType)
            }
        }

    @PublishedApi
    @JvmSynthetic
    internal fun processMappingConfig(
        mapConfig: MappingConfigurationBase<*, Any, *, Any>,
        src: Any,
        srcType: KClass<*>,
        ctorParam: KParameter
    ): Any? =
        when {
            mapConfig.mapCondition == null       -> processAllowedMapping(mapConfig, src, srcType, ctorParam)
            mapConfig.mapCondition!!.invoke(src) -> processAllowedMapping(mapConfig, src, srcType, ctorParam)
            else                                 -> mapConfig.valueIfNotMappedDueCondition
        }

    @PublishedApi
    @JvmSynthetic
    internal fun processAllowedMapping(
        mapConfig: MappingConfigurationBase<*,Any,*,*>,
        src: Any,
        srcType: KClass<*>,
        p: KParameter
    ): Any? =
        when (mapConfig.mapFromFn) {
            null -> processDefaultMapping(p, src, srcType)
            else -> mapConfig.mapFromFn!!.invoke(src)
        } ?: mapConfig.valueIfNull

    @PublishedApi
    @JvmSynthetic
    internal fun processDefaultMapping(
        targetCtorParameter: KParameter,
        srcObject: Any?,
        srcObjectType: KClass<*>
    ): Any? {
        val srcProperty: KProperty<*> = getMatchingProperty(targetCtorParameter, srcObjectType)
        val srcPropertyValue: Any? = Reflection.Properties.getValue(srcProperty, srcObject!!)
        return when {
            srcProperty.returnType == targetCtorParameter.type -> srcPropertyValue
            srcPropertyValue == null -> srcPropertyValue
            else -> map(srcPropertyValue, srcProperty.returnType.jvmErasure, targetCtorParameter.type.jvmErasure)
        }
    }

    @PublishedApi
    @JvmSynthetic
    internal fun getMatchingProperty(param: KParameter, srcType: KClass<*>): KProperty<*> =
        if (param.name == null) Reflection.Properties.getPropertyByIndex(param.index, srcType)
        else Reflection.Properties.getPropertyByName(param.name!!, srcType)
}
