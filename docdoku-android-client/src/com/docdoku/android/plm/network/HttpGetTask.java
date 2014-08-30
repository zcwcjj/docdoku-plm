///*
//* DocDoku, Professional Open Source
//* Copyright 2006 - 2013 DocDoku SARL
//*
//* This file is part of DocDokuPLM.
//*
//* DocDokuPLM is free software: you can redistribute it and/or modify
//* it under the terms of the GNU Affero General Public License as published by
//* the Free Software Foundation, either version 3 of the License, or
//* (at your option) any later version.
//*
//* DocDokuPLM is distributed in the hope that it will be useful,
//* but WITHOUT ANY WARRANTY; without even the implied warranty of
//* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//* GNU Affero General Public License for more details.
//*
//* You should have received a copy of the GNU Affero General Public License
//* along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
//*/
//
//package com.docdoku.android.plm.network;
//
//import android.util.Base64;
//import android.util.Log;
//import com.docdoku.android.plm.client.Session;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.UnsupportedEncodingException;
//import java.net.*;
//
///**
//* Sends an Http GET request to the server, receiving a simple <code>String</code> result.
//* <p>The constructor requires a {@link HttpGetListener} to notify the result of the request.
//* <p>The host's url path is specified in the first <code>String</code> parameter in the <code>execute()</code> method.
//* <p>
//* <p>This method has a second constructors with a <code>Session</code> parameter, that defines the server connection
//* information for all Http methods.
//*
//* @author: Martin Devillers
//* @version 1.0
//*/
//public class HttpGetTask extends HttpTask<String, Void, String>{
//    private static final String LOG_TAG = "com.docdoku.android.plm.network.HttpGetTask";
//
//    private HttpGetListener httpGetListener;
//
//    public HttpGetTask(HttpGetListener httpGetListener){
//        super();
//        this.httpGetListener = httpGetListener;
//    }
//
//    public HttpGetTask(Session session, HttpGetListener httpGetListener) throws UnsupportedEncodingException {
//        host = session.getHost();
//        port = session.getPort();
//        id = Base64.encode((session.getUserLogin() + ":" + session.getPassword()).getBytes("ISO-8859-1"), Base64.DEFAULT);
//        this.httpGetListener = httpGetListener;
//    }
//
//    @Override
//    protected String doInBackground(String... strings) {
//        String result = ERROR_UNKNOWN;
//        try {
//            Log.i(LOG_TAG,"Sending HttpGet request to url: " + strings[0]);
//
//            HttpURLConnection conn = getHttpUrlConnection(strings[0]);
//            conn.setRequestMethod("GET");
//            conn.connect();
//
//            int responseCode = conn.getResponseCode();
//            Log.i(LOG_TAG,"Response code: " + responseCode);
//            if (responseCode == 200){
//                InputStream in = (InputStream) conn.getContent();
//                result = inputStreamToString(in);
//                Log.i(LOG_TAG, "Response headers: " + conn.getHeaderFields());
//                Log.i(LOG_TAG, "Response message: " + conn.getResponseMessage());
//                Log.i(LOG_TAG, "Response content: " + result);
//            }else{
////                result = analyzeHttpErrorCode(responseCode);
//                result = conn.getResponseMessage();
//            }
//
//            conn.disconnect();
//        } catch (MalformedURLException e) {
//            Log.e(LOG_TAG,"ERROR: MalformedURLException");
//            result = ERROR_URL;
//        } catch (ProtocolException e) {
//            Log.e(LOG_TAG,"ERROR: ProtocolException");
//        } catch (UnsupportedEncodingException e) {
//            Log.e(LOG_TAG, "ERROR: UnsupportedEncodingException");
//        } catch (IOException e) {
//            Log.e(LOG_TAG,"ERROR: IOException");
//            Log.e(LOG_TAG, "Exception message: " + e.getMessage());
//        } catch (ArrayIndexOutOfBoundsException e){
//            Log.e(LOG_TAG, "ERROR: No Url provided for the Get query");
//        } catch (URISyntaxException e) {
//            Log.e(LOG_TAG, "URISyntaxException message: " + e.getMessage());
//        }catch (NullPointerException e){
//            Log.e(LOG_TAG, "NullPointerException when connection to server");
//        }
//
//        return result;
//    }
//
//    @Override
//    protected void onPostExecute(String result){
//        super.onPostExecute(result);
//        if (httpGetListener != null){
//            httpGetListener.onHttpGetResult(result);
//        }
//    }
//
//    /**
//     *
//     * @author: Martin Devillers
//     */
//    public static interface HttpGetListener {
//        void onHttpGetResult(String result);
//    }
//}
