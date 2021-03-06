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

package com.docdoku.server.products;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductInstanceManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.*;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IProductInstanceManagerLocal.class)
@Stateless(name = "ProductInstanceManagerBean")
public class ProductInstanceManagerBean implements IProductInstanceManagerLocal {
    @PersistenceContext
    private EntityManager em;
    @EJB
    private IUserManagerLocal userManager;

    private static final Logger LOGGER = Logger.getLogger(ProductInstanceManagerBean.class.getName());

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductInstanceMaster> getProductInstanceMasters(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        return new ProductInstanceMasterDAO(em).findProductInstanceMasters(workspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductInstanceMaster> getProductInstanceMasters(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        userManager.checkWorkspaceReadAccess(configurationItemKey.getWorkspace());
        return new ProductInstanceMasterDAO(em).findProductInstanceMasters(configurationItemKey.getId(), configurationItemKey.getWorkspace());
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceMaster getProductInstanceMaster(ProductInstanceMasterKey productInstanceMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(productInstanceMasterKey.getInstanceOf().getWorkspace());
        Locale userLocal = new Locale(user.getLanguage());
        return new ProductInstanceMasterDAO(userLocal,em).loadProductInstanceMaster(productInstanceMasterKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductInstanceIteration> getProductInstanceIterations(ProductInstanceMasterKey productInstanceMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        userManager.checkWorkspaceReadAccess(productInstanceMasterKey.getInstanceOf().getWorkspace());
        return new ProductInstanceIterationDAO(em).findProductInstanceIterationsByMaster(productInstanceMasterKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceIteration getProductInstanceIteration(ProductInstanceIterationKey productInstanceIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(productInstanceIterationKey.getProductInstanceMaster().getInstanceOf().getWorkspace());
        Locale userLocal = new Locale(user.getLanguage());
        return new ProductInstanceIterationDAO(userLocal,em).loadProductInstanceIteration(productInstanceIterationKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<BaselinedPart> getProductInstanceIterationBaselinedPart(ProductInstanceIterationKey productInstanceIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(productInstanceIterationKey.getProductInstanceMaster().getInstanceOf().getWorkspace());
        Locale userLocal = new Locale(user.getLanguage());
        ProductInstanceIteration productInstanceIteration = new ProductInstanceIterationDAO(userLocal,em).loadProductInstanceIteration(productInstanceIterationKey);
        return new ArrayList<>(productInstanceIteration.getPartCollection().getBaselinedParts().values());
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<BaselinedPart> getProductInstanceIterationPartWithReference(ProductInstanceIterationKey productInstanceIterationKey, String q, int maxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(productInstanceIterationKey.getProductInstanceMaster().getInstanceOf().getWorkspace());
        ProductInstanceIterationDAO productInstanceIterationDAO = new ProductInstanceIterationDAO(new Locale(user.getLanguage()),em);
        ProductInstanceIteration productInstanceIteration = productInstanceIterationDAO.loadProductInstanceIteration(productInstanceIterationKey);
        return productInstanceIterationDAO.findBaselinedPartWithReferenceLike(productInstanceIteration.getPartCollection().getId(), q, maxResults);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceMaster createProductInstance(ConfigurationItemKey configurationItemKey, String serialNumber, int baselineId) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, BaselineNotFoundException, CreationException, ProductInstanceAlreadyExistsException {
        User user = userManager.checkWorkspaceWriteAccess(configurationItemKey.getWorkspace());
        Locale userLocal = new Locale(user.getLanguage());
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocal,em);

        try{                                                                                                            // Check if ths product instance already exist
            ProductInstanceMaster productInstanceMaster= productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber,configurationItemKey.getWorkspace(),configurationItemKey.getId()));
            throw new ProductInstanceAlreadyExistsException(userLocal, productInstanceMaster);
        }catch (ProductInstanceMasterNotFoundException e){
            LOGGER.log(Level.FINEST,null,e);
        }

        ConfigurationItem configurationItem = new ConfigurationItemDAO(em).loadConfigurationItem(configurationItemKey);
        ProductInstanceMaster productInstanceMaster = new ProductInstanceMaster(configurationItem,serialNumber);

        ProductInstanceIteration productInstanceIteration = productInstanceMaster.createNextIteration();
        productInstanceIteration.setIterationNote("Initial");

        PartCollection partCollection = new PartCollection();
        new PartCollectionDAO(em).createPartCollection(partCollection);
        partCollection.setAuthor(user);
        partCollection.setCreationDate(new Date());

        ProductBaseline productBaseline = new ProductBaselineDAO(em).loadBaseline(baselineId);
        productInstanceMasterDAO.createProductInstanceMaster(productInstanceMaster);


        for(BaselinedPart baselinedPart : productBaseline.getBaselinedParts().values()){
            partCollection.addBaselinedPart(baselinedPart.getTargetPart());
        }
        productInstanceIteration.setPartCollection(partCollection);
        return productInstanceMaster;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceIteration updateProductInstance(ConfigurationItemKey configurationItemKey, String serialNumber, String iterationNote, List<PartIterationKey> partIterationKeys) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, PartIterationNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(configurationItemKey.getWorkspace());
        Locale userLocal = new Locale(user.getLanguage());
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocal,em);
        ProductInstanceMaster productInstanceMaster= productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber,configurationItemKey.getWorkspace(),configurationItemKey.getId()));

        ProductInstanceIteration productInstanceIteration = productInstanceMaster.createNextIteration();
        new ProductInstanceIterationDAO(userLocal,em).createProductInstanceIteration(productInstanceIteration);

        productInstanceIteration.setIterationNote(iterationNote);
        PartCollection partCollection = new PartCollection();
        new PartCollectionDAO(em).createPartCollection(partCollection);

        partCollection.setAuthor(user);
        partCollection.setCreationDate(new Date());
        for(PartIterationKey partIterationKey : partIterationKeys){
            partCollection.addBaselinedPart(new PartIterationDAO(userLocal,em).loadPartI(partIterationKey));
        }
        productInstanceIteration.setPartCollection(partCollection);
        return productInstanceIteration;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteProductInstance(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale userLocal = new Locale(user.getLanguage());
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocal,em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber,workspaceId,configurationItemId));
        productInstanceMasterDAO.deleteProductInstanceMaster(prodInstM);
    }
}
