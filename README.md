[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Hawk-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1568)      [![API](https://img.shields.io/badge/API-10%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=10)   [![Join the chat at https://gitter.im/orhanobut/hawk](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/orhanobut/hawk?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)  [![](https://img.shields.io/badge/AndroidWeekly-%23141-blue.svg)](http://androidweekly.net/issues/issue-141) <a href="http://www.methodscount.com/?lib=com.orhanobut%3Ahawk%3A2.0.0%2B"><img src="https://img.shields.io/badge/Methods and size-core: 188 | deps: 1242 | 21 KB-e91e63.svg"/></a> [![Build Status](https://travis-ci.org/orhanobut/hawk.svg?branch=master)](https://travis-ci.org/orhanobut/hawk)

<img align='right' src='https://github.com/orhanobut/hawk/blob/master/art/hawk-logo.png' width='128' height='128'/>

### Hawk 2.0
Secure, simple key-value storage for android

#### Important Note
This version has no backward compatibility with Hawk 1+ versions. If you still want to use old versions, [check here](https://github.com/orhanobut/hawk/tree/hawk1)

### Download
```groovy
compile "com.orhanobut:hawk:2.0.1"
```

### Initialize
```java
Hawk.init(context).build();
```
### Usage
Save any type (Any object, primitives, lists, sets, maps ...)
```java
Hawk.put(key, T);
```
Get the original value with the original type
```java
T value = Hawk.get(key);
```
Delete any entry
```java
Hawk.delete(key);
```
Check if any key exists
```java
Hawk.contains(key);
```
Check total entry count
```java
Hawk.count();
```
Get crazy and delete everything
```java
Hawk.deleteAll();
```

### How does Hawk work?

<img src='https://github.com/orhanobut/hawk/blob/master/art/how-hawk-works.png'/>

### More options
- Everything is pluggable, therefore you can change any layer with your custom implementation.
- NoEncryption implementation is provided out of box If you want to disable crypto.
```java
Hawk.init(context)
  .setEncryption(new NoEncryption())
  .setLogInterceptor(new MyLogInterceptor())
  .setConverter(new MyConverter())
  .setParser(new MyParser())
  .setStorage(new MyStorage())
  .build();
```

### License
<pre>
Copyright 2016 Orhan Obut

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


