/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("FoldInitializerAndIfToElvis")

package com.orhanobut.hawk.dagger.sample.di

import android.annotation.SuppressLint
import android.app.Application
import com.orhanobut.hawk.dagger.Hawker
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module()
class AppModule {
    @Singleton
    @Provides
    fun provideHawk(app: Application): Hawker = Hawker(app.baseContext)
}
