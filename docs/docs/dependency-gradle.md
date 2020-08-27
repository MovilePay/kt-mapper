---
id: dependency-gradle
title: Gradle Dependency
---


For the old-fashion way using Groovy Script:
```groovy
// https://mvnrepository.com/artifact/com.github.movilepay/kt-mapper
compile group: 'com.github.movilepay', name: 'kt-mapper', version: '0.0.3'
```

If you are using Kotlin DLS, just add this:
```kotlin
// https://mvnrepository.com/artifact/com.github.movilepay/kt-mapper
implementation("com.github.movilepay:kt-mapper:0.0.3")
```