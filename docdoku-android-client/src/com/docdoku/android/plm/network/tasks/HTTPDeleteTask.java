/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.android.plm.network.tasks;

import android.util.Log;
import com.docdoku.android.plm.client.Session;
import com.docdoku.android.plm.network.tasks.listeners.HTTPTaskDoneListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URISyntaxException;

/**
 * Sends an Http DELETE request to the server.
 * <p>The constructor requires a {@link com.docdoku.android.plm.network.tasks.listeners.HTTPTaskDoneListener} to notify the result of the request.
 * <p>The host's url path is specified in the first <code>String</code> parameter in the <code>execute()</code> method.
 *
 * @version 1.0
 * @author: Martin Devillers
 */
public class HTTPDeleteTask extends HTTPAsyncTask<String, Void> {
    private static final String LOG_TAG = "com.docdoku.android.plm.network.tasks.HTTPDeleteTask";

    public HTTPDeleteTask() {
        super();
    }

    public HTTPDeleteTask(HTTPTaskDoneListener httpTaskDoneListener) {
        super();
        setHTTPTaskDoneListener(httpTaskDoneListener);
    }

    public HTTPDeleteTask(Session session) {
        super(session);
    }

    public HTTPDeleteTask(Session session, HTTPTaskDoneListener httpTaskDoneListener) {
        super(session);
        setHTTPTaskDoneListener(httpTaskDoneListener);
    }


    @Override
    protected HTTPResultTask doInBackground(String... strings) {
        HTTPResultTask resultTask = new HTTPResultTask();
        try {
            String url = strings[0];
            Log.i(LOG_TAG, "Sending Http request to URL: " + url);

            HttpURLConnection conn = getHttpUrlConnection(url);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(true);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");

            conn.connect();
            resultTask.setHttpURLConnection(conn);

            Log.i(LOG_TAG, "Response code: " + resultTask.getResponseCode());
            if (resultTask.isSucceed()) {
                Log.i(LOG_TAG, "Response headers: " + resultTask.getHeaderFields());
                Log.i(LOG_TAG, "Response message: " + resultTask.getResponseMessage());
                Log.i(LOG_TAG, "Response content: " + resultTask.getResultContent());
            }

            conn.disconnect();
        }
        catch (UnsupportedEncodingException | FileNotFoundException | URISyntaxException e) {
            e.printStackTrace();
        }
        catch (ProtocolException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return resultTask;
    }
}
