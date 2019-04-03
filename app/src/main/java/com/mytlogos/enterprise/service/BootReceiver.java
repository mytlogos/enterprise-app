package com.mytlogos.enterprise.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        JobScheduler service = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            context.startService(new Intent(context, SynchronizeService.class));
            AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);

            if (alarm == null) {
                throw new NullPointerException("alarm is null");
            }

            alarm.set(
                    // This alarm will wake up the device when System.currentTimeMillis()
                    // equals the second argument value
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + (1000 * 60 * 60), // One hour from now
                    // PendingIntent.getService creates an Intent that will start a service
                    // when it is called. The first argument is the Context that will be used
                    // when delivering this intent. Using this has worked for me. The second
                    // argument is a request code. You can use this code to cancel the
                    // pending intent if you need to. Third is the intent you want to
                    // trigger. In this case I want to create an intent that will start my
                    // service. Lastly you can optionally pass flags.
                    PendingIntent.getService(context, 0, new Intent(context, SynchronizeService.class), 0)
            );
        }
    }
}
