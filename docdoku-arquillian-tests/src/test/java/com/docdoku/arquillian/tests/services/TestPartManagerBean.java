package com.docdoku.arquillian.tests.services;

import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterTemplate;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.services.IProductManagerLocal;
import com.sun.enterprise.security.ee.auth.login.ProgrammaticLogin;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.Map;

/**
 * @author Asmae CHADID
 */
@LocalBean
@Stateless
public class TestPartManagerBean {

    @EJB
    private IProductManagerLocal productManagerLocal;

    private ProgrammaticLogin loginP = new ProgrammaticLogin();
    private String password = "password";

    public PartMasterTemplate testPartMasterTemplateCreation(String login,String pWorkspaceId,String pId,String pPartType, String pMask,InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated, boolean attributesLocked) throws NotAllowedException, WorkspaceNotFoundException, CreationException, AccessRightException, PartMasterTemplateAlreadyExistsException, UserNotFoundException {
        loginP.login(login, password.toCharArray());
        PartMasterTemplate partMasterTemplate = productManagerLocal.createPartMasterTemplate(pWorkspaceId, pId, pPartType, pMask, pAttributeTemplates, idGenerated, attributesLocked);
        loginP.logout();
        return  partMasterTemplate;
    }

    public PartMaster testPartMasterCreation(String login,String pWorkspaceId, String pNumber, String pName, boolean pStandardPart, String pWorkflowModelId, String pPartRevisionDescription, String templateId, Map<String, String> roleMappings, ACLUserEntry[] userEntries, ACLUserGroupEntry[] userGroupEntries) throws NotAllowedException, WorkspaceNotFoundException, CreationException, AccessRightException, PartMasterTemplateAlreadyExistsException, UserNotFoundException, FileAlreadyExistsException, PartMasterTemplateNotFoundException, PartMasterAlreadyExistsException, RoleNotFoundException, WorkflowModelNotFoundException {
        loginP.login(login, password.toCharArray());
         PartMaster partMaster = productManagerLocal.createPartMaster(pWorkspaceId,pNumber,pName,pStandardPart,pWorkflowModelId,pPartRevisionDescription,templateId,roleMappings,userEntries,userGroupEntries);
        loginP.logout();
        return  partMaster;
    }


    public int findPartMaster(String login,String pWorkspaceId,String pNumber) throws Exception{
        loginP.login(login, password.toCharArray());
        int totalParts =  productManagerLocal.findPartMasters(pWorkspaceId,pNumber,1).size();
        loginP.logout();
        return totalParts;
    }
}
