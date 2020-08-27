---
id: conditional-mappings
title: Conditional mappings
---

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

