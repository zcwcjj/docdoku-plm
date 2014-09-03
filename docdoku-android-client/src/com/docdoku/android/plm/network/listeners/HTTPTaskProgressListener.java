package com.docdoku.android.plm.network.listeners;

/**
 * Created by G. BOTTIEAU on 21/08/14.
 */
public interface HTTPTaskProgressListener<Progress> {
    void onProgress(Progress... progress);
}
