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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;

/**
 * Sends an Http PUT request to the server.
 * <p>The constructor requires a {@link HTTPTaskDoneListener} to notify the result of the request.
 * <p>The host's url path is specified in the first <code>String</code> parameter in the <code>execute()</code> method.
 * <p>If a request body is to be sent, it is passed as the second parameter in the <code>execute()</code> method.
 *
 * @version 1.0
 * @author: Martin Devillers
 */
public class HTTPPutTask extends HTTPAsyncTask<String, Void> {
    private static final String LOG_TAG = "com.docdoku.android.plm.network.tasks.HTTPPutTask";

    public HTTPPutTask() {
        super();
    }

    public HTTPPutTask(HTTPTaskDoneListener httpTaskDoneListener) {
        super();
        setHTTPTaskDoneListener(httpTaskDoneListener);
    }

    public HTTPPutTask(Session session) {
        super(session);
    }

    public HTTPPutTask(Session session, HTTPTaskDoneListener httpTaskDoneListener) {
        super(session);
        setHTTPTaskDoneListener(httpTaskDoneListener);
    }


    @Override
    protected HTTPResultTask doInBackground(String... strings) {
        HTTPResultTask resultTask = new HTTPResultTask();

        Log.i(LOG_TAG, strings[0]);

        try {
            String url = strings[0];

            Log.i(LOG_TAG, "Sending HttpPut request to url: " + url);

            HttpURLConnection conn = getHttpUrlConnection(url);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("PUT");

            byte[] msgBytes = null;
            try {
                String msg = strings[1];
                msgBytes = msg.getBytes();
                Log.i(LOG_TAG, "Message found attached to put request: " + msg);
                //conn.setRequestProperty("Content-Length", Integer.toString(msg.getBytes().length));
                conn.setFixedLengthStreamingMode(msgBytes.length);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setAllowUserInteraction(true);
                conn.setRequestProperty("Connection", "keep-alive");
            }
            catch (ArrayIndexOutOfBoundsException e) {
                Log.i(LOG_TAG, "No msg attached to HttpPut request");
            }
            conn.connect();

            if (msgBytes != null) {
                writeBytesToConnection(conn, msgBytes);
            }

            resultTask.setHttpURLConnection(conn);

            Log.i(LOG_TAG, "Response code: " + resultTask.getResponseCode());
            if (resultTask.isSucceed()) {
                Log.i(LOG_TAG, "Response headers: " + resultTask.getHeaderFields());
                Log.i(LOG_TAG, "Response message: " + resultTask.getResponseMessage());
                Log.i(LOG_TAG, "Response content: " + resultTask.getResultContent());
            }

            conn.disconnect();

        }
        catch (MalformedURLException e) {
            Log.e(LOG_TAG, "ERROR: MalformedURLException");
        }
        catch (ProtocolException e) {
            Log.e(LOG_TAG, "ERROR: ProtocolException");
        }
        catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "ERROR: UnsupportedEncodingException");
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "ERROR: IOException");
            Log.e(LOG_TAG, "Exception message: " + e.getMessage());
        }
        catch (URISyntaxException e) {
            Log.e(LOG_TAG, "ERROR: URISyntaxException");
        }
        return resultTask;
    }

}
