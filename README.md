[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Hawk-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1568)  [![API](https://img.shields.io/badge/API-10%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=10)

#Hawk

[![Join the chat at https://gitter.im/orhanobut/hawk](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/orhanobut/hawk?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
Secure, simple storage for android

Hawk uses:
- AES for the crypto
- SharedPreferences for the storage
- Gson

Hawk provides:
- Secure data persistence
- Save any type
- Save list of any type

###Add dependency
```groovy
repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots/"}
}
dependencies {
    compile 'com.orhanobut:hawk:1.1-SNAPSHOT'
}
```

#### Initialize the hawk

```java
Hawk.init(context);
```

#### Save
```java
Hawk.put(key, ANYTHING);
```

#### Get
```java
T result = Hawk.get(key);
```
or with default value

```java
T result = Hawk.get(key, T);
```

#### Remove
```java
Hawk.remove(key);
```

#### Contains
```java
boolean contains = Hawk.contains(key); 
```

#### Set the log output (optional)
```java
Hawk.init(context, LogLevel.FULL); // as default it is NONE
```

##### More samples for save

```java
Hawk.put("key", "something"); // Save string
Hawk.put("key", true); // save boolean
Hawk.put("key", new Foo()); // save an object
Hawk.put("key", List<String>); // save list
Hawk.put("key", List<Foo>); // save list of any type
Hawk.put("key", 1234); // save numbers
```

##### More samples for get

```java
String value = Hawk.get(key);
int value = Hawk.get(key);
Foo value = Hawk.get(key);
boolean value = Hawk.get(key);
List<String> value = Hawk.get(key);
List<Foo> value = Hawk.get(key);
```
or with the defaults
```java
String value = Hawk.get(key, "");
int value = Hawk.get(key, 0);
Foo value = Hawk.get(key, new Foo());
boolean value = Hawk.get(key, false);
List<String> value = Hawk.get(key, Collections.emptyList());
List<Foo> value = Hawk.get(key, new ArrayList<Foo>);
```


###License
<pre>
Copyright 2015 Orhan Obut

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>
