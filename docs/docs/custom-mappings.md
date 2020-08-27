---
id: custom-mappings
title: Custom Mappings Patterns
---


Sometimes, the relation between source objects and target objects will not be an *as-is* relation. For this cases, you can use the *KtMapper Fluent Mapping API*. This API let you to change the way that KtMapper will map a specific property of a target object from a source object.
The *KtMapper Fluent Mapping API* has two main units:

 - A *target property* selector
 - A *source* selector

Lets consider the following classes:

```kotlin
data class Foo(
    val id: UUID,
    val name: String,
    val age: Int,
    val address: String
)

data class Bar(
    val id: UUID,
    val name: String,
    val age: Int,
    val addressWithAnotherName: String
)
```

In this case, the default naming convention adopted by KtMapper will not work for the properties `address` and `addressWithAnotherName` due to the fact the properties has different names. So, if we want to map source objects `Bar` to `Foo`, we will need to *teach* KtMapper that the property `addressWithAnotherName` should be redirected to the property `address`.
The best way to use the *KtMapper Fluent Mapping API* is combine KtMapper structures and `with()` Kotlin function. You can see the example of this mapping below:

```kotlin
with (MapForProperty<Foo, String>(Foo::address)) { // Target property selector
    whenSourceIs<Bar>() // Source selector
        .mapFrom { it.addressWithAnotherName } // How to map the target property from source
        .register() // Registering mapping to KtMapper. Do not forget to call this!
}
```

A target object can be mapped from different sources. So, a single target property selector can handle multiple property selectors. Below there is an example with a single target property selector with multiple source selectors.

```kotlin
with (MapForProperty<Foo, String>(Foo::address)) { // Single target property selector
    whenSourceIs<Bar>() // Source selector 1
        .mapFrom { it.addressWithAnotherName }
        .register()
        
    whenSourceIs<OtherClass>() // Source selector 2
        .mapFrom { it.otherAddressProperty }
        .register()        
} // End of target property selector
```

**An important observation: we just need to use *KtMapper Fluent Mapping API* for properties that does not follow the conventions! The other properties will follow the way of naming conventions naturally!**

