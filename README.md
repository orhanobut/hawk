[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Hawk-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1568)      [![API](https://img.shields.io/badge/API-8%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=8)   [![Join the chat at https://gitter.im/orhanobut/hawk](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/orhanobut/hawk?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)  [![](https://img.shields.io/badge/AndroidWeekly-%23141-blue.svg)](http://androidweekly.net/issues/issue-141)

<img align='right' src='https://github.com/orhanobut/hawk/blob/master/images/hawk-logo.png' width='128' height='128'/>

###Hawk
Secure, simple key-value storage for android

Hawk uses:
- AES for the crypto
- SharedPreferences or Sqlite for the storage
- Gson for parsing

Hawk provides:
- Secure data persistence
- Save any type

###Add dependency
https://jitpack.io/#orhanobut/hawk/1.19
```groovy
repositories {
  // ...
  maven { url "https://jitpack.io" }
}

dependencies {
  compile 'com.github.orhanobut:hawk:1.19'
}
```

If you want to have Rx features, make sure to add Rx dependency

#### Initialize the hawk
```java
Hawk.init(this)
    .setEncryptionMethod(HawkBuilder.EncryptionMethod.MEDIUM)
    .setStorage(HawkBuilder.newSqliteStorage(this))
    .setLogLevel(LogLevel.FULL)
    .build();
```

or use buildRx to add init to your rx stream
```java
.buildRx().
```

You can use highest secure crypto approach, init might take 36-400ms. You also need to provide password
```java
Hawk.init(this)
    .setEncryptionMethod(HawkBuilder.EncryptionMethod.HIGHEST)
    .setStorage(HawkBuilder.newSqliteStorage(this))
    .setLogLevel(LogLevel.FULL)
    .build();
```

You can use no-crypto mode if you don't want encryption. This mode will be automatically used if the device does not
support AES, PBE algorithm.
```java
Hawk.init(context)
    .setEncryptionMethod(HawkBuilder.EncryptionMethod.NO_ENCRYPTION)
    .build();
```

Select the storage, you can either use sharedpreferences or sqlite to store data
```java
.setStorage(HawkBuilder.newSqliteStorage(this))
```
or
```java
.setStorage(HawkBuilder.newSharedPrefStorage(this))
```

You may want to use async solution for init. Add a callback to init and it will work asynchronous.
```java
Hawk.init(this)
    .setEncryptionMethod(HawkBuilder.EncryptionMethod.HIGHEST)
    .setPassword("password")
    .setStorage(HawkBuilder.newSqliteStorage(this))
    .setLogLevel(LogLevel.FULL)
    .setCallback(new HawkBuilder.Callback() {
      @Override
      public void onSuccess() {

      }

      @Override
      public void onFail(Exception e) {

      }
    })
    .build();
```

#### Save
put method accept any type such as list, map, primitive...
```java
Hawk.put(key, T); // Returns the result as boolean
```
You can also store multiple items at once by using chain feature. Remember to use commit() at the end. Either all of them will be saved or none.
```java
// Returns the result as boolean
Hawk.chain()
     .put(KEY_LIST, List<T>)
     .put(KEY_ANOTHER,"test")
     .commit();
```

#### Save (Rx)
```java
Observable<Boolean> result = Hawk.putObservable(key, T); // Returns the result as boolean
```

example usage
```java
Hawk.putObservable(KEY, new Foo())
    .observeOn(Schedulers.io())
    .subscribeOn(AndroidSchedulers.mainThread())
    .subscribe(new Subscriber<Boolean>() {
      @Override
      public void onCompleted() {
      }

      @Override
      public void onError(Throwable e) {
      }

      @Override
      public void onNext(Boolean s) {
      }
    });
```

#### Get
```java
T result = Hawk.get(key);
```
or with default value

```java
T result = Hawk.get(key, T);
```

#### Get Observable (Rx support)
To be able to use rx support, you need to add the dependency.
```java
Observable<T> result = Hawk.getObservable(key);
```
or with default value

```java
Observable<T> result = Hawk.getObservable(key, T);
```

example usage
```java
Hawk.<Foo>getObservable(KEY)
    .observeOn(Schedulers.io())
    .subscribeOn(AndroidSchedulers.mainThread())
    .subscribe(new Subscriber<Foo>() {
      @Override
      public void onCompleted() {
        Log.d("rxtest", "completed");
      }

      @Override
      public void onError(Throwable e) {
        Log.d("rxtest", "error");
      }

      @Override
      public void onNext(Foo s) {
        Log.d("rxtest", s.toString());
      }
    });
```

#### Remove
```java
Hawk.remove(key); // Returns the result as boolean
```
or you can remove multiple items at once
```java
Hawk.remove(KEY_LIST, KEY_NAME); // Returns the result as boolean
```
#### Contains
```java
boolean contains = Hawk.contains(key);
```

#### Set the log output (optional)
```java
Hawk.init(context,PASSWORD, LogLevel.FULL); // as default it is NONE
```

##### More samples for save

```java
Hawk.put("key", "something"); // Save string
Hawk.put("key", true); // save boolean
Hawk.put("key", new Foo()); // save an object
Hawk.put("key", List<String>); // save list
Hawk.put("key", List<Foo>); // save list
Hawk.put("key", Map<Foo,Foo>); // save map
Hawk.put("key", Set<Foo>); // save set
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
Map<String,Foo> value = Hawk.get(key);
Set<Foo> value = Hawk.get(key);
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

##### Benchmark result (ms)
Done with Nexus 4, Android L. Note that this is not certain values, I just made a few runs and show it to give you an idea.
<pre>
| Hawk (ms)        | init | security| Primitive | Object | List<T> |   Map   |   Set   |
|                  |      |  level  |  PUT/GET  | PUT/GET| PUT/GET | PUT/GET | PUT/SET |
|------------------|------|---------|-----------|--------|---------|---------|---------|
| With password    | 24   | high    | 26  | 2   | 14 | 1 | 20 | 36 | 12 | 4  | 15 | 3  |
| Without password | 16   | less    | 14  | 2   | 8  | 1 | 20 | 30 | 12 | 3  | 9  | 3  |
| No encryption    | 14   | none    | 9   | 2   | 8  | 1 | 20 | 30 | 11 | 3  | 9  | 3  |
| Prefs            | 5    | none    | 8   | 1   | 10 | 1 | 30 | 9  | 14 | 2  | 17 | 2  |
</pre>

##### How Hawk works

<img src='https://github.com/orhanobut/hawk/blob/master/images/flow-chart.png'/>

##### Notes
- Password should be provided by the user, we try to find better solution for this.
- Salt key is stored plain text in the storage currently. We are checking to find a better solution for this. Any contribution about this will be great help as well.

##### Credits
I use the following implementation for the crypto and I believe it should get more attention. Thanks for this great hard work. https://github.com/tozny/java-aes-crypto and a great article about it : http://tozny.com/blog/encrypting-strings-in-android-lets-make-better-mistakes/

#### Proguard
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
