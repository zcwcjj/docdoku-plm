/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.android.plm.client;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.*;
import com.docdoku.android.plm.client.documents.Document;
import com.docdoku.android.plm.client.documents.DocumentActivity;
import com.docdoku.android.plm.network.tasks.HTTPDownloadTask;
import com.docdoku.android.plm.network.tasks.HTTPGetTask;
import com.docdoku.android.plm.network.tasks.HTTPPutTask;
import com.docdoku.android.plm.network.tasks.HTTPResultTask;
import com.docdoku.android.plm.network.tasks.listeners.HTTPTaskDoneListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Abstract class for <code>Activity</code> representing an <code>Element</code>'s data.
 * <p>Contains the methods used for operation that <code>Document</code>s and <code>Part</code>s have in common.
 *
 * @version 1.0
 * @author: Martin Devillers
 */
public abstract class ElementActivity extends SimpleActionBarActivity {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.ElementActivity";
    protected Switch                                 switchChechkInOut;
    private   Element                                element;
    private   boolean                                checkedIn;
    private   ProgressDialog                         loadingDialog;
    private   String                                 iterationNote;
    private   CompoundButton.OnCheckedChangeListener switchListener;

    protected BaseExpandableListAdapter adapter;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        switchListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkOutElement();
                }
                else {
                    checkInElement();
                }
            }
        };
    }

    /**
     * Obtains the instance of the <code>Element</code> that this <code>Activity</code> is presenting to the user
     *
     * @see android.app.Activity
     */
    @Override
    public void onResume() {
        super.onResume();
        element = getElement();
    }

    protected abstract Element getElement();

    protected View createHeaderView(ViewGroup header, Element document) {
        header = (ViewGroup) getLayoutInflater().inflate(R.layout.adapter_document_header, null);
//        TextView documentReference = (TextView) header.findViewById(R.id.documentIdentification);
//        documentReference.setText(document.getIdentification());


//        Switch notifyIteration = (Switch) header.findViewById(R.id.notifyIteration);
//        setNotifyIterationNotification(notifyIteration);
//        Switch notifyStateChange = (Switch) header.findViewById(R.id.notifyStateChange);
//        setNotifyStateChangeNotification(notifyStateChange);

        switchChechkInOut = (Switch) header.findViewById(R.id.checkInOutButton);
        if (document.getCheckOutUserLogin() != null) {
            if (getCurrentUserLogin().equals(document.getCheckOutUserLogin())) {
                setElementCheckedOutByCurrentUser(document.getCheckOutDate());
            }
            else {
//                switchChechkInOut.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_out_other_user_light, 0, 0);
//                switchChechkInOut.setClickable(false);
//                switchChechkInOut.setText(R.string.locked);
                switchChechkInOut.setVisibility(View.GONE);
            }
        }
        else {
            setElementCheckedIn();
        }

        switchChechkInOut.setOnCheckedChangeListener(switchListener);
        return header;
    }

    /**
     * Sets the <code>Element</code> checked in by the current user.
     * <p>Set the <code>switchChechkInOut OnClickListener</code>'s <code>onClick()</code> method to start the
     * {@link #checkOutElement()} method.
     */
    protected void setElementCheckedIn() {
        checkedIn = true;
        getElement().setCheckOutInformation(null, null, null);
    }

    private HTTPPutTask createTask() {
        HTTPPutTask task = new HTTPPutTask(new HTTPTaskDoneListener() {
            @Override
            public void onDone(HTTPResultTask result) {
                processResult(result);
            }
        });

        return task;
    }

    private void processResult(HTTPResultTask result) {
        Log.i(LOG_TAG, "Result of checkin/checkout: " + result.isSucceed());
        if (result.isSucceed()) {
            if (checkedIn) {
                setElementCheckedOutByCurrentUser();
                Toast.makeText(ElementActivity.this, R.string.dialog_check_out_successful, Toast.LENGTH_SHORT).show();
                try {
                    JSONObject responseJSON = new JSONObject(result.getResultContent());
                    element.updateFromJSON(responseJSON, getResources());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            else {
                setElementCheckedIn();
                Toast.makeText(ElementActivity.this, R.string.diaog_check_in_successful, Toast.LENGTH_SHORT).show();
                SimpleDateFormat dateFormat = new SimpleDateFormat(getResources().getString(R.string.simpleDateFormat));
                element.setLastIteration(element.iterationNumber, iterationNote, getCurrentUserName(), dateFormat.format(Calendar.getInstance().getTime()));
            }
            adapter.notifyDataSetChanged();
        }
        else {
            switchChechkInOut.setChecked(!switchChechkInOut.isChecked());
            String msg = result.getErrorMsg();
            if (msg == null)
                msg = getResources().getString(R.string.net_connection_error);

            new AlertDialog.Builder(ElementActivity.this)
                    .setIcon(R.drawable.ic_error_blue)
                    .setTitle(" ")
                    .setMessage(msg)
                    .setPositiveButton(R.string.OK, null)
                    .create().show();
        }
        switchChechkInOut.setOnCheckedChangeListener(switchListener);
    }

    /**
     * Attempts to check out the <code>Element</code> by the current user.
     * <p>Shows an <code>AlertDialog</code> to obtain confirmation that this is what the user wants to do.
     * If he confirms it, a new {@link HTTPPutTask} is started.
     */
    private void checkOutElement() {
        switchChechkInOut.setOnCheckedChangeListener(null);

        new AlertDialog.Builder(ElementActivity.this)
                .setIcon(R.drawable.ic_check_out_dark)
                .setTitle(R.string.dialog_check_out)
                .setMessage(R.string.dialog_check_out_confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createTask().execute(getUrlWorkspaceApi() + element.getUrlPath() + "/checkout/");
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switchChechkInOut.setChecked(!switchChechkInOut.isChecked());
                        switchChechkInOut.setOnCheckedChangeListener(switchListener);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        switchChechkInOut.setChecked(!switchChechkInOut.isChecked());
                        switchChechkInOut.setOnCheckedChangeListener(switchListener);
                    }
                })
                .create().show();
    }

    /**
     * Sets the <code>Element</code> checked out by the current user at the current time by calling the
     * {@link #setElementCheckedOutByCurrentUser(String) setElementCheckedOutByCurrentUser(String date)} method
     * with date set to current date.
     */
    protected void setElementCheckedOutByCurrentUser() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.simpleDateFormat));
        setElementCheckedOutByCurrentUser(simpleDateFormat.format(c.getTime()));
    }

    /**
     * Sets the <code>Element</code> checked out by the current user at the specified time.
     * <p>Set the <code>switchChechkInOut OnClickListener</code>'s <code>onClick()</code> method to start the
     * {@link #checkInElement()} method.
     *
     * @param date the checkout date
     */
    protected void setElementCheckedOutByCurrentUser(String date) {
        checkedIn = false;
        switchChechkInOut.setChecked(true);
        getElement().setCheckOutInformation(getCurrentUserName(), getCurrentUserLogin(), date);
//        switchChechkInOut.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                checkInElement();
//            }
//        });
    }

    /**
     * Attempts to check in the <code>Element</code> by the current user.
     * <p>Shows an <code>AlertDialog</code> to obtain confirmation that this is what the user wants to do and to allow
     * the user to add a revision note in an <code>EditText</code>.
     * If he confirm without a revision  it, a new {@link HTTPPutTask} is started to check in the <code>Element</code>.
     * If he confirms with a revision, a new {@link HTTPPutTask} is started to send the revision note to the server, and
     * if that task returns a positive result, then another task is started to do the checkin.
     */
    private void checkInElement() {
        switchChechkInOut.setOnCheckedChangeListener(null);

        final EditText iterationNoteField = new EditText(ElementActivity.this);
        new AlertDialog.Builder(ElementActivity.this)
                .setIcon(R.drawable.ic_check_in_dark)
                .setTitle(R.string.dialog_check_in)
                .setMessage(R.string.dialog_check_in_confirm)
                .setMessage(R.string.dialog_check_in_note)
                .setView(iterationNoteField)
                .setPositiveButton(R.string.dialog_do_check_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        iterationNote = iterationNoteField.getText().toString();
                        if (iterationNote.length() > 0) {
                            Log.i(LOG_TAG, "Iteration note for document checkin: " + iterationNote);
                            new HTTPPutTask(new HTTPTaskDoneListener() {
                                @Override
                                public void onDone(HTTPResultTask result) {
                                    if (result.isSucceed()) {
                                        Log.i(LOG_TAG, "Checking out document after successfully uploading iteration");
                                        createTask().execute("api/workspaces/" + getCurrentWorkspace() + element.getUrlPath() + "/checkin/");
                                    }
                                    else {
                                        processResult(result);
                                    }
                                }
                            }).execute(getUrlWorkspaceApi() + element.getUrlPath() + "/iterations/" + element.getIterationNumber(), element.getLastIterationJSONWithUpdateNote(iterationNote).toString());
                        }
                        else {
                            Log.i(LOG_TAG, "No iteration note was entered for document checkin");
                            createTask().execute(getUrlWorkspaceApi() + element.getUrlPath() + "/checkin/");
                        }
                    }
                })
                .setNeutralButton(R.string.dialog_cancel_check_out, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createTask().execute(getUrlWorkspaceApi() + element.getUrlPath() + "/undocheckout/");
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_action, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switchChechkInOut.setChecked(!switchChechkInOut.isChecked());
                        switchChechkInOut.setOnCheckedChangeListener(switchListener);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        switchChechkInOut.setChecked(!switchChechkInOut.isChecked());
                        switchChechkInOut.setOnCheckedChangeListener(switchListener);
                    }
                })
                .create().show();
    }

    /**
     * Inflates a layout for an attribute having a name and a value.
     * <p>Layout file: {@link /res/layout/adapter_name_value_pair.xml adapter_name_value_pair}
     *
     * @param name  The attribute's name
     * @param value The attribute's value
     * @return The <code>View</code>, which is a row presenting the name and value.
     */
    protected View createNameValuePairRowView(String name, String value) {
        View rowView = getLayoutInflater().inflate(R.layout.adapter_name_value_pair, null);
        ((TextView) rowView.findViewById(R.id.fieldName)).setText(name);
        ((TextView) rowView.findViewById(R.id.fieldValue)).setText(value);
        return rowView;
    }

    /**
     * Inflates a layout for an linked document, showing its id.
     * Sets the <code>OnClickListener</code> that starts the download of the document's information with an {@link HTTPGetTask}, then, on result,
     * start the {@link DocumentActivity} for it.
     * <p>Layout file: {@link /res/layout/adapter_document_simple.xml adapter_document_simple}
     *
     * @param linkedDocument the id of the linked document
     * @return The <code>View</code>, which is a row with the document's id
     */
    protected View createLinkedDocumentRowView(final String linkedDocument) {
        ViewGroup rowView = (ViewGroup) getLayoutInflater().inflate(R.layout.adapter_document_simple, null);
        TextView docView = (TextView) rowView.findViewById(R.id.docId);
        docView.setText(linkedDocument);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("com.docdoku.android.plm.client", "Following link to " + linkedDocument);
                HTTPGetTask task = new HTTPGetTask(new HTTPTaskDoneListener() {
                    @Override
                    public void onDone(HTTPResultTask result) {
                        try {
                            JSONObject jsonObject = new JSONObject(result.getResultContent());
                            Document document1 = new Document(jsonObject.getString("id"));
                            document1.updateFromJSON(jsonObject, getResources());
                            Intent intent = new Intent(ElementActivity.this, DocumentActivity.class);
                            intent.putExtra(DocumentActivity.EXTRA_DOCUMENT, document1);
                            startActivity(intent);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                task.execute(getUrlWorkspaceApi() + "/documents/" + linkedDocument);
            }
        });
        return rowView;
    }

    /**
     * Inflates a layout for an linked file, showing its name.
     * Sets the <code>OnClickListener</code> that starts the download of the file with an {@link com.docdoku.android.plm.network.tasks.HTTPDownloadTask}.
     * <p>Layout file: {@link /res/layout/adapter_dowloadable_file.xml adapter_downloadable_file}
     *
     * @param fileName the name of the downloadable file
     * @param fileUrl  the end of the url used to download the file
     * @return The <code>View</code>, which is a row with the file's name
     */
    protected View createFileRowView(final String fileName, final String fileUrl) {
        View rowView = getLayoutInflater().inflate(R.layout.adapter_dowloadable_file, null);
        TextView fileNameField = (TextView) rowView.findViewById(R.id.fileName);
        fileNameField.setText(fileName);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("com.docdoku.android.plm.client", "downloading file from path: " + fileUrl);

                final String dest = getExternalCacheDir() + "/" + fileUrl.replaceAll(fileName, "");

                // TODO : if file exists propose to check for update
                // TODO quick patch done : now factorize this code
                final File file = new File(dest + fileName);
                if(file.exists()){
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                    String type = mime.getMimeTypeFromExtension(ext);

                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), type);

                    startActivity(Intent.createChooser(intent, getResources().getString(R.string.dialog_choose_opening_app)));
                }
                else
                {
                    loadingDialog = new ProgressDialog(ElementActivity.this);
                    loadingDialog.setTitle(R.string.net_loading);
                    loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    loadingDialog.setIndeterminate(true);
                    loadingDialog.show();

                    new HTTPDownloadTask(new HTTPTaskDoneListener() {
                        @Override
                        public void onDone(HTTPResultTask result) {
                            loadingDialog.dismiss();
                            if (result.isSucceed()) {
                                Intent intent = new Intent();
                                intent.setAction(android.content.Intent.ACTION_VIEW);

//                                File file = new File(dest + fileName);

                                MimeTypeMap mime = MimeTypeMap.getSingleton();
                                String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                                String type = mime.getMimeTypeFromExtension(ext);

                                intent.setDataAndType(Uri.fromFile(file), type);

                                startActivity(Intent.createChooser(intent, getResources().getString(R.string.dialog_choose_opening_app)));
                            }
                            else {
                                Toast.makeText(ElementActivity.this, R.string.net_download_failed, Toast.LENGTH_LONG).show();
                            }
                        }
                    }).execute("files/" + fileUrl, dest, fileName);
                }


            }
        });
        return rowView;
    }

    /**
     * Inflates a layout for a row indicating a simple message.
     * <p>Layout file: {@link /res/layout/adapter_message.xml adapter_message}
     *
     * @param messageId the id of the <code>String</code> resource containing the message
     * @return The <code>View</code>, which is a row with the message
     */
    protected View createNoContentFoundRowView(int messageId) {
        View rowView = getLayoutInflater().inflate(R.layout.adapter_message, null);
        TextView message = (TextView) rowView.findViewById(R.id.message);
        message.setText(messageId);
        return rowView;
    }
}
