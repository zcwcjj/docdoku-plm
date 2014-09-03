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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.network.HTTPGetTask;
import com.docdoku.android.plm.network.HTTPResultTask;
import com.docdoku.android.plm.network.listeners.HTTPTaskDoneListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * <code>Activity</code> that displays the documents in the workspace using the filing system created by the user.
 * <p>First, a request is sent to the server to get the list of sub-folders in the current folder. Once these have been
 * downloaded and displayed, a second request is made to download all the documents in the current folder at once.
 * <p>Due to the fact that this <code>Activity</code>, unlike {@link DocumentCompleteListActivity}, does use a page-by-page <code>Loader</code>
 * to download the <code>Documents</code>, it may encounter problems delivering results if the user does not have a good
 * filing system.
 * <p>Layout file: {@link /res/layout/activity_element_list.xml activity_element_list}
 *
 * @version 1.0
 * @author: Martin Devillers
 */
public class DocumentFoldersActivity extends DocumentListActivity {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.documents.DocumentFoldersActivity";

    private static final String INTENT_KEY_FOLDER = "folder";

    private Folder[] folders;
    private String   currentFolderId;

    /**
     * Called when the <code>Activity</code> is created.
     * <p>Reads the <code>Intent</code> to find what is the current folder. If no data is in the <code>Extra</code>s, the
     * current folder is assumed to be the root folder.
     * <p>Starts an {@link HTTPGetTask} to query the list of sub-folders.
     *
     * @param savedInstanceState
     * @see android.app.Activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        currentFolderId = intent.getStringExtra(INTENT_KEY_FOLDER);


        /**
         * Handles the result of the query for the list of sub-folders.
         * <p>The result is a {@code JSONArray} of the sub-folders of the current folder. These are put into an {@code Array}
         * then passed to a new {@link FolderAdapter} to be displayed. The {@code OnItemClickListener} is then set on the
         * {@code ListView}.
         * <br>If the current folder is the root one, the user's private folder is added to the list of folders.
         * <p>A new {@link HTTPGetTask} is started to query the list of document in the current folder. When the results are
         * obtained, they are added to the {@link FolderAdapter}.
         *
         * @param result the {@code JSONArray} of sub-folders
         * @see com.docdoku.android.plm.network.listeners.HTTPTaskDoneListener
         */
        HTTPGetTask task = new HTTPGetTask(new HTTPTaskDoneListener() {
            @Override
            public void onDone(HTTPResultTask result) {
                try {
                    JSONArray foldersArray = new JSONArray(result.getResultContent());
                    if (currentFolderId == null) {
                        folders = new Folder[foldersArray.length() + 1];
                        folders[0] = new Folder(getCurrentUserLogin(), getCurrentWorkspace() + ":~" + getCurrentUserLogin());
                    }
                    else {
                        folders = new Folder[foldersArray.length()];
                    }
                    for (int i = 0; i < foldersArray.length(); i++) {
                        JSONObject folderObject = foldersArray.getJSONObject(i);
                        if (currentFolderId == null) {
                            folders[i + 1] = new Folder(folderObject.getString(Folder.JSON_KEY_FOLDER_NAME), folderObject.getString(Folder.JSON_KEY_FOLDER_ID));
                        }
                        else {
                            folders[i] = new Folder(folderObject.getString(Folder.JSON_KEY_FOLDER_NAME), folderObject.getString(Folder.JSON_KEY_FOLDER_ID));
                        }
                    }
                }
                catch (JSONException e) {
                    Log.e(LOG_TAG, "JSONException: could not read downloaded folder names");
                    folders = new Folder[0];
                }
                documentAdapter = new FolderAdapter(folders, new ArrayList<Document>(), DocumentFoldersActivity.this);
                final ProgressBar progressBar = new ProgressBar(DocumentFoldersActivity.this);
                documentListView.addFooterView(progressBar);
                documentListView.setAdapter(documentAdapter);
                documentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Object object = documentListView.getAdapter().getItem(i);
                        if (!object.getClass().equals(Document.class)) {
                            Intent intent = new Intent(DocumentFoldersActivity.this, DocumentFoldersActivity.class);
                            intent.putExtra(INTENT_KEY_FOLDER, folders[i].getId());
                            startActivity(intent);
                        }
                        else {
                            onDocumentClick((Document) documentListView.getAdapter().getItem(i));
                        }
                    }
                });

                HTTPGetTask taskDoc = new HTTPGetTask(new HTTPTaskDoneListener() {
                    @Override
                    public void onDone(HTTPResultTask result) {
                        try {
                            JSONArray documentJSONArray = new JSONArray(result.getResultContent());
                            ArrayList<Document> documents = new ArrayList<Document>();
                            for (int i = 0; i < documentJSONArray.length(); i++) {
                                JSONObject documentJSON = documentJSONArray.getJSONObject(i);
                                Document document = new Document(documentJSON.getString("id"));
                                document.updateFromJSON(documentJSON, getResources());
                                documents.add(document);
                            }
                            ((FolderAdapter) documentAdapter).addDocuments(documents);
                            documentAdapter.notifyDataSetChanged();
                            documentListView.removeFooterView(progressBar);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                taskDoc.execute(getUrlWorkspaceApi() + "/folders/" + ((currentFolderId != null) ? currentFolderId : getCurrentWorkspace()) + "/documents/");

                removeLoadingView();
            }
        });

        if (currentFolderId == null) {
            task.execute(getUrlWorkspaceApi() + "/folders/");
        }
        else {
            task.execute(getUrlWorkspaceApi() + "/folders/" + currentFolderId + "/folders/");
        }
    }

    /**
     * @return
     * @see com.docdoku.android.plm.client.SimpleActionBarActivity#getActivityButtonId()
     */
    @Override
    protected int getActivityButtonId() {
        return R.id.documentFolders;
    }

}