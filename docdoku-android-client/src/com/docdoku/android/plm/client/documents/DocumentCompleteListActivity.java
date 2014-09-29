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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.network.tasks.HTTPGetTask;
import com.docdoku.android.plm.network.tasks.HTTPResultTask;
import com.docdoku.android.plm.network.tasks.listeners.HTTPTaskDoneListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>Activity</code> for presenting all the {@link Document Documents} in a workspace.
 * <p>The documents are loaded asynchronously by pages of 20 items using a <code>Loader</code>.
 * <p>Layout file: {@link /res/layout/activity_element_list.xml activity_element_list}
 *
 * @version 1.0
 * @author: Martin Devillers
 */
public class DocumentCompleteListActivity extends DocumentListActivity implements LoaderManager.LoaderCallbacks<List<Document>> {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.documents.DocumentCompleteListActivity";

    private int         numDocumentsAvailable;
    private ProgressBar footerProgressBar;
    private int         numPagesDownloaded;

    /**
     * Called when the <code>Activity</code> is created.
     * <p>Adds a footer <code>ProgressBar</code> that will be maintained while there remains more <code>Document</code>s
     * to be loaded.
     * <br>Sets an <code>setOnScrollListener</code> that loads the next page of <code>Document</code>s when this footer
     * becomes visible.
     * <p>Initializes the <code>ArrayList</code> containing the <code>Document</code>s and the <code>Adapter</code> for the
     * <code>ListView</code>, as well as the number of documents downloaded.
     * <p>Starts an {@link HTTPGetTask} to download the number of documents in the workspace.
     *
     * @param savedInstanceState
     * @see android.app.Activity
     * @see DocumentListActivity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        footerProgressBar = new ProgressBar(this);
        documentListView.addFooterView(footerProgressBar);

        documentArray = new ArrayList<>();
        documentAdapter = new DocumentAdapter(documentArray, this);
        documentListView.setAdapter(documentAdapter);

        numDocumentsAvailable = 0;
        numPagesDownloaded = 0;

        HTTPGetTask task = new HTTPGetTask(new HTTPTaskDoneListener() {
            @Override
            public void onDone(HTTPResultTask result) {
                try {
                    JSONArray jsonArray = new JSONArray(result.getResultContent());
                    numDocumentsAvailable = jsonArray.length();

                    removeLoadingView();
                    Bundle bundle = new Bundle();
                    bundle.putInt("page", 0);
                    bundle.putString("workspace", getCurrentWorkspace());
                    Log.i(LOG_TAG, "Loading first part page");
                    getSupportLoaderManager().initLoader(0, bundle, DocumentCompleteListActivity.this);
                }
                catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "NumberFormatException: didn't correctly download number of pages of documents");
                    Log.e(LOG_TAG, "Number of pages result: " + result);
                }
                catch (JSONException e) {
                    Log.e(LOG_TAG, "JSONException: didn't correctly download json object");
                    Log.e(LOG_TAG, "Number of pages result: " + result);
                }
            }
        });
        task.execute("api/workspaces/" + getCurrentWorkspace() + "/documents");


        documentListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && documentAdapter.getCount() < numDocumentsAvailable) {
                    Log.i(LOG_TAG, "Loading more parts. Next page: " + numPagesDownloaded);
                    Bundle bundle = new Bundle();
                    bundle.putInt("page", numPagesDownloaded);
                    bundle.putString("workspace", getCurrentWorkspace());
                    getSupportLoaderManager().initLoader(numPagesDownloaded, bundle, DocumentCompleteListActivity.this);
                }
            }
        });
    }


    /**
     * @return
     * @see com.docdoku.android.plm.client.SimpleActionBarActivity
     */
    @Override
    protected int getActivityButtonId() {
        return R.id.menuAllDocuments;
    }

    /**
     * Returns a new {@link DocumentLoaderByPage} for the page indicated in the <code>Bundle</code>
     *
     * @param id
     * @param bundle <code>Bundle</code> containing the <code>Document</code> page and the current workspace
     * @return The resulting <code>Loader</code>
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public Loader<List<Document>> onCreateLoader(int id, Bundle bundle) {
        return new DocumentLoaderByPage(this, bundle.getInt("page"), bundle.getString("workspace"));
    }

    /**
     * Adds the <code>List</code> of <code>Document</code>s obtained by the {@link DocumentLoaderByPage} to the
     * <code>ArrayList</code> of documents, and notifies the {@link DocumentAdapter} that the data has changed.
     * Updates the number of pages downloaded.
     * <p>If there are no more documents to download, removes the <code>FooterView</code>.
     *
     * @param loader the <code>Loader</code> that provided the result
     * @param data   the {@code List<Document>} provided by the <code>Loader</code>
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public void onLoadFinished(Loader<List<Document>> loader, List<Document> data) {
        documentArray.addAll(data);
        documentAdapter.notifyDataSetChanged();
        numPagesDownloaded++;

        Log.i(LOG_TAG, "Finished loading a page. \nNumber of parts available: " + numDocumentsAvailable + "; \nNumber of parts downloaded: " + documentAdapter.getCount());
        if (documentAdapter.getCount() == numDocumentsAvailable) {
            documentListView.removeFooterView(footerProgressBar);
        }
    }

    /**
     * @param loader
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public void onLoaderReset(Loader<List<Document>> loader) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Class that handles the loading of documents asynchronously.
     */
    private static class DocumentLoaderByPage extends Loader<List<Document>> {

        private final int            startIndex;
        private final String         workspace;
        private       HTTPGetTask    asyncTask;
        private       List<Document> downloadedParts;

        /**
         * Constructor called by the <code>LoaderManager.LoaderCallbacks</code> to start loading a page of documents.
         * The documents with index ranging from <code>20*page</code> to <code>20*page+19</code> will be loaded.
         *
         * @param context
         * @param page
         * @param workspace
         * @see Loader
         */
        public DocumentLoaderByPage(Context context, int page, String workspace) {
            super(context);
            startIndex = page * 20;
            downloadedParts = new ArrayList<>();
            this.workspace = workspace;
        }

        /**
         * Starts an {@link HTTPGetTask} to download the page of documents If the documents have already been downloaded,
         * passes them as a result.
         *
         * @see Loader
         */
        @Override
        protected void onStartLoading() {
            Log.i(LOG_TAG, "Starting DocumentLoader load for page " + startIndex / 20);
            if (downloadedParts.size() == 0) {
//                asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/documents?start=" + startIndex);
                createTask();
            }
            else {
                deliverResult(downloadedParts);
            }
        }

        /**
         * @see Loader
         */
        @Override
        protected void onForceLoad() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * Cancels the {@link HTTPGetTask} that was downloading the document page.
         *
         * @see Loader
         */
        @Override
        protected void onStopLoading() {
            if (asyncTask != null) {
                asyncTask.cancel(false);
            }
        }

        /**
         * @see Loader
         */
        @Override
        protected void onAbandon() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * Restarts the {@link HTTPGetTask} that was downloading the document page.
         *
         * @see Loader
         */
        @Override
        protected void onReset() {
            Log.i(LOG_TAG, "Restarting DocumentLoader load for page " + startIndex / 20);
            downloadedParts = new ArrayList<Document>();
            if (asyncTask != null) {
                asyncTask.cancel(false);
            }
//            asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/documents?start=" + startIndex);
            createTask();

        }

        private void createTask() {
            /**
             * Handles the result of the {@link HttpGetTask} containing a <code>JSONArray</code> of documents.
             * <p>Creates <code>Document</code> instances from the result and adds them to an {@code ArrayList<Document>}
             * which is passed to the {@code LoaderManager.LoaderCallbacks} in the {@code deliverResult()} method.
             */
            asyncTask = new HTTPGetTask(new HTTPTaskDoneListener() {
                @Override
                public void onDone(HTTPResultTask result) {
                    try {
                        JSONArray jsonArray = new JSONArray(result.getResultContent());
                        Log.d(LOG_TAG, " jsonArray.length() === " + jsonArray.length());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject partJSON = jsonArray.getJSONObject(i);
                            Document part = new Document(partJSON.getString("id"));
                            part.updateFromJSON(partJSON, getContext().getResources());
                            downloadedParts.add(part);
                        }
                    }
                    catch (JSONException e) {
                        Log.e(LOG_TAG, "Error handling json array of workspace's documents");
                        Log.d(LOG_TAG, " result === " + result);
                        e.printStackTrace();
                        Log.i(LOG_TAG, "Error message: " + e.getMessage());
                    }
                    deliverResult(downloadedParts);
                }
            });
            asyncTask.execute("api/workspaces/" + workspace + "/documents?start=" + startIndex);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Handle the back button
        if(keyCode == KeyEvent.KEYCODE_BACK && isTaskRoot()) {
            //Ask the user if they want to quit
            new AlertDialog.Builder(this)
                    .setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details))
                    .setTitle(R.string.dialog_app_close)
                    .setMessage(R.string.dialog_app_confirm_close)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Stop the activity
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();

            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
