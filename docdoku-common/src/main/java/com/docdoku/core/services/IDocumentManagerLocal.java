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

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.document.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.meta.TagKey;
import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.sharing.SharedEntityKey;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.TaskKey;

import java.util.Date;
import java.util.Map;

/**
 *
 * @author Florent Garin
 */
public interface IDocumentManagerLocal {
    DocumentRevision approveTaskOnDocument(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException;

    DocumentRevision checkInDocument(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException, ESServerException;

    DocumentRevision checkOutDocument(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException;

    DocumentMasterTemplate createDocumentMasterTemplate(String pWorkspaceId, String pId, String pDocumentType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException;

    DocumentRevision[] createDocumentRevision(DocumentRevisionKey pOriginalDocRPK, String pTitle, String pDescription, String pWorkflowModelId, ACLUserEntry[] aclUserEntries, ACLUserGroupEntry[] aclUserGroupEntries, Map<String,String> roleMappings) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, NotAllowedException, DocumentRevisionAlreadyExistsException, CreationException, WorkflowModelNotFoundException, RoleNotFoundException, DocumentRevisionNotFoundException, FileAlreadyExistsException;

    Folder createFolder(String pParentFolder, String pFolder) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, FolderAlreadyExistsException, UserNotFoundException, CreationException;

    void createTag(String pWorkspaceId, String pLabel) throws WorkspaceNotFoundException, AccessRightException, CreationException, TagAlreadyExistsException, UserNotFoundException, UserNotActiveException;

    void deleteDocumentMasterTemplate(DocumentMasterTemplateKey pKey) throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateNotFoundException, UserNotFoundException;

    void deleteDocumentRevision(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException, ESServerException;

    DocumentRevisionKey[] deleteFolder(String pCompletePath) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException, ESServerException;

    void deleteTag(TagKey pKey) throws WorkspaceNotFoundException, AccessRightException, TagNotFoundException, UserNotFoundException;

    BinaryResource getBinaryResource(String pFullName) throws WorkspaceNotFoundException, NotAllowedException, FileNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException;

    DocumentRevision[] getCheckedOutDocumentRevisions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentMasterTemplate getDocumentMasterTemplate(DocumentMasterTemplateKey pKey) throws WorkspaceNotFoundException, DocumentMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentMasterTemplate[] getDocumentMasterTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentRevision getDocumentRevision(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, DocumentRevisionNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, AccessRightException;

    DocumentRevisionKey[] getIterationChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    String[] getFolders(String pCompletePath) throws WorkspaceNotFoundException, FolderNotFoundException, UserNotFoundException, UserNotActiveException;

    User[] getReachableUsers() throws AccountNotFoundException;

    DocumentRevisionKey[] getStateChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    String[] getTags(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    Task[] getTasks(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    User[] getUsers(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException, UserNotFoundException, UserNotActiveException;

    String generateId(String pWorkspaceId, String pDocMTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, DocumentMasterTemplateNotFoundException;

    DocumentRevision[] findDocumentRevisionsByFolder(String pCompletePath) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentRevision[] findDocumentRevisionsByTag(TagKey pKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    boolean isUserStateChangeEventSubscribedForGivenDocument(String pWorkspaceId, DocumentRevision docR) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    DocumentRevision moveDocumentRevision(String pParentFolder, DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, DocumentRevisionNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentRevisionKey[] moveFolder(String pCompletePath, String pDestParentFolder, String pDestFolder) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException, CreationException, FolderAlreadyExistsException;

    DocumentRevision rejectTaskOnDocument(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException;

    DocumentRevision removeFileFromDocument(String pFullName) throws WorkspaceNotFoundException, DocumentRevisionNotFoundException, NotAllowedException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentMasterTemplate removeFileFromTemplate(String pFullName) throws WorkspaceNotFoundException, DocumentMasterTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException;

    BinaryResource saveFileInDocument(DocumentIterationKey pDocPK, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException, AccessRightException;

    BinaryResource saveFileInTemplate(DocumentMasterTemplateKey pDocMTemplateKey, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException, AccessRightException;

    DocumentRevision saveTags(DocumentRevisionKey pDocRPK, String[] pTags) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException, ESServerException;

    DocumentRevision[] searchDocumentRevisions(DocumentSearchQuery pQuery) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, ESServerException;

    void subscribeToIterationChangeEvent(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException;

    void subscribeToStateChangeEvent(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException;

    DocumentRevision undoCheckOutDocument(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, DocumentRevisionNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, AccessRightException;

    void unsubscribeToIterationChangeEvent(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, DocumentRevisionNotFoundException;

    void unsubscribeToStateChangeEvent(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, DocumentRevisionNotFoundException;

    DocumentRevision updateDocument(DocumentIterationKey key, String revisionNote, InstanceAttribute[] attributes, DocumentIterationKey[] linkKeys) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException;

    DocumentMasterTemplate updateDocumentMasterTemplate(DocumentMasterTemplateKey pKey, String pDocumentType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateNotFoundException, UserNotFoundException;

    boolean isUserIterationChangeEventSubscribedForGivenDocument(String pWorkspaceId, DocumentRevision docR) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    DocumentRevision[] getDocumentRevisionsWithAssignedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentRevision[] getDocumentRevisionsWithOpenedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    DocumentRevision[] getDocumentRevisionsWithReference(String pWorkspaceId, String reference, int maxResults) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;

    int getTotalNumberOfDocuments(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException;

    long getDiskUsageForDocumentsInWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException;

    long getDiskUsageForDocumentTemplatesInWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException;

    DocumentRevision createDocumentMaster(String pParentFolder, String pDocMId, String pTitle, String pDescription, String pDocMTemplateId, String pWorkflowModelId, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries, Map<String, String> roleMappings) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, NotAllowedException, FolderNotFoundException, DocumentMasterTemplateNotFoundException, FileAlreadyExistsException, CreationException, DocumentRevisionAlreadyExistsException, RoleNotFoundException, WorkflowModelNotFoundException, DocumentMasterAlreadyExistsException;

    DocumentRevision[] getAllCheckedOutDocumentRevisions(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException;

    SharedDocument createSharedDocument(DocumentRevisionKey pDocRPK, String pPassword, Date pExpireDate) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, UserNotActiveException, NotAllowedException;

    void deleteSharedDocument(SharedEntityKey sharedEntityKey) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, SharedEntityNotFoundException;

    DocumentIteration findDocumentIterationByBinaryResource(BinaryResource pBinaryResource) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException;

    void updateDocumentACL(DocumentRevisionKey docKey, Map<String, String> userEntries, Map<String, String> userGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, AccessRightException;

    void removeACLFromDocumentRevision(DocumentRevisionKey documentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, AccessRightException;

    DocumentRevision[] getAllDocumentsInWorkspace(String workspaceId, int start, int pMaxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    int getDocumentsInWorkspaceCount(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    DocumentRevision removeTag(DocumentRevisionKey pDocMPK, String pTag) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException, ESServerException;
}
