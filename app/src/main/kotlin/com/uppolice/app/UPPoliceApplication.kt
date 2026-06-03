package com.uppolice.app

import android.app.Application
import com.google.android.material.color.DynamicColors

class UPPoliceApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
