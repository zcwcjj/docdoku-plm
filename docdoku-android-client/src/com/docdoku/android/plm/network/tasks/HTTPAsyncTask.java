package com.docdoku.android.plm.network.tasks;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import com.docdoku.android.plm.client.Session;
import com.docdoku.android.plm.network.tasks.listeners.HTTPTaskBeforeStartListener;
import com.docdoku.android.plm.network.tasks.listeners.HTTPTaskCanceledListener;
import com.docdoku.android.plm.network.tasks.listeners.HTTPTaskDoneListener;
import com.docdoku.android.plm.network.tasks.listeners.HTTPTaskProgressListener;

import java.io.*;
import java.net.*;

/**
 * Created by G. BOTTIEAU on 21/08/14.
 */
public abstract class HTTPAsyncTask<Params, Progress> extends AsyncTask<Params, Progress, HTTPResultTask> {

    public static final  String ERROR_UNKNOWN           = "Connection Error";
    public static final  String ERROR_URL               = "Error on URL";
    public static final  String ERROR_HTTP_BAD_REQUEST  = "Http Bad request";
    public static final  String ERROR_HTTP_UNAUTHORIZED = "Http unauthorized";
    private static final String LOG_TAG                 = "com.docdoku.android.plm.network.tasks.HTTPAsyncTask";
    private final static int    CHUNK_SIZE              = 1024 * 8;
    private final static int    BUFFER_CAPACITY         = 1024 * 32;

    private static String host;
    private static int    port;
    private static byte[] id;

    private HTTPTaskBeforeStartListener httpTaskBeforeStartListener;
    private HTTPTaskDoneListener        httpTaskDoneListener;
    private HTTPTaskCanceledListener    httpTaskCanceledListener;
    private HTTPTaskProgressListener    httpTaskProgressListener;


    HTTPAsyncTask() {
        this(null);
    }

    HTTPAsyncTask(Session session) {
        if (session == null) {
            try {
                session = Session.getSession();
            }
            catch (Session.SessionLoadException e) {
                Log.e(LOG_TAG, "Error : Session.SessionLoadException : " + e.getMessage());
            }
        }


        Log.i(LOG_TAG, "attempting to retrieve server connection information from memory...");
        host = session.getHost();
        port = session.getPort();
        try {
            id = Base64.encode((session.getUserLogin() + ":" + session.getPassword()).getBytes("ISO-8859-1"), Base64.DEFAULT);
        }
        catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "Error : UnsupportedEncodingException : " + e.getMessage());
        }

        Log.i(LOG_TAG, "All the server connection information is correctly available : " + host + ":" + port);
    }

    protected boolean isReachable() {
        boolean reachable = false;
        try {
            if (host != null)
                reachable = InetAddress.getByName(host).isReachable(5000);
        }
        catch (UnknownHostException e) {

        }
        catch (IOException e) {

        }
        return reachable;
    }

    HttpURLConnection getHttpUrlConnection(String path) throws IOException, URISyntaxException {
        URL url = createURL(path);

        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Authorization", "Basic " + new String(id, "US-ASCII"));

        httpURLConnection.setConnectTimeout(8000 /* milliseconds */);

        return httpURLConnection;
    }

    URL createURL(String path) throws URISyntaxException, MalformedURLException {
        String uriPath = path.replace(" ", "%20");
        URI uri = new URI(uriPath);
        String ASCIIPath = uri.toASCIIString();
        Log.i(LOG_TAG, "Parameters for Http connection: " +
                "\n Host: " + host +
                "\n Port: " + port +
                "\n Path: " + path);
        if (port == -1) {
            return new URL("http", host, ASCIIPath);
        }
        return new URL("http", host, port, ASCIIPath);
    }

    /**
     * Writes a {@code byte[]} to an {@code HttpURLConnection}
     * <p>Creates an {@code OutputStream} and writes the {@code InputStream} created from the {@code byte[]} onto it.
     *
     * @param connection the Http Connection, already opened
     * @param bytes      the {@code byte[]} to write on the Http conection
     * @throws IOException
     */
    void writeBytesToConnection(HttpURLConnection connection, byte[] bytes) throws IOException {
        OutputStream out = new BufferedOutputStream(connection.getOutputStream(), BUFFER_CAPACITY);
        InputStream inputStream = new ByteArrayInputStream(bytes);
        byte[] data = new byte[CHUNK_SIZE];
        int length;
        while ((length = inputStream.read(data)) != -1) {
            out.write(data, 0, length);
        }
        out.flush();
        out.close();
    }


    /**
     * Reads an {@code InputStream}, writing it to a {@code String}
     *
     * @param in the {@code InputStream} to read
     * @return the {@code String} read from the {@code InputStream}
     * @throws IOException
     */
    protected String inputStreamToString(InputStream in) {
        BufferedReader bf = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bf.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "ERROR: IOException in inputStreamToString method while reading");
        }
        finally {
            try {
                in.close();
                bf.close();
            }
            catch (IOException e) {
                String msg = e.getMessage();
                Log.e(LOG_TAG, "Close ERROR: IOException in inputStreamToString method" + ((msg != null) ? " : " + msg : ""));
            }
        }
        return sb.toString();
    }

    public void setHTTPTaskBeforeStartListener(HTTPTaskBeforeStartListener httpTaskBeforeStartListener) {
        this.httpTaskBeforeStartListener = httpTaskBeforeStartListener;
    }

    public void setHTTPTaskProgressListener(HTTPTaskProgressListener httpTaskProgressListener) {
        this.httpTaskProgressListener = httpTaskProgressListener;
    }

    public void setHTTPTaskCanceledListener(HTTPTaskCanceledListener httpTaskCanceledListener) {
        this.httpTaskCanceledListener = httpTaskCanceledListener;
    }

    void setHTTPTaskDoneListener(HTTPTaskDoneListener httpTaskDoneListener) {
        this.httpTaskDoneListener = httpTaskDoneListener;
    }

    @Override
    protected void onPreExecute() {
        if (httpTaskBeforeStartListener != null) {
            httpTaskBeforeStartListener.onStart();
        }
    }

    @Override
    protected void onPostExecute(HTTPResultTask result) {
        if (httpTaskDoneListener != null) {
            httpTaskDoneListener.onDone(result);
        }
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        if (httpTaskProgressListener != null) {
            httpTaskProgressListener.onProgress(values);
        }
    }

    @Override
    protected void onCancelled() {
        if (httpTaskCanceledListener != null) {
            httpTaskCanceledListener.onCancel();
        }
    }
}