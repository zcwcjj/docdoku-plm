package com.docdoku.android.plm.client.documents;

/**
 * Created by G. BOTTIEAU on 27/08/14.
 */

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.client.Session;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// TODO continue refactoring "documents" package.

/**
 * {@code BaseAdapter} implementation for handling the representation of {@link Document} rows.
 */
class DocumentAdapter extends BaseAdapter {

    private static final String LOG_TAG = "com.docdoku.android.plm.client.documents.DocumentAdapter";

    private List<Document> documents;

    private LayoutInflater inflater;
    private Activity       activity;
    private Session        session;

    public DocumentAdapter(List<Document> documents, Activity activity) {
        this.documents = documents;
        this.activity = activity;

        try {
            this.session = Session.getSession(activity);
        }
        catch (Session.SessionLoadException e) {
            e.printStackTrace();
        }

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Activity getActivity() {
        return activity;
    }

    public void addDocument(Document document) {
        getDocuments().add(document);
    }

    public List<Document> getDocuments() {
        if (documents == null) {
            documents = new ArrayList<>();
        }
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public void addDocuments(List<Document> documents) {
        getDocuments().addAll(documents);
    }

    @Override
    public int getCount() {
        return documents.size();
    }

    @Override
    public Object getItem(int i) {
        return documents.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Generates the {@code View} for a row representing a {@link Document}
     * <p>If the document at position {@code i} is {@code null}, then a row with a {@code ProgressBar} is returned to
     * indicate that the {@code Document} is still being loaded.
     * <p>If the {@code Document} is not {@code null} but it's {@code author} is {@code null}, the the document loading is
     * assumed to have failed, and a row indicating an error is created.
     * <p>If the {@code Document} is correctly available:
     * <br>Its reference and last revision {@code TextView}s are set
     * <br>Its check in/out {@code ImageView} is set by comparing its {@code checkoutUserLogin} to the current user's login
     * <br>Its iteration number {@code TextView} is set
     * <br>Its number of linked files {@code TextView} is set. If that number is equal to 0, then the {@code ViewGroup}
     * indicating the number of linked files is removed.
     *
     * @param i
     * @param view
     * @param viewGroup
     * @return view
     * @see BaseAdapter
     */
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            final Document doc = documents.get(i);
            if (doc == null) { //Document is still being loaded
                view = new ProgressBar(activity);
            }
            else if (doc.getAuthor() == null) { //Document load failed
                view = inflater.inflate(R.layout.adapter_document, null);
                TextView identification = (TextView) view.findViewById(R.id.identification);
                identification.setText(doc.getIdentification());
                ImageView checkedInOutImage = (ImageView) view.findViewById(R.id.checkedInOutImage);
                checkedInOutImage.setImageResource(R.drawable.error_light);
                View iterationNumberBox = view.findViewById(R.id.iterationNumberBox);
                ((ViewGroup) iterationNumberBox.getParent()).removeView(iterationNumberBox);
                View numAttachedFiles = view.findViewById(R.id.attachedFilesIndicator);
                ((ViewGroup) numAttachedFiles.getParent()).removeView(numAttachedFiles);
            }
            else { //Document was loaded successfully
                view = inflater.inflate(R.layout.adapter_document, null);
                TextView identification = (TextView) view.findViewById(R.id.identification);
                identification.setText(doc.getIdentification());
                ImageView checkedInOutImage = (ImageView) view.findViewById(R.id.checkedInOutImage);
                String checkOutUserName = doc.getCheckOutUserName();
                if (checkOutUserName != null) {
                    String checkOutUserLogin = doc.getCheckOutUserLogin();
                    if (checkOutUserLogin.equals(session.getCurrentWorkspace(activity))) {
                        checkedInOutImage.setImageResource(R.drawable.checked_out_current_user_light);
                    }
                }
                else {
                    checkedInOutImage.setImageResource(R.drawable.checked_in_light);
                }
                int docNumAttachedFiles = doc.getNumberOfFiles();
                if (docNumAttachedFiles == 0) {
                    View numAttachedFiles = view.findViewById(R.id.attachedFilesIndicator);
                    ((ViewGroup) numAttachedFiles.getParent()).removeView(numAttachedFiles);
                }
                else {
                    TextView numAttachedFiles = (TextView) view.findViewById(R.id.numAttachedFiles);
                    numAttachedFiles.setText(" " + docNumAttachedFiles);
                }
                TextView iterationNumber = (TextView) view.findViewById(R.id.iterationNumber);
                iterationNumber.setText("" + doc.getIterationNumber());
                TextView lastIteration = (TextView) view.findViewById(R.id.lastIteration);
                try {
                    lastIteration.setText(String.format(activity.getResources().getString(R.string.documentIterationPhrase, simplifyDate(doc.getLastIterationDate()), doc.getLastIterationAuthorName())));
                }
                catch (ParseException e) {
                    lastIteration.setText(" ");
                    Log.i(LOG_TAG, "Unable to correctly get a date for document (ParseException)" + doc.getIdentification());
                }
                catch (NullPointerException e) {
                    lastIteration.setText(" ");
                    Log.i(LOG_TAG, "Unable to correctly get a date for document (NullPointerException)" + doc.getIdentification());
                }
            }
        }

        return view;
    }

    /**
     * Method that converts a date into a {@code String} that is easier to read for the user. The possible scenarios are:
     * today, yesterday, and the date for a previous event.
     * <p>The {@code currentTime} is created, and compared to {@code date}, the date parsed from {@code dateString}:
     * <br>If they are the same year and the same day of the year, then the resource at {@code R.string.today} is returned.
     * <br>If they are the same year and {@code date} is one day before {@code currentTime}, then the resource at
     * {@code R.string.yesterday} is returned.
     * <br>Otherwise, {@code DateUtils.getRelativeTimeSpanString} is used to generate a {@code String} easily readable by the user.
     * <p>Note: if we are the first day of a new year and the {@code dateString} indicates the last day of previous year,
     * then this method will not return yesterday but instead the date. But nobody really cares.
     *
     * @param dateString
     * @return the {@code String} to be displayed to the user
     * @throws ParseException       if the {@code dateString} could not be parsed into a {@code Calendar}
     * @throws NullPointerException
     */
    String simplifyDate(String dateString) throws ParseException, NullPointerException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(activity.getResources().getString(R.string.simpleDateFormat));
        Calendar date = Calendar.getInstance();
        date.setTime(simpleDateFormat.parse(dateString));
        Calendar currentTime = Calendar.getInstance();
        if (currentTime.get(Calendar.YEAR) == date.get(Calendar.YEAR)) {
            int dayDifference = currentTime.get(Calendar.DAY_OF_YEAR) - date.get(Calendar.DAY_OF_YEAR);
            if (dayDifference == 0) {
                return activity.getResources().getString(R.string.today);
            }
            if (dayDifference == 1) {
                return activity.getResources().getString(R.string.yesterday);
            }
        }
        String timeDifference = DateUtils.getRelativeTimeSpanString(activity, date.getTimeInMillis(), true).toString();
        return timeDifference;
    }

    /**
     * Returns if the row at {@code position} is clickable
     * <br>If the {@code Document} at {@code position} is not {@code null} and its {@code author} is not {@code null},
     * then it is clickable.
     *
     * @param position
     * @return
     * @see BaseAdapter
     */
    @Override
    public boolean isEnabled(int position) {
        try {
            Document document = documents.get(position);
            return !(document == null || document.getAuthor() == null);
        }
        catch (IndexOutOfBoundsException e) {
            return false;
        }
    }
}