---
id: mapping-conventions
title: Mapping Conventions
---

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