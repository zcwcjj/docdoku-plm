package com.docdoku.server;

import com.docdoku.core.change.ChangeItem;
import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.UserKey;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.security.WorkspaceUserGroupMembership;
import com.docdoku.core.security.WorkspaceUserMembership;
import com.docdoku.core.security.WorkspaceUserMembershipKey;
import com.docdoku.core.services.IAccessRightLocal;
import com.docdoku.server.dao.*;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Locale;

@DeclareRoles({"users","admin"})
@Stateless(name = "AccessRightBean")
public class AccessRightBean implements IAccessRightLocal {
    @PersistenceContext
    private EntityManager em;
    @Resource
    private SessionContext ctx;


    @Override
    @RolesAllowed({"users"})
    public User checkChangeItemReadAccess(ChangeItem pChangeItem) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        User user = checkWorkspaceReadAccess(pChangeItem.getWorkspaceId());                                             // Check if the user have read access on the workspace
        if(user.isAdministrator()                                                                                       // Check if it is the workspace's administrator
                || pChangeItem.getACL()==null                                                                           // Check if the item haven't ACL
                || pChangeItem.getACL().hasReadAccess(user)){                                                           // Check if ACL grant read access
            return user;
        }else{                                                                                                          // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()),user);
        }
    }

    @Override
    @RolesAllowed({"users"})
    public User checkChangeItemWriteAccess(ChangeItem pChangeItem) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        User user = checkWorkspaceReadAccess(pChangeItem.getWorkspaceId());                                             // Check if the user have read access on the workspace
        if(user.isAdministrator()){                                                                                     // Check if it is the workspace's administrator
            return user;
        }
        if(pChangeItem.getACL()==null){                                                                                 // Check if the item haven't ACL
            return checkWorkspaceWriteAccess(user, pChangeItem.getWorkspace());
        }else if(pChangeItem.getACL().hasWriteAccess(user)){                                                            // Check if there is a write access
            return user;
        }else{                                                                                                          // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()),user);
        }
    }

    @Override
    @RolesAllowed({"users"})
    public User checkDocumentIterationReadAccess(DocumentIterationKey pDocumentIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException {
        DocumentRevisionKey documentRevisionKey= pDocumentIterationKey.getDocumentRevision();
        User user = checkDocumentRevisionReadAccess(documentRevisionKey);
        DocumentRevision documentRevision = new DocumentRevisionDAO(em).loadDocR(documentRevisionKey);
        if (!documentRevision.isCheckedOut()){
            return user;
        }else if(!(documentRevision.getLastIteration().getKey().equals(pDocumentIterationKey))){
            return user;
        } else if(!documentRevision.getCheckOutUser().equals(user)){
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        return user;
    }

    @Override
    @RolesAllowed({"users"})
    public User checkDocumentIterationWriteAccess(DocumentIterationKey pDocumentIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, DocumentRevisionNotFoundException {
        DocumentRevisionKey documentRevisionKey= pDocumentIterationKey.getDocumentRevision();
        User user = checkDocumentRevisionWriteAccess(documentRevisionKey);
        DocumentRevision documentRevision = new DocumentRevisionDAO(em).loadDocR(documentRevisionKey);
        if ((!documentRevision.isCheckedOut())
                || (!documentRevision.getCheckOutUser().equals(user))
                || (!documentRevision.getLastIteration().getKey().equals(pDocumentIterationKey))){
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        return user;
    }

    @Override
    @RolesAllowed({"users"})
    public User checkDocumentRevisionReadAccess(DocumentRevisionKey pDocumentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException {
        User user = checkWorkspaceReadAccess(pDocumentRevisionKey.getDocumentMaster().getWorkspace());
        DocumentRevision documentRevision = new DocumentRevisionDAO(em).loadDocR(pDocumentRevisionKey);
        String owner = documentRevision.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException5");
        }
        if(user.isAdministrator()){                                                                                     // If it is the workspace's owner, give it access
            return user;
        }
        if((documentRevision.getACL()==null) || (documentRevision.getACL().hasReadAccess(user))){                       // If the document haven't ACL, give it access or if there is a read access
            return user;
        }else{                                                                                                          // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()),user);
        }
    }

    @Override
    @RolesAllowed({"users"})
    public User checkDocumentRevisionCheckoutAccess(DocumentRevisionKey pDocumentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException {
        User user = checkWorkspaceReadAccess(pDocumentRevisionKey.getDocumentMaster().getWorkspace());
        DocumentRevision documentRevision = new DocumentRevisionDAO(em).loadDocR(pDocumentRevisionKey);
        String owner = documentRevision.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException5");
        }
        if(user.isAdministrator()){
            return user;
        }
        if(documentRevision.getACL()==null){
            return checkWorkspaceWriteAccess(user,documentRevision.getDocumentMaster().getWorkspace());
        }else if(documentRevision.getACL().hasWriteAccess(user)){
            return user;
        }else{
            throw new AccessRightException(new Locale(user.getLanguage()),user);
        }
    }

    @Override
    @RolesAllowed({"users"})
    public User checkDocumentRevisionWriteAccess(DocumentRevisionKey pDocumentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException {
        User user = checkDocumentRevisionCheckoutAccess(pDocumentRevisionKey);
        DocumentRevision documentRevision = new DocumentRevisionDAO(em).loadDocR(pDocumentRevisionKey);
        if (documentRevision.isCheckedOut() && documentRevision.getCheckOutUser().equals(user)) {
            return user;
        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException25");
        }
    }

    @Override
    @RolesAllowed({"users"})
    public User checkDocumentRevisionGrantAccess(DocumentRevisionKey pDocumentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException {
        User user = checkWorkspaceReadAccess(pDocumentRevisionKey.getDocumentMaster().getWorkspace());
        DocumentRevision documentRevision = new DocumentRevisionDAO(em).loadDocR(pDocumentRevisionKey);
        String owner = documentRevision.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException5");
        }
        if(user.isAdministrator() || documentRevision.getAuthor().getLogin().equals(user.getLogin())){                                                                                     // If it is the workspace's owner, give it access
            return user;
        }else{
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }

    @Override
    @RolesAllowed({"users"})
    public User checkPartIterationReadAccess(PartIterationKey pPartIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartRevisionNotFoundException {
        PartRevisionKey partRevisionKey= pPartIterationKey.getPartRevision();
        User user = checkPartRevisionReadAccess(partRevisionKey);
        PartRevision partRevision = new PartRevisionDAO(em).loadPartR(partRevisionKey);
        if (!partRevision.isCheckedOut()){
            return user;
        }else if(!(partRevision.getLastIteration().getKey().equals(pPartIterationKey))){
            return user;
        } else if(!partRevision.getCheckOutUser().equals(user)){
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        return user;
    }

    @Override
    @RolesAllowed({"users"})
    public User checkPartIterationWriteAccess(PartIterationKey pPartIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, PartRevisionNotFoundException {
        PartRevisionKey partRevisionKey= pPartIterationKey.getPartRevision();
        User user = checkPartRevisionWriteAccess(partRevisionKey);
        PartRevision partRevision = new PartRevisionDAO(em).loadPartR(partRevisionKey);
        if ((!partRevision.isCheckedOut())
                || (!partRevision.getCheckOutUser().equals(user))
                || (!partRevision.getLastIteration().getKey().equals(pPartIterationKey))){
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        return user;
    }

    @Override
    @RolesAllowed({"users"})
    public User checkPartRevisionReadAccess(PartRevisionKey pPartRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartRevisionNotFoundException {
        User user = checkWorkspaceReadAccess(pPartRevisionKey.getPartMaster().getWorkspace());
        PartRevision partRevision = new PartRevisionDAO(new Locale(user.getLanguage()),em).loadPartR(pPartRevisionKey);
        if(user.isAdministrator()){                                                                                     // If it is the workspace's owner, give it access
            return user;
        }
        if((partRevision.getACL()==null) || (partRevision.getACL().hasReadAccess(user))){                       // If the document haven't ACL, give it access or if there is a read access
            return user;
        }else{                                                                                                          // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()),user);
        }
    }

    @Override
    @RolesAllowed({"users"})
    public User checkPartRevisionCheckoutAccess(PartRevisionKey pPartRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartRevisionNotFoundException {
        User user = checkWorkspaceReadAccess(pPartRevisionKey.getPartMaster().getWorkspace());
        PartRevision partRevision = new PartRevisionDAO(new Locale(user.getLanguage()),em).loadPartR(pPartRevisionKey);
        if(user.isAdministrator()){
            return user;
        }
        if(partRevision.getACL()==null){
            return checkWorkspaceWriteAccess(user,partRevision.getPartMaster().getWorkspace());
        }else if(partRevision.getACL().hasWriteAccess(user)){
            return user;
        }else{
            throw new AccessRightException(new Locale(user.getLanguage()),user);
        }
    }

    @Override
    @RolesAllowed({"users"})
    public User checkPartRevisionWriteAccess(PartRevisionKey pPartRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartRevisionNotFoundException, NotAllowedException {
        User user = checkWorkspaceReadAccess(pPartRevisionKey.getPartMaster().getWorkspace());
        PartRevision partRevision = new PartRevisionDAO(new Locale(user.getLanguage()),em).loadPartR(pPartRevisionKey);
        if (partRevision.isCheckedOut() && partRevision.getCheckOutUser().equals(user)) {
            return user;
        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException25");
        }
    }

    @Override
    @RolesAllowed({"users"})
    public User checkPartRevisionGrantAccess(PartRevisionKey pPartRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartRevisionNotFoundException {
        User user = checkWorkspaceReadAccess(pPartRevisionKey.getPartMaster().getWorkspace());
        PartRevision partRevision = new PartRevisionDAO(new Locale(user.getLanguage()),em).loadPartR(pPartRevisionKey);
        if(user.isAdministrator() || partRevision.getAuthor().getLogin().equals(user.getLogin())){                                                                                     // If it is the workspace's owner, give it access
            return user;
        }else{
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }

    @Override
    @RolesAllowed({"users"})
    public User checkWorkspaceReadAccess(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        String login = ctx.getCallerPrincipal().toString();

        UserDAO userDAO = new UserDAO(em);
        User user = userDAO.loadUser(new UserKey(pWorkspaceId, login));
        if (user.isAdministrator()) {                                                                  //Check if it's the Workspace's Admin
            return user;
        }
        WorkspaceUserMembership userMS = userDAO.loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
        if (userMS != null) {
            return userMS.getMember();
        }
        WorkspaceUserGroupMembership[] groupMS = new UserGroupDAO(em).getUserGroupMemberships(pWorkspaceId, user);
        if (groupMS.length > 0) {
            return user;
        } else {
            throw new UserNotActiveException(Locale.getDefault(), login);
        }
    }

    @Override
    @RolesAllowed({"users"})
    public User checkWorkspaceWriteAccess(String pWorkspaceId) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException {
        String login = ctx.getCallerPrincipal().toString();
        Workspace wks = new WorkspaceDAO(em).loadWorkspace(pWorkspaceId);
        User user = new UserDAO(em).loadUser(new UserKey(pWorkspaceId, login));
        return checkWorkspaceWriteAccess(user,wks);
    }

    @RolesAllowed({"users","admin"})
    @Override
    public Account checkWorkspaceAdminAccess(String pWorkspaceId) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
        if(ctx.isCallerInRole("admin")){
            return account;
        }

        Workspace wks = new WorkspaceDAO(new Locale(account.getLanguage()), em).loadWorkspace(pWorkspaceId);
        if (!wks.getAdmin().equals(account)) {                                                                          //Check if it isn't the Workspace's Admin
            throw new AccessRightException(new Locale(account.getLanguage()), account);
        }
        return account;
    }

    /**
     * Check if a user have access to a workspace
     * @param user User to check
     * @param wks Workspace to check
     * @return the user if he have access to the workspace, throw exception otherwise
     * @throws com.docdoku.core.exceptions.AccessRightException
     */
    private User checkWorkspaceWriteAccess(User user, Workspace wks) throws AccessRightException {
        String login = user.getLogin();
        String workspaceId = wks.getId();
        if (user.isAdministrator()) {                                                                                   //Check if it's the Workspace's Admin
            return user;
        }
        WorkspaceUserMembership userMS = new UserDAO(em).loadUserMembership(new WorkspaceUserMembershipKey(workspaceId, workspaceId, login));
        if (userMS != null) {
            if (userMS.isReadOnly()) {
                throw new AccessRightException(new Locale(user.getLanguage()), user);
            } else {
                return userMS.getMember();
            }
        }
        WorkspaceUserGroupMembership[] groupMS = new UserGroupDAO(em).getUserGroupMemberships(workspaceId, user);
        for (WorkspaceUserGroupMembership ms : groupMS) {
            if (!ms.isReadOnly()) {
                return user;
            }
        }
        throw new AccessRightException(new Locale(user.getLanguage()), user);
    }

}