package com.docdoku.android.plm.client.documents;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.docdoku.android.plm.client.R;

import java.util.List;

/**
 * Created by G. BOTTIEAU on 27/08/14.
 */
class FolderAdapter extends DocumentAdapter {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.documents.FolderAdapter";
    private Folder[] folders;

    public FolderAdapter(Folder[] folders, List<Document> documents, Activity activity) {
        super(documents, activity);
        this.folders = folders;
    }

    @Override
    public int getCount() {
        return folders.length + getDocuments().size();
    }

    @Override
    public Object getItem(int i) {
        if (i < folders.length) {
            return folders[i];
        }
        else {
            return super.getItem(i - folders.length);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            if (i < folders.length) {
                Log.d(LOG_TAG, i + " " + folders.length);

                final Folder folder = folders[i];
                view = getActivity().getLayoutInflater().inflate(R.layout.adapter_folder, null);
                TextView folderName = (TextView) view.findViewById(R.id.folderName);
                folderName.setText(folder.getName());
            }
            else {
                view = super.getView(i - folders.length, view, viewGroup);
            }
        }
        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }
}
