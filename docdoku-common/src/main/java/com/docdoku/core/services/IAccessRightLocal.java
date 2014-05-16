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
package com.docdoku.core.services;

import com.docdoku.core.change.ChangeItem;
import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartRevisionKey;

import javax.annotation.security.RolesAllowed;

/**
 *
 * @author Florent Garin
 */
public interface IAccessRightLocal {
    @RolesAllowed({"users"})
    public User checkChangeItemReadAccess(ChangeItem pChangeItem) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException;
    @RolesAllowed({"users"})
    public User checkChangeItemWriteAccess(ChangeItem pChangeItem) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException;
    @RolesAllowed({"users"})
    public User checkDocumentIterationReadAccess(DocumentIterationKey pDocumentIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException;
    @RolesAllowed({"users"})
    public User checkDocumentIterationWriteAccess(DocumentIterationKey pDocumentIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, DocumentRevisionNotFoundException;
    @RolesAllowed({"users"})
    public User checkDocumentRevisionReadAccess(DocumentRevisionKey pDocumentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException;
    @RolesAllowed({"users"})
    public User checkDocumentRevisionCheckoutAccess(DocumentRevisionKey pDocumentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException;
    @RolesAllowed({"users"})
    public User checkDocumentRevisionWriteAccess(DocumentRevisionKey pDocumentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException;
    @RolesAllowed({"users"})
    public User checkDocumentRevisionGrantAccess(DocumentRevisionKey pDocumentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException;
    @RolesAllowed({"users"})
    public User checkPartIterationReadAccess(PartIterationKey pPartIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartRevisionNotFoundException;
    @RolesAllowed({"users"})
    public User checkPartIterationWriteAccess(PartIterationKey pPartIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, PartRevisionNotFoundException;
    @RolesAllowed({"users"})
    public User checkPartRevisionReadAccess(PartRevisionKey pPartRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartRevisionNotFoundException;
    @RolesAllowed({"users"})
    public User checkPartRevisionCheckoutAccess(PartRevisionKey pPartRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartRevisionNotFoundException;
    @RolesAllowed({"users"})
    public User checkPartRevisionWriteAccess(PartRevisionKey pPartRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartRevisionNotFoundException, NotAllowedException;
    @RolesAllowed({"users"})
    public User checkPartRevisionGrantAccess(PartRevisionKey pPartRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartRevisionNotFoundException;
    @RolesAllowed({"users"})
    public User checkWorkspaceReadAccess(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    @RolesAllowed({"users"})
    public User checkWorkspaceWriteAccess(String pWorkspaceId) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException;
    @RolesAllowed({"users","admin"})
    public Account checkWorkspaceAdminAccess(String pWorkspaceId) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException;
}