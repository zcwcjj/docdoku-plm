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

package com.docdoku.android.plm.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.docdoku.android.plm.client.documents.*;
import com.docdoku.android.plm.client.parts.PartCompleteListActivity;
import com.docdoku.android.plm.client.parts.PartHistoryListActivity;
import com.docdoku.android.plm.client.parts.PartSearchActivity;

/**
 * The <code>DrawerLayout</code> component that slides from the left side of the screen to allow users to access the menu.
 *
 * @version 1.0
 * @author: Martin Devillers
 */
public class MenuFragment extends Fragment implements View.OnClickListener {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.MenuFragment";

    private boolean workspaceChanged = false;

    private View       menuView;
    private RadioGroup workspaceRadioGroup;
    private TextView   expandRadioButtons;
    private Session    session;
    private String[]   downloadedWorkspaces;
    private String     currentWorkspace;

    /**
     * Creates the <code>View</code> for the sliding menu.
     * <p>Attempts to find the downloaded workspaces  and the current workspace for the <code>RadioGroup</code> in the
     * current <code>Session</code>, and if that fails attempts to load them from the <code>SharedPreferences</code>.
     * Sets the <code>OnClickListener</code> that expands the list of workspaces, and the one that detects when the current
     * workspace has been changed.
     * <p>Sets this <code>MenuFragment</code> as the <code>OnClickListener</code> for the menu items.
     * <p>Layout file: {@link /res/layout/fragment_menu.xml fragment_menu}
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     * @see Fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        menuView = inflater.inflate(R.layout.fragment_menu, container);
        workspaceChanged = false;
        workspaceRadioGroup = (RadioGroup) menuView.findViewById(R.id.workspaceRadioGroup);
        try {
            session = Session.getSession();
            currentWorkspace = session.getCurrentWorkspace(getActivity());
            feedWorkspacesRadioGroup(currentWorkspace, workspaceRadioGroup);
            downloadedWorkspaces = session.getDownloadedWorkspaces(getActivity());
            if (downloadedWorkspaces.length == 1) {
                ((ViewGroup) expandRadioButtons.getParent()).removeView(expandRadioButtons);
            }
            else {
                expandRadioButtons.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((ViewGroup) expandRadioButtons.getParent()).removeView(expandRadioButtons);
                        addWorkspaces(downloadedWorkspaces, workspaceRadioGroup);
                    }
                });
            }

            workspaceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    RadioButton selectedWorkspace = (RadioButton) menuView.findViewById(radioGroup.getCheckedRadioButtonId());
                    session.setCurrentWorkspace(getActivity(), selectedWorkspace.getText().toString());
                    workspaceChanged = true;
                }
            });
        }
        catch (Session.SessionLoadException e) {
            e.printStackTrace();
        }

        menuView.findViewById(R.id.menuDocumentSearch).setOnClickListener(this);
        menuView.findViewById(R.id.menuRecentlyViewedDocuments).setOnClickListener(this);
        menuView.findViewById(R.id.menuAllDocuments).setOnClickListener(this);
        menuView.findViewById(R.id.menuCheckedOutDocuments).setOnClickListener(this);
        menuView.findViewById(R.id.menuFolders).setOnClickListener(this);

        menuView.findViewById(R.id.menuPartSearch).setOnClickListener(this);
        menuView.findViewById(R.id.menuRecentlyViewedParts).setOnClickListener(this);
        menuView.findViewById(R.id.menuAllParts).setOnClickListener(this);

        return menuView;
    }

    /**
     * Sets the current workspace in the workspaces <code>RadioGroup</code>
     * <p>Adds a single <code>RadioButton</code>, which is selected, for the current workspace. If more than one workspace
     * have been downloaded, than add a <code>Button</code> to show the other workspaces.
     *
     * @param workspace  the selected workspace
     * @param radioGroup the empty <code>RadioGroup</code> to be populated
     */
    private void feedWorkspacesRadioGroup(String workspace, RadioGroup radioGroup) {
        RadioButton radioButton;
        radioButton = new RadioButton(getActivity());
        radioButton.setText(workspace);
        radioButton.setTextColor(getResources().getColor(R.color.darkGrey));
        radioGroup.addView(radioButton);
        radioGroup.check(radioButton.getId());
        expandRadioButtons = new TextView(getActivity());
        expandRadioButtons.setText("...");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        expandRadioButtons.setLayoutParams(params);
        expandRadioButtons.setGravity(Gravity.CENTER_HORIZONTAL);
        expandRadioButtons.setBackgroundResource(R.drawable.menu_item);
        radioGroup.addView(expandRadioButtons);
    }

    /**
     * Expands the list of workspaces in the <code>RadioGroup</code> to show them all.
     * <p>Removes the selected workspace, then adds the full list of workspaces. Goes through the list until the position
     * of the current workspace is found and sets its <code>RadioButton</code> to checked.
     *
     * @param workspaces the list of downloaded workspaces
     * @param radioGroup the <code>RadioGroup</code>, containing only one child <code>View</code>, which is the selected workspace
     */
    private void addWorkspaces(String[] workspaces, RadioGroup radioGroup) {
        int selectedButtonId = radioGroup.getCheckedRadioButtonId();
        if (selectedButtonId != RadioGroup.NO_ID) {
            radioGroup.removeView(menuView.findViewById(selectedButtonId));
        }
        workspaceChanged = false;
        for (String workspace : workspaces) {
            RadioButton radioButton;
            radioButton = new RadioButton(getActivity());
            radioButton.setText(workspace);
            radioButton.setTextColor(getResources().getColor(R.color.darkGrey));
            radioGroup.addView(radioButton);
            if (workspace.equals(currentWorkspace)) {
                radioGroup.check(radioButton.getId());
            }
        }
    }

    /**
     * Highlights the menu item that represents the current <code>Activity</code>, if such an item exists.
     *
     * @param buttonId The id of the item linking to the current <code>Activity</code>, if such an item exists.
     */
    public void setCurrentActivity(int buttonId) {
        View activityView = menuView.findViewById(buttonId);
        if (activityView != null) {
            activityView.setSelected(true);
        }
        else {
            Log.i(LOG_TAG, "Current activity did not provide a correct button id. Id provided: " + buttonId);
        }
    }

    /**
     * Handles a click on a menu item by starting an <code>Intent</code> to the corresponding activity.
     *
     * @param view
     * @see android.view.View.OnClickListener
     */
    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        Intent intent = null;
        switch (viewId) {
            case R.id.menuDocumentSearch:
                intent = new Intent(getActivity(), DocumentSearchActivity.class);
                break;
            case R.id.menuRecentlyViewedDocuments:
                intent = new Intent(getActivity(), DocumentHistoryListActivity.class);
                break;
            case R.id.menuAllDocuments:
                intent = new Intent(getActivity(), DocumentCompleteListActivity.class);
                break;
            case R.id.menuFolders:
                intent = new Intent(getActivity(), DocumentFoldersActivity.class);
                break;
            case R.id.menuCheckedOutDocuments:
                intent = new Intent(getActivity(), DocumentSimpleListActivity.class);
                intent.putExtra(DocumentSimpleListActivity.LIST_MODE_EXTRA, DocumentSimpleListActivity.CHECKED_OUT_DOCUMENTS_LIST);
                break;
            case R.id.menuPartSearch:
                intent = new Intent(getActivity(), PartSearchActivity.class);
                break;
            case R.id.menuRecentlyViewedParts:
                intent = new Intent(getActivity(), PartHistoryListActivity.class);
                break;
            case R.id.menuAllParts:
                intent = new Intent(getActivity(), PartCompleteListActivity.class);
                break;
        }
        if (intent != null) {
            intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
    }

    /**
     * Indicates whether the user has changed workspace while this drawer menu was open.
     *
     * @return if the selected workspace has changed
     */
    public boolean isWorkspaceChanged() {
        return workspaceChanged;
    }
}
