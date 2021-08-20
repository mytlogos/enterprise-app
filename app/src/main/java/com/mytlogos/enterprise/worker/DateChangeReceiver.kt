package com.mytlogos.enterprise.worker

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mytlogos.enterprise.background.repository.NewsRepository

class DateChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_DATE_CHANGED == intent.action) {
            if (context !is Application) {
                println("Context not instance of Application")
                return
            }
            NewsRepository.getInstance(context).removeOldNews()
        }
    }
}