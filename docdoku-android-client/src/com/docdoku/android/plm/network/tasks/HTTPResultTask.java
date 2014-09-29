
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Created by G. BOTTIEAU on 25/08/14.
 */
public class HTTPResultTask {
    private static final String  LOG_TAG          = "com.docdoku.android.plm.network.tasks.HTTPResultTask";
    public static final  int     NO_RESPONSE_CODE = -1;
    public static final  String  NO_RESPONSE_MSG  = "Connection did not start";
    private              int     responseCode     = NO_RESPONSE_CODE;
    private              String  responseMessage  = NO_RESPONSE_MSG;
    private              boolean succeed          = false;
    private              String  resultContent    = null;

    private Map<String, List<String>> headerFields;
    private HttpURLConnection         httpURLConnection;
    private String                    errorMsg;


    public HTTPResultTask() {
    }

    public void setHttpURLConnection(HttpURLConnection httpURLConnection) throws IOException {
        this.httpURLConnection = httpURLConnection;
        responseCode = httpURLConnection.getResponseCode();
        responseMessage = httpURLConnection.getResponseMessage();
        headerFields = httpURLConnection.getHeaderFields();

        succeed = (responseCode == HttpURLConnection.HTTP_OK);

        if(!succeed)
            errorMsg = httpURLConnection.getHeaderField("Reason-Phrase");
    }

    public String getResultContent() {
        if (resultContent == null) {
            if (succeed) {
                try {
                    InputStream ips = (InputStream) httpURLConnection.getContent();
                    if (ips != null) {
                        resultContent = inputStreamToString(ips);
                    }
                }
                catch (IOException e) {
                    resultContent = responseMessage;
                }
            }
            if (resultContent == null) {
                resultContent = responseMessage;
            }
        }

        return resultContent;
    }

    private String inputStreamToString(InputStream in) {
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

    public boolean isSucceed() {
        return succeed;
    }

    public Map<String, List<String>> getHeaderFields() {
        return headerFields;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    @Override
    public String toString() {
        return "Code:" + responseCode + " " + responseMessage;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
