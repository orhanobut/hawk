package com.orhanobut.hawk.dagger.sample

import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.orhanobut.hawk.dagger.Hawker
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

@VisibleForTesting
class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var hawker: Hawker

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector() = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        save()
        load()
    }

    private fun save() {
        hawker.put("name", "Hawker")
        hawker.put("description", "Hawk with Dagger 2 support")
    }

    private fun load() {
        val name: String? = hawker.get("name")
        val description: String? = hawker.get("description")
    }
}
