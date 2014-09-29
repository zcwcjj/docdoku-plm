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

package com.docdoku.android.plm.client.documents;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.docdoku.android.plm.client.Element;
import com.docdoku.android.plm.client.ElementActivity;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.network.tasks.HTTPPutTask;
import com.docdoku.android.plm.network.tasks.HTTPResultTask;
import com.docdoku.android.plm.network.tasks.listeners.HTTPTaskDoneListener;
import com.docdoku.android.plm.network.old.HttpPostUploadFileTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <code>Activity</code> presenting the details for a {@link Document} in the form of an <code>ExpandableListView</code>
 * <p>Layout file: {@link /res/layout/activity_element.xml activity_element}
 *
 * @version 1.0
 * @author: Martin Devillers
 */
public class DocumentActivity extends ElementActivity implements HttpPostUploadFileTask.HttpPostUploadFileListener {
    /**
     * Key for the <code>Parcelable Intent Extra</code> which is the <code>Document</code> represented in this <code>Activity</code>
     */
    public static final  String EXTRA_DOCUMENT                    = "document";
    private static final String LOG_TAG                           = "com.docdoku.android.plm.client.documents.DocumentActivity";
    private static final int    INTENT_CODE_ACTIVITY_PICTURE      = 0;
    private static final int    INTENT_CODE_ACTIVITY_VIDEO        = 1;
    private static final int    INTENT_CODE_ACTIVITY_FILE_CHOOSER = 2;
    private static final int    NUM_PAGES                         = 5;
    private static final int    NUM_GENERAL_INFORMATION_FIELDS    = 10;
    private static final int    NUM_REVISION_FIELDS               = 4;
    private static final int    NOTIFICATION_STATE_CHANGE         = 1;
    private static final int    NOTIFICATION_NEW_ITERATION        = 2;

    private Document           document;
    private ExpandableListView expandableListView;
    private String             pictureSavePath;
    private String             fileUploadUrl;
    private ProgressDialog     progressDialog;

    /**
     * Called when the <code>Activity</code> started by <code>startActivityForResult()</code> is finished.
     * The <code>Activity</code> started may have been to take a picture or to choose a file to upload.
     * If the <code>Activity</code> was successful:
     * <br>If the <code>Activity</code> was taking a picture, a <code>Toast</code> shows the user where the picture was saved.
     * An <code>AlertDialog</code> show a preview of the picture, allows the user to choose the name of the picture on the server, and
     * offers him to start the upload (starting an {@link HttpPostUploadFileTask}) or cancelling it.
     * <br>If the <code>Activity</code> was choosing a file, an {@link HttpPostUploadFileTask} is started to upload the file to
     * the server.
     *
     * @param requestCode the <code>Intent</code> code that was passed to the <code>Activity</code> to identify the request
     * @param resultCode  the result indicating a success or failure in the <code>Activity</code>
     * @param data        the data passed as a result by the <code>Activity</code>
     * @see android.app.Activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case INTENT_CODE_ACTIVITY_PICTURE:
                    Toast.makeText(this, getResources().getString(R.string.imageSavedIn) + pictureSavePath, Toast.LENGTH_LONG).show();
                    final View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload_picture, null);
                    Bitmap picture = BitmapFactory.decodeFile(pictureSavePath);
                    ((ImageView) dialogView.findViewById(R.id.image)).setImageBitmap(picture);
                    new AlertDialog.Builder(this)
                            .setIcon(R.drawable.ic_camera_blue)
                            .setTitle(" ")
                            .setView(dialogView)
                            .setCancelable(false)
                            .setPositiveButton(R.string.uploadImage, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String fileName = ((EditText) dialogView.findViewById(R.id.imageName)).getText().toString();
                                    if (fileName.length() == 0)
                                        fileName = "mobileImage" + new SimpleDateFormat("HH-mm-ss_MM-dd-yyyy").format(new Date());
                                    startUploadingFile(fileName + ".png", pictureSavePath);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create().show();
                    break;
                /*case INTENT_CODE_ACTIVITY_VIDEO:
                    Uri uri = data.getData();
                    final String path = getRealPathFromURI(uri);
                    String fileName  = path.substring(path.lastIndexOf("/")+1);
                    if (fileName.length() == 0){
                        fileName = "AndroidFile";
                    }
                    Log.i("com.docdoku.android.plm", "Uploading video named " + fileName + " from path: " + path);
                    Toast.makeText(this, getResources().getString(R.string.imageSavedIn) + pictureSavePath, Toast.LENGTH_LONG).show();
                    final View videoDialogView = getLayoutInflater().inflate(R.layout.dialog_upload_picture, null);
                    Bitmap video = BitmapFactory.decodeFile(path);
                    ((ImageView) videoDialogView.findViewById(R.id.image)).setImageBitmap(video);
                    new AlertDialog.Builder(this)
                            .setIcon(R.drawable.take_picture_light)
                            .setTitle(" ")
                            .setView(videoDialogView)
                            .setCancelable(false)
                            .setPositiveButton(R.string.uploadImage, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String fileName = ((EditText) videoDialogView.findViewById(R.id.imageName)).getText().toString();
                                    if (fileName.length() == 0) fileName = "mobileImage" + new SimpleDateFormat("HH-mm-ss_MM-dd-yyyy").format(new Date());
                                    startUploadingFile(fileName + ".png", path);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create().show();
                    break;*/
                case INTENT_CODE_ACTIVITY_FILE_CHOOSER:
                    Uri uri = data.getData();

                    Log.d(LOG_TAG, uri.toString());

                    String path = getRealPathFromURI(uri);
                    String fileName = path.substring(path.lastIndexOf("/") + 1);
                    if (fileName.length() == 0) {
                        fileName = "AndroidFile";
                    }
                    Log.i(LOG_TAG, "Uploading file named " + fileName + " from path: " + path);
                    startUploadingFile(fileName, path);
                    break;
            }
        }
        else if (resultCode == RESULT_CANCELED) {
            // User cancelled the image capture
        }
        else {
            Toast.makeText(this, R.string.mediaError, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Called on this <code>Activity</code>'s creation.
     * Extracts the <code>Document</code> from the <code>Intent</code> and then sets the <code>Adapter</code> for the
     * <code>ExpandableListView</code>. The first group (general information) of the <code>ExpandableListView</code> is
     * expanded here.
     *
     * @param savedInstanceState
     * @see android.app.Activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element);

        Intent intent = getIntent();
        document = (Document) intent.getSerializableExtra(EXTRA_DOCUMENT);

        Log.i(LOG_TAG, "starting activity for document with id: " + document.getIdentification());

        setTitle(getTitle() + " " + document.getIdentification());

        expandableListView = (ExpandableListView) findViewById(R.id.list);
        expandableListView.addHeaderView(createHeaderView((ViewGroup) getLayoutInflater().inflate(R.layout.adapter_document_header, null), document));
        createNotificationsTags();
        adapter = new DocumentDetailsExpandableListAdapter();
        expandableListView.setAdapter(adapter);
        expandableListView.expandGroup(0);
    }

    private void createNotificationsTags() {
        //TODO display informations about notifications
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.setGroupVisible(R.id.menu_subscriptions, true);

        MenuItem itemNotifIterations = menu.findItem(R.id.menu_subscribeIterationsNotification);
        MenuItem itemNotifStates = menu.findItem(R.id.menu_subscribeStateChangeNotification);
//        MenuItem itemShare = menu.findItem(R.id.menu_share);
        MenuItem itemLogout = menu.findItem(R.id.menu_logout);

        itemLogout.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        setNotifyIterationNotification(itemNotifIterations);
        setNotifyStateChangeNotification(itemNotifStates);

        return true;
    }


    private void setNotifyIterationNotification(final MenuItem menuItem) {
        menuItem.setChecked(document.getIterationNotification());
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // get opposite value (isChecked returns current value, not new value !)
                final boolean b = !item.isChecked();

                HTTPTaskDoneListener taskDoneListener = new HTTPTaskDoneListener() {
                    @Override
                    public void onDone(HTTPResultTask result) {
                        if (result.isSucceed()) {
                            if (b) {
                                Toast.makeText(DocumentActivity.this, R.string.documentIterationChangeNotificationSuccessfullyActivated, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(DocumentActivity.this, R.string.documentIterationChangeNotificationSuccessfullyDeactivated, Toast.LENGTH_SHORT).show();
                            }
                            document.setIterationNotification(b);
                            menuItem.setChecked(b);
                            adapter.notifyDataSetChanged();
                            createNotificationsTags();
                        }
                        else {
                            Toast.makeText(DocumentActivity.this, R.string.net_connection_error, Toast.LENGTH_LONG).show();
                        }
                    }
                };

                if (b) {
                    subscriptionChangeRequested(
                            R.drawable.ic_iteration_blue,
                            R.string.confirmSubscribeToIterationChangeNotification,
                            "iterationChange/subscribe",
                            menuItem,
                            taskDoneListener);
                }
                else {
                    subscriptionChangeRequested(
                            R.drawable.ic_iteration_blue,
                            R.string.confirmUnsubscribeToIterationChangeNotification,
                            "iterationChange/unsubscribe",
                            menuItem,
                            taskDoneListener);
                }
                return b;
            }
        });
    }

    private void setNotifyStateChangeNotification(final /*CompoundButton*/ MenuItem menuItem) {
        menuItem.setChecked(document.getStateChangeNotification());
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // get opposite value (isChecked returns current value, not new value !)
                final boolean b = !item.isChecked();

                HTTPTaskDoneListener taskDoneListener = new HTTPTaskDoneListener() {
                    @Override
                    public void onDone(HTTPResultTask result) {
                        if (result.isSucceed()) {
                            if (b) {
                                Toast.makeText(DocumentActivity.this, R.string.documentStateChangeNotificationSuccessfullyActivated, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(DocumentActivity.this, R.string.documentStateChangeNotificationSuccessfullyDeactivated, Toast.LENGTH_SHORT).show();
                            }
                            document.setStateChangeNotification(b);
                            menuItem.setChecked(b);

                            adapter.notifyDataSetChanged();

                            createNotificationsTags();
                        }
                        else {
                            Toast.makeText(DocumentActivity.this, R.string.net_connection_error, Toast.LENGTH_LONG).show();
                        }
                    }
                };

                if (b) {
                    subscriptionChangeRequested(
                            R.drawable.state_change_notification_on_light,
                            R.string.confirmSubscribeToStateChangeNotification,
                            "stateChange/subscribe",
                            menuItem,
                            taskDoneListener);
                }
                else {
                    subscriptionChangeRequested(
                            R.drawable.state_change_notification_on_light,
                            R.string.confirmUnsubscribeToStateChangeNotification,
                            "stateChange/unsubscribe",
                            menuItem,
                            taskDoneListener);

                }
                return true;
            }
        });
    }

    private void subscriptionChangeRequested(int iconId, int messageId, final String urlCommand, final MenuItem compoundButton/*final CompoundButton compoundButton*/, final HTTPTaskDoneListener taskDoneListener) {
        new AlertDialog.Builder(DocumentActivity.this)
                .setIcon(iconId)
                .setTitle(" ")
                .setMessage(messageId)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(LOG_TAG, "Subscribing to iteration change notification for document with reference " + document.getIdentification());
                        new HTTPPutTask(taskDoneListener).execute("api/workspaces/" + getCurrentWorkspace() + "/documents/" + document.getIdentification() + "/notification/" + urlCommand);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setOnCancelListener(null)
                .create().show();
    }

    private void startUploadingFile(String fileName, String filePath) {
        String docReference = document.getIdentification();
        String docId = docReference.substring(0, docReference.lastIndexOf("-"));
        String docVersion = docReference.substring(docReference.lastIndexOf("-") + 1);
        fileUploadUrl = "files/" + getCurrentWorkspace() + "/documents/" + docId + "/" + docVersion + "/" + document.getIterationNumber() + "/" + fileName;
        new HttpPostUploadFileTask(DocumentActivity.this).execute(fileUploadUrl, filePath);
    }

    private String getRealPathFromURI(Uri contentUri) throws IllegalArgumentException{
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
//        Cursor cursor = this.getContentResolver().query(contentUri, proj, null, null, null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        Log.d(LOG_TAG, cursor.getColumnCount() + "");

        return cursor.getString(column_index);
    }

    /**
     * Returns the id of the menu button leading to this activity
     *
     * @Override getActivityButtonId
     */
    @Override
    protected int getActivityButtonId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * @return
     * @see com.docdoku.android.plm.client.ElementActivity#getElement()
     */
    @Override
    protected Element getElement() {
        return document;
    }

    private View createUploadFileRowView() {
        View rowView = getLayoutInflater().inflate(R.layout.adapter_upload_file, null);
        ImageButton takePicture = (ImageButton) rowView.findViewById(R.id.takePicture);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String timeStamp = new SimpleDateFormat("HH-mm-ss_MM-dd-yyyy").format(new Date());
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "DocDokuPLM" + timeStamp + ".jpg");
                try {
                    file.createNewFile();
                    pictureSavePath = file.getAbsolutePath();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    startActivityForResult(intent, INTENT_CODE_ACTIVITY_PICTURE);
                }
                catch (IOException e) {
                    Toast.makeText(DocumentActivity.this, R.string.documentPictureDirectoryFail, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    Log.e(LOG_TAG, "IOException when creating file." +
                            "\nError message: " + e.getMessage() +
                            "\nError cause: " + e.getCause() +
                            "\nFile path" + file.getAbsolutePath());
                }
            }
        });
        /*ImageButton takeVideo = (ImageButton) rowView.findViewById(R.id.takeVideo);
        takeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                String timeStamp = new SimpleDateFormat("HH-mm-ss_MM-dd-yyyy").format(new Date());
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "DocDokuPLM" + timeStamp +".jpg");
                try {
                    file.createNewFile();
                    pictureSavePath = file.getAbsolutePath();
                    //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file)); //File path causes crash for videos
                    startActivityForResult(intent, INTENT_CODE_ACTIVITY_VIDEO);
                } catch (IOException e) {
                    Toast.makeText(DocumentActivity.this, R.string.documentPictureDirectoryFail, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    Log.e(LOG_TAG, "IOException when creating file." +
                            "\nError message: " + e.getMessage() +
                            "\nError cause: " + e.getCause() +
                            "\nFile path" + file.getAbsolutePath());
                }
            }
        });*/
        ImageButton uploadFile = (ImageButton) rowView.findViewById(R.id.uploadFile);
        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.upload_select)), INTENT_CODE_ACTIVITY_FILE_CHOOSER);
                }
                catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(DocumentActivity.this, "Please install a File Manager.", Toast.LENGTH_LONG).show();
                }
            }
        });
        return rowView;
    }

    /**
     * Opens a <code>ProgressDialog</code> to show progress while uploading a file to the server.
     *
     * @see com.docdoku.android.plm.network.old.HttpPostUploadFileTask.HttpPostUploadFileListener
     */
    @Override
    public void onUploadStart() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.uploading);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    /**
     * Changes the progress on the progress dialog
     *
     * @param progress the percentage of the upload progress
     * @see com.docdoku.android.plm.network.old.HttpPostUploadFileTask.HttpPostUploadFileListener
     */
    @Override
    public void onUploadProgressUpdate(int progress) {
        if (progressDialog != null) {
            progressDialog.setProgress(progress);
        }
    }

    /**
     * Called when the file upload is finished, closes the progress dialog.
     * If the file upload was successful, shows a <code>Toast</code> and updates the document.
     * If the file upload was unsuccessful, show an <code>AlertDialog</code>, offering the user to try again.
     *
     * @param result
     * @param fileName
     * @see com.docdoku.android.plm.network.old.HttpPostUploadFileTask.HttpPostUploadFileListener
     */
    @Override
    public void onUploadResult(boolean result, final String fileName) {
        progressDialog.dismiss();
        if (result) {
            Toast.makeText(this, R.string.uploadSuccess, Toast.LENGTH_SHORT).show();
            document.addFile(fileUploadUrl.substring("files/".length())); // quick hack : TODO get real path on device
            adapter.notifyDataSetChanged();
        }
        else {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_upload_blue)
                    .setTitle(R.string.net_uploading)
                    .setMessage(R.string.net_upload_failed)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String docReference = document.getIdentification();
                            String docId = docReference.substring(0, docReference.lastIndexOf("-"));
                            String docVersion = docReference.substring(docReference.lastIndexOf("-") + 1);
                            new HttpPostUploadFileTask(DocumentActivity.this).execute("files/" + getCurrentWorkspace() + "/documents/" + docId + "/" + docVersion + "/" + document.getIterationNumber() + "/" + fileName + ".png", pictureSavePath);
                        }
                    })
                    .create().show();
        }
    }

    /**
     * {@code Adapter} for the {@code ExpandableListView}
     * Group 0: Header with tile of document and notification and check in/out {@code Buttons}
     * Group 1: General information about the document
     * Group 2: Linked files
     * Group 3: Linked documents
     * Group 4: Information about the last iteration
     * Group 5: Attributes
     */
    private class DocumentDetailsExpandableListAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return NUM_PAGES;
        }

        @Override
        public int getChildrenCount(int i) {
            switch (i) {
                case 0:
                    return NUM_GENERAL_INFORMATION_FIELDS;
                case 1:
                    if (getCurrentUserLogin().equals(document.getCheckOutUserLogin())) {
                        return Math.max(2, document.getNumberOfFiles() + 1);
                    }
                    else {
                        return Math.max(1, document.getNumberOfFiles());
                    }
                case 2:
                    return Math.max(1, document.getNumberOfLinkedDocuments());
                case 3:
                    return NUM_REVISION_FIELDS;
                case 4:
                    return Math.max(1, document.getNumberOfAttributes());
            }
            return 0;
        }

        @Override
        public Object getGroup(int i) {
            return null;
        }

        @Override
        public Object getChild(int i, int i2) {
            return null;
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i2) {
            return i2;
        }

        @Override
        public boolean hasStableIds() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
            viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.adapter_document_detail_header, null);

            if (b) {
                ImageView img = ((ImageView) viewGroup.findViewById(R.id.collapse_expand_group));
                img.setImageResource(R.drawable.ic_navigation_collapse_light);
            }
            TextView title = (TextView) viewGroup.findViewById(R.id.section_title);
            switch (i) {
                case 0:
                    title.setText(R.string.documentGeneralInformation);
                    break;
                case 1:
                    title.setText(R.string.documentFiles);
                    break;
                case 2:


                    // TEST !
                    // TODO : method to check if view will be valued. if not, don't show it
//                    try {
//                        document.getLinkedDocument(2);
//                    }
//                    catch (Exception e) {
//                        pageView.setVisibility(View.GONE);
//                        break;
//                    }

                    title.setText(R.string.documentLinks);
                    break;
                case 3:
                    title.setText(R.string.documentIteration);
                    break;
                case 4:
                    title.setText(R.string.documentAttributes);
                    break;
            }
            return viewGroup;
        }

        @Override
        public View getChildView(int i, final int i2, boolean b, View view, ViewGroup viewGroup) {
            View rowView = null;
            switch (i) {
                case 0: //Document general information
                    String[] fieldNames = getResources().getStringArray(R.array.documentGeneralInformationFieldNames);
                    String[] fieldValues = document.getDocumentDetails();
                    rowView = createNameValuePairRowView(fieldNames[i2], fieldValues[i2]);
                    break;
                case 1: // Document attached files
                    try {
                        if (getCurrentUserLogin().equals(document.getCheckOutUserLogin())) {
                            if (i2 == 0) {
                                rowView = createUploadFileRowView();
                            }
                            else {
                                String fileName = document.getFileName(i2 - 1);
                                String fileUrl = document.getFile(i2 - 1);
                                rowView = createFileRowView(fileName, fileUrl);
                            }
                        }
                        else {
                            String fileName = document.getFileName(i2);
                            String fileUrl = document.getFile(i2);
                            rowView = createFileRowView(fileName, fileUrl);
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        rowView = createNoContentFoundRowView(R.string.documentNoAttachedFiles);
                    }
                    catch (NullPointerException e) {
                        rowView = createNoContentFoundRowView(R.string.documentNoAttachedFiles);
                    }
                    break;
                case 2: // Linked documents
                    try {
                        String linkedDocument = document.getLinkedDocument(i2);
                        rowView = createLinkedDocumentRowView(linkedDocument);
                    }
                    catch (NullPointerException e) {
                        return createNoContentFoundRowView(R.string.documentNoLinkedDocuments);
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        return createNoContentFoundRowView(R.string.documentNoLinkedDocuments);
                    }
                    break;
                case 3: // Last document revision
                    fieldNames = getResources().getStringArray(R.array.iterationFieldNames);
                    fieldValues = document.getLastIteration();
                    rowView = createNameValuePairRowView(fieldNames[i2], fieldValues[i2]);
                    break;
                case 4: //Document attributes
                    try {
                        Element.Attribute attribute = document.getAttribute(i2);
                        rowView = createNameValuePairRowView(attribute.getName(), attribute.getValue());
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        rowView = createNoContentFoundRowView(R.string.documentNoAttributes);
                    }
                    catch (NullPointerException e) {
                        rowView = createNoContentFoundRowView(R.string.documentNoAttributes);
                    }
                    break;
            }
            return rowView;
        }

        @Override
        public boolean isChildSelectable(int i, int i2) {
            switch (i) {
                case 1: //Document attached files
                    if (document.getNumberOfFiles() > 0) {
                        return true;
                    }
                    break;
                case 2: //Linked documents
                    if (document.getNumberOfLinkedDocuments() > 0) {
                        return true;
                    }
                    break;
            }
            return false;
        }
    }
}

