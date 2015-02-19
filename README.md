#Hawk
Secure and easy storage for Android.

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
    compile 'com.orhanobut:hawk:1.0-SNAPSHOT'
}
```

#### Initialize the hawk

```java
Hawk.init(context);
```

#### Save
```java
Hawk.put("key", ANYTHING);
```

#### Get
```java
T result = Hawk.get("key");
```
or with default value

```java
T result = Hawk.get("key", T);
```
##### More samples for save

```java
Hawk.put("key", "something"); // Save string
Hawk.put("key", true); // save boolean
Hawk.put("key", new Foo()); // save an object
Hawk.put("key", List<String>); // save list
Hawk.put("key", List<Foo>); // save list of any class
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


####ProGuard

If you are using ProGuard you should add the following options to your configuration file:

```
#Gson
-keep class com.google.gson.** { *; }
-keepattributes Signature
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
