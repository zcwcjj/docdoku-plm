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
package com.docdoku.server.documents;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.document.DocumentMasterTemplateKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IDocumentTemplateManagerLocal;
import com.docdoku.core.services.IDocumentTemplateManagerWS;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.core.util.Tools;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.workflow.WorkflowModelKey;
import com.docdoku.server.dao.*;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IDocumentTemplateManagerLocal.class)
@Stateless(name = "DocumentTemplateManagerBean")
@WebService(endpointInterface = "com.docdoku.core.services.IDocumentTemplateManagerWS")
public class DocumentTemplateManagerBean implements IDocumentTemplateManagerWS, IDocumentTemplateManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IDataManagerLocal dataManager;

    private static final Logger LOGGER = Logger.getLogger(DocumentTemplateManagerBean.class.getName());

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public String generateId(String pWorkspaceId, String pDocMTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, DocumentMasterTemplateNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale locale = new Locale(user.getLanguage());
        DocumentMasterTemplate template = new DocumentMasterTemplateDAO(locale, em).loadDocMTemplate(new DocumentMasterTemplateKey(user.getWorkspaceId(), pDocMTemplateId));

        String newId = null;
        try {
            String latestId = new DocumentRevisionDAO(locale, em).findLatestDocMId(pWorkspaceId, template.getDocumentType());
            String inputMask = template.getMask();
            String convertedMask = Tools.convertMask(inputMask);
            newId = Tools.increaseId(latestId, convertedMask);
        } catch (NoResultException ex){
            LOGGER.log(Level.FINER,null,ex);
            //may happen when no document of the specified type has been created
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, null,ex);
            //may happen when a different mask has been used for the same document type
        }
        return newId;

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentMasterTemplate[] getDocumentMasterTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em).findAllDocMTemplates(pWorkspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentMasterTemplate createDocumentMasterTemplate(String pWorkspaceId, String pId, String pDocumentType,
                                                               String pMask, InstanceAttributeTemplate[] pAttributeTemplates,
                                                               boolean idGenerated, boolean attributesLocked,
                                                               String workflowModelId, boolean workflowLocked)
            throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException, WorkflowModelNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Locale locale = new Locale(user.getLanguage());
        checkNameValidity(pId, locale);

        DocumentMasterTemplate template = new DocumentMasterTemplate(user.getWorkspace(), pId, user, pDocumentType, pMask);
        Date now = new Date();
        template.setCreationDate(now);
        template.setIdGenerated(idGenerated);
        template.setAttributesLocked(attributesLocked);

        Set<InstanceAttributeTemplate> attrs = new HashSet<>();
        Collections.addAll(attrs, pAttributeTemplates);
        template.setAttributeTemplates(attrs);

        if(workflowModelId!=null && !workflowModelId.isEmpty()){
            template.setWorkflowLocked(workflowLocked);
            WorkflowModelKey workflowKey = new WorkflowModelKey(pWorkspaceId,workflowModelId);
            WorkflowModel workflowModel = new WorkflowModelDAO(locale,em).loadWorkflowModel(workflowKey);
            template.setWorkflowModel(workflowModel);
        }

        new DocumentMasterTemplateDAO(locale, em).createDocMTemplate(template);
        return template;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentMasterTemplate getDocumentMasterTemplate(DocumentMasterTemplateKey pKey)
            throws WorkspaceNotFoundException, DocumentMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em).loadDocMTemplate(pKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentMasterTemplate updateDocumentMasterTemplate(DocumentMasterTemplateKey pKey,
                                                               String pDocumentType, String pMask,
                                                               InstanceAttributeTemplate[] pAttributeTemplates,
                                                               boolean idGenerated, boolean attributesLocked,
                                                               String workflowModelId, boolean workflowLocked)
            throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateNotFoundException, UserNotFoundException, WorkflowModelNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());

        DocumentMasterTemplateDAO templateDAO = new DocumentMasterTemplateDAO(locale, em);
        DocumentMasterTemplate template = templateDAO.loadDocMTemplate(pKey);
        Date now = new Date();
        template.setCreationDate(now);
        template.setAuthor(user);
        template.setDocumentType(pDocumentType);
        template.setMask(pMask);
        template.setIdGenerated(idGenerated);
        template.setAttributesLocked(attributesLocked);

        Set<InstanceAttributeTemplate> attrs = new HashSet<>();
        Collections.addAll(attrs, pAttributeTemplates);

        Set<InstanceAttributeTemplate> attrsToRemove = new HashSet<>(template.getAttributeTemplates());
        attrsToRemove.removeAll(attrs);

        InstanceAttributeTemplateDAO attrDAO = new InstanceAttributeTemplateDAO(em);
        for (InstanceAttributeTemplate attrToRemove : attrsToRemove) {
            attrDAO.removeAttribute(attrToRemove);
        }

        if(workflowModelId!=null && !workflowModelId.isEmpty()){
            template.setWorkflowLocked(workflowLocked);
            WorkflowModelKey workflowKey = new WorkflowModelKey(pKey.getWorkspaceId(),workflowModelId);
            WorkflowModel workflowModel = new WorkflowModelDAO(locale,em).loadWorkflowModel(workflowKey);
            template.setWorkflowModel(workflowModel);
        } else {
            template.setWorkflowModel(null);
            template.setWorkflowLocked(false);
        }

        template.setAttributeTemplates(attrs);
        return template;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteDocumentMasterTemplate(DocumentMasterTemplateKey pKey)
            throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        DocumentMasterTemplateDAO templateDAO = new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em);
        DocumentMasterTemplate template = templateDAO.removeDocMTemplate(pKey);
        for (BinaryResource file : template.getAttachedFiles()) {
            try {
                dataManager.deleteData(file);
            } catch (StorageException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public long getDiskUsageForDocumentTemplatesInWorkspace(String pWorkspaceId)
            throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException {
        Account account = userManager.checkAdmin(pWorkspaceId);
        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(new Locale(account.getLanguage()),em);
        return documentRevisionDAO.getDiskUsageForDocumentTemplatesInWorkspace(pWorkspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource saveFileInTemplate(DocumentMasterTemplateKey pDocMTemplateKey, String pName, long pSize)
            throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException, AccessRightException {
        User user = userManager.checkWorkspaceWriteAccess(pDocMTemplateKey.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(pName, locale);

        DocumentMasterTemplateDAO templateDAO = new DocumentMasterTemplateDAO(locale,em);
        DocumentMasterTemplate template = templateDAO.loadDocMTemplate(pDocMTemplateKey);
        BinaryResource binaryResource = null;
        String fullName = template.getWorkspaceId() + "/document-templates/" + template.getId() + "/" + pName;

        for (BinaryResource bin : template.getAttachedFiles()) {
            if (bin.getFullName().equals(fullName)) {
                binaryResource = bin;
                break;
            }
        }

        if (binaryResource == null) {
            binaryResource = new BinaryResource(fullName, pSize, new Date());
            new BinaryResourceDAO(locale,em).createBinaryResource(binaryResource);
            template.addFile(binaryResource);
        } else {
            binaryResource.setContentLength(pSize);
            binaryResource.setLastModified(new Date());
        }
        return binaryResource;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentMasterTemplate removeFileFromTemplate(String pFullName)
            throws WorkspaceNotFoundException, DocumentMasterTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceWriteAccess(BinaryResource.parseWorkspaceId(pFullName));

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        DocumentMasterTemplate template = binDAO.getDocumentTemplateOwner(file);
        try {
            dataManager.deleteData(file);
        } catch (StorageException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        template.removeFile(file);
        binDAO.removeBinaryResource(file);
        return template;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public BinaryResource getTemplateBinaryResource(String pFullName)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());
        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        return binDAO.loadBinaryResource(pFullName);
    }


    private void checkNameValidity(String name, Locale locale) throws NotAllowedException {
        if (!NamingConvention.correct(name)) {
            throw new NotAllowedException(locale, "NotAllowedException9");
        }
    }
    private void checkNameFileValidity(String name, Locale locale) throws NotAllowedException {
        if (!NamingConvention.correctNameFile(name)) {
            throw new NotAllowedException(locale, "NotAllowedException9");
        }
    }
}