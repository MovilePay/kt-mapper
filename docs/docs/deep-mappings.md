---
id: deep-mappings
title: Deep Mapping
---

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


export const Highlight = ({children, color}) => (
  <span
    style={{
      backgroundColor: color,
      borderRadius: '2px',
      color: '#fff',
      padding: '0.2rem',
    }}>
    {children}
  </span>
);

<Highlight color="#25c2a0">Docusaurus green</Highlight> and <Highlight color="#1877F2">Facebook blue</Highlight> are my favorite colors.
