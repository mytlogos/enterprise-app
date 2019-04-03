package com.mytlogos.enterprise.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class SynchronizeService extends Service {

    public SynchronizeService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
