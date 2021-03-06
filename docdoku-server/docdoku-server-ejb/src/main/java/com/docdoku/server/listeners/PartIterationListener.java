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
package com.docdoku.server.listeners;


import com.docdoku.core.change.ModificationNotification;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.dao.ModificationNotificationDAO;

import javax.ejb.EJB;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Florent Garin
 */

public class PartIterationListener {


    @EJB
    private IProductManagerLocal productService;


    @PostUpdate
    private void addModificationNotification(Object object) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {
        if(object instanceof PartIteration) {
            PartIteration partIteration = (PartIteration) object;
            if(!partIteration.getPartRevision().isCheckedOut()) {
                productService.createModificationNotifications(partIteration);
            }

        }
    }
}
