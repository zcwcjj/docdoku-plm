/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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

package com.docdoku.server.rest.dto;

import java.util.Date;

public class ModificationNotificationDTO {

    private String modifiedPartNumber;
    private String modifiedPartVersion;
    private int modifiedPartIteration;

    private Date checkInDate;
    private String iterationNote;
    private UserDTO author;


    public ModificationNotificationDTO() {
    }

    public String getModifiedPartNumber() {
        return modifiedPartNumber;
    }

    public void setModifiedPartNumber(String modifiedPartNumber) {
        this.modifiedPartNumber = modifiedPartNumber;
    }

    public String getModifiedPartVersion() {
        return modifiedPartVersion;
    }

    public void setModifiedPartVersion(String modifiedPartVersion) {
        this.modifiedPartVersion = modifiedPartVersion;
    }

    public int getModifiedPartIteration() {
        return modifiedPartIteration;
    }

    public void setModifiedPartIteration(int modifiedPartIteration) {
        this.modifiedPartIteration = modifiedPartIteration;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getIterationNote() {
        return iterationNote;
    }

    public void setIterationNote(String iterationNote) {
        this.iterationNote = iterationNote;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }
}
