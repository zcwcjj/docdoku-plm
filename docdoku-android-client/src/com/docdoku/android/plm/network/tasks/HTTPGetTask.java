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

public class HTTPGetTask extends HTTPAsyncTask<String, Void> {
    private static final String LOG_TAG = "com.docdoku.android.plm.network.tasks.HTTPGetTask";

    public HTTPGetTask() {
        super();
    }

    public HTTPGetTask(HTTPTaskDoneListener httpTaskDoneListener) {
        super();
        setHTTPTaskDoneListener(httpTaskDoneListener);
    }

    public HTTPGetTask(Session session) {
        super(session);
    }

    public HTTPGetTask(Session session, HTTPTaskDoneListener httpTaskDoneListener) {
        super(session);
        setHTTPTaskDoneListener(httpTaskDoneListener);
    }

    @Override
    protected HTTPResultTask doInBackground(String... strings) {
        HTTPResultTask resultTask = new HTTPResultTask();
        HttpURLConnection conn = null;
        try {
            String url = strings[0];
            Log.i(LOG_TAG, "Sending HttpGet request to url: " + url);

            conn = getHttpUrlConnection(url);
            conn.setRequestMethod("GET");
            conn.connect();

            resultTask.setHttpURLConnection(conn);

            Log.i(LOG_TAG, "Response code: " + resultTask.getResponseCode());
            if (resultTask.isSucceed()) {
                Log.i(LOG_TAG, "Response headers: " + resultTask.getHeaderFields());
                Log.i(LOG_TAG, "Response message: " + resultTask.getResponseMessage());
                Log.i(LOG_TAG, "Response content: " + resultTask.getResultContent());
            }


        }
        catch (MalformedURLException e) {
            Log.e(LOG_TAG, "ERROR: MalformedURLException");
//            result = ERROR_URL; //TODO l10n externalization
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
        catch (ArrayIndexOutOfBoundsException e) {
            Log.e(LOG_TAG, "ERROR: No Url provided for the Get query");
        }
        catch (URISyntaxException e) {
            Log.e(LOG_TAG, "URISyntaxException message: " + e.getMessage());
        }
        catch (NullPointerException e) {
            Log.e(LOG_TAG, "NullPointerException when connection to server");
        }
        finally{
            conn.disconnect();
        }

        return resultTask;

    }
}