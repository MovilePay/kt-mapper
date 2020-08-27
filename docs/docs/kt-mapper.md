---
id: kt-mapper
title: Getting Started
---

**KtMapper** is a library to perform an "object to object" mapping for Kotlin classes based on naming convetions. It can be useful for, for example, do a mapping between domain entities and DTO entities in a API by a "automatic" way.
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