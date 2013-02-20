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

package com.docdoku.cli.helpers;

import java.io.*;
import java.util.Date;
import java.util.Properties;

public class MetaDirectoryManager {

    private File metaDirectory;
    private Properties indexProps;


    private final static String META_DIRECTORY_NAME = ".dplm";
    private final static String INDEX_FILE_NAME = "index.xml";

    private final static String LAST_MODIFIED_DATE_PROP = "lastModifiedDate";
    private final static String DIGEST_PROP = "digest";

    public MetaDirectoryManager(File workingDirectory) throws IOException {
        this.metaDirectory=new File(workingDirectory,META_DIRECTORY_NAME);
        if(!metaDirectory.exists())
            metaDirectory.mkdir();

        File indexFile = new File(metaDirectory,INDEX_FILE_NAME);
        indexProps = new Properties();
        if(indexFile.exists()){
            try{
                indexProps.loadFromXML(new BufferedInputStream(new FileInputStream(indexFile)));
            }catch(IOException ex){
                indexFile.delete();
            }
        }
    }



    private void saveIndex() throws IOException {
        File indexFile = new File(metaDirectory,INDEX_FILE_NAME);
        if(!indexFile.exists())
            indexFile.createNewFile();

        OutputStream out = new BufferedOutputStream(new FileOutputStream(indexFile));
        indexProps.storeToXML(out, null);
    }

    public void setLastModifiedDate(String filePath, long lastModifiedDate) throws IOException {
        indexProps.setProperty(filePath + "." + LAST_MODIFIED_DATE_PROP, lastModifiedDate+"");
        saveIndex();
    }

    public void setLastModifiedDate(String filePath, Date lastModifiedDate) throws IOException {
        setLastModifiedDate(filePath, lastModifiedDate.getTime());
    }

    public void setDigest(String filePath, String digest) throws IOException {
        indexProps.setProperty(filePath + "." + DIGEST_PROP, digest);
        saveIndex();
    }


    public String getDigest(String filePath){
        return indexProps.getProperty(filePath + "." + DIGEST_PROP);
    }

    public long getLastModifiedDate(String filePath){
        return Long.parseLong(indexProps.getProperty(filePath + "." + LAST_MODIFIED_DATE_PROP,"0"));
    }

}