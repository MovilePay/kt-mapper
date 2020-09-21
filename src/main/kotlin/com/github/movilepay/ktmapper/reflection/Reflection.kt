package com.github.movilepay.ktmapper.reflection

import java.lang.reflect.Member
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

/**
 * Internal class helper for reflection tasks
 */
object Reflection {

    object Clazz {
        inline fun <reified T : Any> getKClass(): KClass<T> =
            T::class

        inline fun <reified T : Any> isDataClass(): Boolean =
            T::class.isData
    }

    @Suppress("UNCHECKED_CAST")
    object Constructors {
        inline fun <reified T : Any> getAllConstructors(): Collection<KFunction<T>> =
            Clazz.getKClass<T>().constructors

        inline fun <reified T : Any> getConstructorWithMostParameters(): KFunction<T> = getConstructorWithMostParameters(Clazz.getKClass<T>()) as KFunction<T>
        fun getConstructorWithMostParameters(type: KClass<*>): KFunction<*> = type.constructors.maxBy { it.parameters.count() }!!

        inline fun <reified T : Any> getConstructorWithLeastParameters(): KFunction<T> = getConstructorWithLeastParameters(Clazz.getKClass<T>()) as KFunction<T>
        fun getConstructorWithLeastParameters(type: KClass<*>): KFunction<*> = type.constructors.minBy { it.parameters.count() }!!
    }

    object Params {
        fun getFunctionParameters(fn: KFunction<*>): List<KParameter> = fn.parameters
    }

    @Suppress("UNCHECKED_CAST")
    object Properties {
        inline fun <reified T : Any> getPropertyByName(name: String) = getPropertyByName(name, Clazz.getKClass<T>())
        fun getPropertyByName(name: String, targetType: KClass<*>): KProperty<*> = targetType.declaredMemberProperties.firstOrNull { it.name == name }!!

        inline fun <reified T : Any> getPropertyByIndex(index: Int): KProperty<*> = getPropertyByIndex(index, Clazz.getKClass<T>())
        fun getPropertyByIndex(index: Int, targetType: KClass<*>): KProperty<*> = targetType.declaredMemberProperties.elementAt(index)

        fun <T> getValue(prop: KProperty<*>, src: Any): T = prop.call(src) as T

        fun getDeclaringClass(prop: KProperty<*>): Class<*> = (prop.javaField as Member? ?: prop.javaGetter)?.declaringClass!!
    }

    object Functions {
        fun <T> invoke(fn: KFunction<T>, params: Collection<Any?>): T = fn.call(*params.toTypedArray())
    }

}