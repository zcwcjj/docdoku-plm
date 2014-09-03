package com.docdoku.android.plm.network;

import android.util.Log;
import com.docdoku.android.plm.client.Session;
import com.docdoku.android.plm.network.listeners.HTTPTaskDoneListener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.util.Date;

public class HTTPDownloadTask extends HTTPAsyncTask<String, Integer> {
    private static final String LOG_TAG = "com.docdoku.android.plm.network.HTTPDownloadTask";

    private int headerFileSize = -1;

    public HTTPDownloadTask() {
        super();
    }

    public HTTPDownloadTask(HTTPTaskDoneListener httpTaskDoneListener) {
        super();
        setHTTPTaskDoneListener(httpTaskDoneListener);
    }

    public HTTPDownloadTask(Session session) {
        super(session);
    }

    public HTTPDownloadTask(Session session, HTTPTaskDoneListener httpTaskDoneListener) {
        super(session);
        setHTTPTaskDoneListener(httpTaskDoneListener);
    }

    @Override
    protected HTTPResultTask doInBackground(String... strings) {
//        String result = ERROR_UNKNOWN;
        HTTPResultTask resultTask = new HTTPResultTask();
        try {
            String url = strings[0];
            String destination = strings[1];
            String fileName = strings[2];

            Log.i(LOG_TAG, "Sending HttpGet request to url: " + url + "\n" + destination + "\n" + fileName);

            HttpURLConnection conn = getHttpUrlConnection(url);
            conn.setRequestMethod("GET");
            conn.connect();

            headerFileSize = conn.getContentLength();

            File folders = new File(destination);

            if (!folders.exists()) {
                if (!folders.mkdirs()) {
                    throw new IOException("Cannot create path : " + folders.getAbsolutePath());
                }
            }

            File sdcardFile = new File(folders, fileName);

            boolean startDL = true;
            long lastModifiedHeaderDate = new Date(conn.getHeaderFieldDate("Last-Modified", sdcardFile.lastModified())).getTime();
            if (sdcardFile.exists()) {

                // if file size and datetime are equal server values : skip this download
                if ((sdcardFile.length() == headerFileSize) && (lastModifiedHeaderDate == sdcardFile.lastModified())) {
                    startDL = false;
                }
            }

            if (startDL) {
                //this will be used to write the downloaded data into the file we created
                FileOutputStream fos = new FileOutputStream(sdcardFile);
                InputStream inputStream = conn.getInputStream();

                //create a buffer
                byte[] buffer = new byte[2048];
                int bufferLength = 0; //used to store a temporary size of the buffer

                //variable to store total downloaded bytes
                int downloadedSize = 0;

                //now, read through the input buffer and write the contents to the file
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;

                    // update progressbar
                    publishProgress(downloadedSize, headerFileSize);
                }
                sdcardFile.setLastModified(lastModifiedHeaderDate);
            }

            resultTask.setHttpURLConnection(conn);

            Log.i(LOG_TAG, "Response code: " + resultTask.getResponseCode());
            if (resultTask.isSucceed()) {
                Log.i(LOG_TAG, "Response headers: " + resultTask.getHeaderFields());
                Log.i(LOG_TAG, "Response message: " + resultTask.getResponseMessage());
//                Log.i(LOG_TAG, "Response content: " + resultTask.getResultContent());
            }

            conn.disconnect();
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

        return resultTask;

    }
}