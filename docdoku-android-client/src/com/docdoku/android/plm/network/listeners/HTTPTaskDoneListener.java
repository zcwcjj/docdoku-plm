package com.docdoku.android.plm.network.listeners;

import com.docdoku.android.plm.network.HTTPResultTask;

/**
 * Created by G. BOTTIEAU on 21/08/14.
 */
public interface HTTPTaskDoneListener {
    void onDone(HTTPResultTask result);
}