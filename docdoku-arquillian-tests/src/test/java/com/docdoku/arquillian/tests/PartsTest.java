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

package com.docdoku.arquillian.tests;

import com.docdoku.arquillian.tests.services.TestDocumentManagerBean;
import com.docdoku.arquillian.tests.services.TestPartManagerBean;
import com.docdoku.arquillian.tests.services.TestUserManagerBean;
import com.docdoku.core.common.*;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.security.*;
import com.docdoku.core.services.*;
import com.docdoku.server.*;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.esindexer.ESMapper;
import com.docdoku.server.esindexer.ESSearcher;
import com.docdoku.server.esindexer.ESTools;
import com.docdoku.server.gcm.GCMSenderBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Asmae CHADID
 */

@RunWith(Arquillian.class)
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING )
public class PartsTest {

    @EJB
    private ESIndexer esIndexer;

    @EJB
    private TestDocumentManagerBean documentManagerBean;
    @EJB
    private TestUserManagerBean userManagerBean;

    @EJB
    private TestPartManagerBean partManagerBean;

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserTransaction utx;

    private static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(PartsTest.class);
    private static final int COUNT = 5;

    @Deployment
    public static Archive<?> createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "docdoku-arquillian-tests.war")
                .addPackage(Workspace.class.getPackage())
                .addClasses(
                        Account.class,
                        Organization.class,
                        Credential.class,
                        DataManagerBean.class,
                        DocumentManagerBean.class,
                        ESIndexer.class,
                        ESMapper.class,
                        ESSearcher.class,
                        ESTools.class,
                        GCMAccount.class,
                        GCMSenderBean.class,
                        IDataManagerLocal.class,
                        IDocumentManagerLocal.class,
                        IProductManagerLocal.class,
                        IGCMSenderLocal.class,
                        IMailerLocal.class,
                        IUserManagerLocal.class,
                        IWorkspaceManagerLocal.class,
                        JsonValue.class,
                        MailerBean.class,
                        TestDocumentManagerBean.class,
                        TestUserManagerBean.class,
                        UserManagerBean.class,
                        Workspace.class,
                        WorkspaceManagerBean.class,
                        WorkspaceUserMembership.class
                )
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("WEB-INF/sun-web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("log4j.properties", ArchivePaths.create("log4j.properties"));


    }

    @Before
    public void insertData() throws Exception {
        utx.begin();
        em.joinTransaction();
        for (int i = 1; i <= COUNT; i++) {
            Account account = new Account("user" + i, "user" + i, "user" + i + "@docdoku.com", "FR", new Date());
            em.merge(Credential.createCredential(account.getLogin(), "password"));
            em.merge(new UserGroupMapping(account.getLogin()));
            em.merge(account);
        }
        utx.commit();
    }


    @Test
    public void testCreationPartMasterFromTemplate() throws Exception {
        Logger.getLogger(ProductTest.class.getName()).log(Level.INFO, "Test method : testCreationPartMasterFromTemplate");
        org.glassfish.hk2.utilities.reflection.Logger.getLogger().debug("Test method : testCreationPartMasterFromTemplate");
        userManagerBean.testWorkspaceCreation("user1", "TEST_WORKSPACE");
        partManagerBean.testPartMasterTemplateCreation("user1", "TEST_WORKSPACE", "template1", "planes", "plane_###", null, true, true);
        partManagerBean.testPartMasterCreation("user1", "TEST_WORKSPACE", "plane_125", " ", true, null, "", "template1", null, null, null);
        assertTrue(partManagerBean.testPartMasterCreation("user1", "TEST_WORKSPACE", "plane_125", " ", true, null, "", "template1", null, null, null).getNumber().equals("ddd"));
    }


    @Test
    public void testMaskValidityPartTemplate() throws Exception {

        Logger.getLogger(ProductTest.class.getName()).log(Level.INFO, "Test method : testMaskValidityPartTemplate");
        userManagerBean.testWorkspaceCreation("user1", "TEST_WORKSPACE");
        partManagerBean.testPartMasterTemplateCreation("user1", "TEST_WORKSPACE", "template1", "", "ref_###", null, true, true);
        partManagerBean.testPartMasterCreation("user1", "TEST_WORKSPACE", "ref_1235", " ", true, null, "", "template1", null, null, null);
        partManagerBean.testPartMasterCreation("user1", "TEST_WORKSPACE", "ref_125", " ", true, null, "", "template1", null, null, null);
        assertTrue(partManagerBean.findPartMaster("user1", "TEST_WORKSPACE", "ref_1235") == 1);
        assertEquals(partManagerBean.findPartMaster("user1", "TEST_WORKSPACE", "ref_125"), 1);
    }

}
