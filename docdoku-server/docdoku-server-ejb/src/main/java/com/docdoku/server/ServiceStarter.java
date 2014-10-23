package com.docdoku.server;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by docdoku on 27/10/14.
 */
@Singleton
@Startup
public class ServiceStarter {
    @Resource(mappedName = "jdbc/docdokuPU")
    private DataSource dataSource;

    private static final String CONF_PROPERTIES="/com/docdoku/server/liquibase/liquibase.properties";
    private static final String I18N_CONF="com.docdoku.core.i18n.LocalStrings";
    private static final Properties CONF = new Properties();
    private static final Logger LOGGER = Logger.getLogger(ServiceStarter.class.getName());

    static{
        InputStream inputStream = null;
        try {
            inputStream = ServiceStarter.class.getResourceAsStream(CONF_PROPERTIES);
            CONF.load(inputStream);
        } catch (IOException e) {
            String message = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString("ES_ConfWarning1");
            Logger.getLogger(ServiceStarter.class.getName()).log(Level.WARNING,message,e);
        } finally {
            try{
                if(inputStream!=null){
                    inputStream.close();
                }
            }catch (IOException e){
                Logger.getLogger(ServiceStarter.class.getName()).log(Level.WARNING,null,e);
            }
        }
    }

    @PostConstruct
    public void onInitialize() {
        try {
            Connection connection = dataSource.getConnection();
            ResourceAccessor classLoaderResourceAccessor = new ClassLoaderResourceAccessor();
            ResourceAccessor fileSystemResourceAccessor = new FileSystemResourceAccessor();
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            LOGGER.info("Database version : "+database.getDatabaseProductName()+" - "+database.getDatabaseProductVersion());
            database.setDefaultSchemaName("APP");
            Liquibase liquibase = new Liquibase(CONF.getProperty("changeLogFile"),
                    new CompositeResourceAccessor(classLoaderResourceAccessor, fileSystemResourceAccessor), database);
            liquibase.update("dev");
        } catch (SQLException | LiquibaseException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }
}
