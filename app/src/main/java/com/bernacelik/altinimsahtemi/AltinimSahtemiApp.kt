package com.bernacelik.altinimsahtemi

import android.app.Application
import com.google.firebase.FirebaseApp

class AltinimSahtemiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
