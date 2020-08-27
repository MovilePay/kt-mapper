---
id: null-values
title: Dealing with nullable values
---

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
