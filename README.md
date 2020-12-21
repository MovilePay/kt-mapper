[![MovilePay](https://maquinadecartaoboa.com/wp-content/uploads/2019/06/maquininha-de-cart%C3%A3o-movile-pay.png?s=10)](https://maquinadecartaoboa.com/wp-content/uploads/2019/06/maquininha-de-cart%C3%A3o-movile-pay.png)
# MovilePay KtMapper

**KtMapper** is a library to perform an "object to object" mapping for Kotlin classes based on naming conventions. It can be useful when mapping between domain entities and DTO entities in an API by an "automatic" way.
For example: supposing that you have the following class...

```kotlin
data class Bar (
    val id: UUID,
    val name: String,
    val age: Int
)
```

... and you want to map `Bar` objects to `Foo` objects with following structure:

```kotlin
data class Foo(
    val id: UUID,
    val name: String
)
```

You can use KtMapper to create `Bar` objects without any configuration:

```kotlin
val res: Foo = KtMapper.map(
    Bar(
        id = UUID.randomUUID(),
        name = "Zezinho da Silva Sauro",
        age = 20
    )
)
//The line bellow will print "Foo(id=ANY-UUID, name=Zezinho da Silva Sauro)"
println(res)
```

#### Mapping conventions
KtMapper uses the concept of *naming convention*. This means that: without any kind of configuration, KtMapper is capable to map objects by a naming convention. In this case, the convention is based on the name of properties that should be mapped from source object to target object.
This mapping convention can be represented by the following illustration:

```
----------------              ----------------            
| SourceClass  |              | TargetClass  |
================              ================
| name: String | -----------> | name: String |
----------------              ----------------
| age: Int     | -----------> | age: Int     |
----------------              ----------------
 ```
 
 KtMapper also take into account the data type of the properties between source objects and target objects.

#### Custom mapping patterns
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

#### Dealing with nullable values
Sometimes, we can need to replace `null` when mapping with a default value. The *KtMapper Fluent Mapping API* supports this by using the method `ifNullReplaceWith()`. Lets consider the following classes:

```kotlin
data class Foo(
    val id: UUID,
    val name: String,
    val age: Int
)

data class Bar(
    val id: UUID,
    val name: String,
    val age: Int? = null
)
```

If we map the property `age` from `Bar` to property `age` from `Foo`, we can get a `null` value being mapped to `age` from `Foo` because `age` on `Bar` is nullable. But, we can set a default value to be put into `age` from `Foo` if `age` from `Bar` is null by using *KtMapper Fluent Mapping API*. Below there is an example of this mapping:

```kotlin
with(MapForProperty<Foo, Int>(Foo::age)) {
    whenSourceIs<Bar>()
        .ifNullReplaceWith(-1) // Default value will be -1 
        .register()
    }
```

With this mapping configuration, if we call KtMapper for these two instances of `Bar`...

```
val res: Foo = KtMapper.map(
    Bar(
        id = UUID.randomUUID(),
        name = "Zezinho da Silva Sauro",
        age = 20
    )
)
println(res)

val res2: Foo = KtMapper.map(
    Bar(
        id = UUID.randomUUID(),
        name = "Zezinho da Silva Sauro",
        age = null
    )
)
println(res2)
```

... we will get the following output:

```
// Mapping without using the default value
Foo(id=54821917-4e03-45b5-a12c-105308f7fefb, name=Zezinho da Silva Sauro, age=20)
// Mapping using the default value -------------------------------------------|
Foo(id=e06b402c-446e-447b-9d79-ba98ce26f436, name=Zezinho da Silva Sauro, age=-1)
```

*KtMapper Fluent Mapping API* let you to use combination between `mapFrom()` and `ifNullReplaceWith()`. With this combination, KtMapper will evaluate `mapFrom()` first. If the returned value is `null`, the value configured by `ifNullReplaceWith()` will be used. Below, there is an example of this kind of combination.

```kotlin
with(MapForProperty<Foo, Int>(Foo::age)) {
    whenSourceIs<Bar>()
        .mapFrom { it.age?.let { a -> a * 10 } }
        .ifNullReplaceWith(-1)
        .register()
}
```

The calling order of methods of *KtMapper Fluent Mapping API* does not affect the way that KtMapper works: you can call the methods in any order. **You can also combine all the methods from *KtMapper Fluent Mapping API* if you need.**

#### Conditional mappings
Sometimes, we need to map a specific property only if a specific condition is satisfied. For this case, *KtMapper Fluent Mapping API* let you to use the method `onlyMapIf()`. This method let you to specify a condition on source object that must be satisfied to let the value be mapped to target object. In addition, you can set a value to be used if the condition is not satisfied by using the method `ifNotMappedReplaceWith()`.
Lets consider the following classes:

```kotlin
data class Foo(
    val id: UUID,
    val name: String,
    val age: Int
)

data class Bar(
    val id: UUID,
    val name: String,
    val age: Int
)
```

We can wish to map the property `name` from `Bar` to `name` from `Foo` only if the number of characters is up to 10. If this condition is false, we can wish to put "NOT MAPPED" instead of the original value. To fit this requirements, we can use the following mapping configuration:

```kotlin
with(MapForProperty<Foo, String>(Foo::name)) {
    whenSourceIs<Bar>()
        .onlyMapIf { it.name.length <= 10 }
        .ifNotMappedReplaceWith("NOT MAPPED")
        .register()
}
```

With this configuration, if we map the following objects...

```kotlin
val res: Foo = KtMapper.map(
    Bar(
        id = UUID.randomUUID(),
        name = "Zé", // Less that 10 characters
        age = 20
    )
)
println(res)

val res2: Foo = KtMapper.map(
    Bar(
        id = UUID.randomUUID(),
        name = "Zezinho da Silva Sauro", // More than 10 characters
        age = 10
    )
)
println(res2)
```

... the following output will be produced:

```
// First object: up to 10 characters
Foo(id=292b65a1-223a-48b3-bd98-7a30a8a95c75, name=Zé, age=20)
// Secound object: more than 10 characters - must use value of ifNotMappedReplaceWith()
Foo(id=36030a7c-f32e-4cce-958e-e66fecffdd3c, name=NOT MAPPED, age=10)
```

It is a good idea using `onlyMapIf()` together with `ifNotMappedReplaceWith()`. If the condition of `onlyMapIf()` will not satisfied and the mapping does not contain a value defined by `ifNotMappedReplaceWith()`, KtMapper will send `null` to the property of target object. If the property can not receive a `null` value, an error in execution time will be raised.


#### Deep mapping
Unfortunately, the current version of KtMapper does not support deep mapping for properties like collections in a "natural way". But, there is an workaround: you can use a combination of methods of *KtMapper Fluent Mapping API* with inner calls of KtMapper, mainly inside the method `mapFrom()`. Lets consider the following classes:
 
```kotlin
data class InternalForFoo(
    val id: String,
    val name: String
)

class InternalForBar(
    val id: String,
    val nameInternalBar: String
)

data class Foo(
    val id: UUID,
    val name: String,
    val internalFoos: List<InternalForFoo>
)

data class Bar(
    val id: UUID,
    val name: String = "BLA",
    val internalBars: List<InternalForBar>
)
```

Above, we can see two classes that does not follow the naming conventions that KtMapper adopts. We also can see that we have collections of objects (`InternalForFoo` and `InternalForBar`) that does not follow the conventions. In this case, we need to write a bit more complex configuration mapping like this:

```kotlin
// First: mapping for internal objects...
with(MapForProperty<InternalForFoo, String>(InternalForFoo::name)) {
    whenSourceIs<InternalForBar>()
        .mapFrom { it.nameInternalBar }
        .register()
}

// Second: mapping for collections with inner calls of KtMapper
with(MapForProperty<Foo, List<InternalForFoo>>(Foo::internalFoos)) {
    whenSourceIs<Bar>()
        .mapFrom {
            it.internalBars.map { i: InternalForBar ->
                // Inner call of KtMapper: OK, no problem! :)
                KtMapper.map<InternalForBar, InternalForFoo>(i)
            }
        }.register()
}
```

With this mapping configuration, if we execute the following code...

```kotlin
val res: Foo = KtMapper.map(
    Bar(
        id = UUID.randomUUID(),
        name = "Zezinho da Silva Sauro",
        internalBars = listOf(
            InternalForBar(
                id = "JC",
                nameInternalBar = "Jaca"
            )
        )
    )
)
println(res)
```

...the output will be:

```
Foo(id=298922fe-4bea-4063-92aa-efd09c0fc7c3, name=Zezinho da Silva Sauro, internalFoos=[InternalForFoo(id=JC, name=Jaca)])
```

We can see mapping worked as expected.

#### Current wishlist
- Refactor code to a more Kotlin idiomatic way (mainly internal code);
- Remove the need to call `register()` method explicitly for every mapping configuration;
- Let create a class with mapping configurations for a target with auto-loading by KtMapper;
- Implement collection mapping without inner KtMapper calls.

**Feel free to open a pull request to fix bugs and/or improve how KtMapper works! ;)**

*Made with ❤ by MovilePay*
