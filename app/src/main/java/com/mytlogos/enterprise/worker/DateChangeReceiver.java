package com.mytlogos.enterprise.worker;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mytlogos.enterprise.background.RepositoryImpl;

public class DateChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_DATE_CHANGED.equals(intent.getAction())) {
            if (!(context instanceof Application)) {
                System.out.println("Context not instance of Application");
                return;
            }
            RepositoryImpl.Companion.getInstance((Application) context).removeOldNews();
        }
    }
}
