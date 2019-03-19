package com.mytlogos.enterprise.background;

import android.os.AsyncTask;

public class WorkAsyncTask extends AsyncTask<Void, Void, Void> {

    private final Work work;
    private final DatabaseStorage storage;

    public WorkAsyncTask(Work work, DatabaseStorage storage) {
        this.work = work;
        this.storage = storage;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        work.work();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        storage.setLoading(false);
    }

    @Override
    protected void onCancelled() {
        storage.setLoading(false);
    }
}
