package com.docdoku.android.plm.network.tasks.listeners;

import com.docdoku.android.plm.network.tasks.HTTPResultTask;

/**
 * Created by G. BOTTIEAU on 21/08/14.
 */
public interface HTTPTaskDoneListener {
    void onDone(HTTPResultTask result);
}