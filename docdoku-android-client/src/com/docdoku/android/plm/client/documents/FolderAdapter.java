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
