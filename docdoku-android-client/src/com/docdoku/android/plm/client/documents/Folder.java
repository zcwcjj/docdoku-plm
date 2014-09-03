package com.docdoku.android.plm.client.documents;

/**
 * Created by G. BOTTIEAU on 27/08/14.
 */
public class Folder {

    public static final String JSON_KEY_FOLDER_NAME = "name";
    public static final String JSON_KEY_FOLDER_ID   = "id";

    private final String name;
    private final String id;

    public Folder(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
