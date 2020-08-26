package org.bonitasoft.serverconfiguration.analyse;

import org.bonitasoft.serverconfiguration.CollectOperation.BonitaAccessor;
import org.bonitasoft.serverconfiguration.analyse.Analyse.AnalyseRecommendation;
import org.bonitasoft.serverconfiguration.analyse.Analyse.LEVELRECOMMENDATION;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.bonitasoft.serverconfiguration.CollectResult;

/* ******************************************************************************** */
/*                                                                                  */
/* Analyse Environement */
/*                                                                                  */
/*
 * According
 * https://documentation.bonitasoft.com/bonita/7.11/install-a-bonita-bpm-cluster?searchRequest=ehcache
 * if the cluster is ON, the cache must be off
 */
/*                                                                                  */
/* ******************************************************************************** */

public class AnalyseEnvironment extends Analyse {

    @Override
    public String getTitle() {
        return "Environment (Java, Database)";
    }

    @Override
    public String getName() {
        return "Environment";
    }

    @Override
    public void analyse(CollectResult collectResult, BonitaAccessor apiAccessor) {
        
        String osName = System.getProperty("os.name");
        setInfo("OS", osName);

        
        
        String version = System.getProperty("java.version");
        setInfo("Java Version", version);
        AnalyseRecommendation recommendation = new AnalyseRecommendation();
        recommendation.name = "Java version";
        recommendation.value = version;
        try {
            StringTokenizer st = new StringTokenizer(version, ".");
            int mainVersion = st.hasMoreTokens() ? Integer.valueOf(st.nextToken()) : 0;
            int minorVersion = st.hasMoreTokens() ? Integer.valueOf(st.nextToken()) : 0;

            recommendation.level = LEVELRECOMMENDATION.SUCCESS;
            if (mainVersion < 1 && minorVersion < 8) {
                recommendation.level = LEVELRECOMMENDATION.DANGER;
                recommendation.explanation = "Use a JDK > 1.8";
            }
        } catch (Exception e) {
            recommendation.level = LEVELRECOMMENDATION.WARNING;
            recommendation.explanation = "Can't decode the JDK used";

        }
        addRecommendations(recommendation);

        try (Connection con = getConnection()) {
            if (con==null)
                setInfo("Database Name", "Can't access the datasource");
            else {
                DatabaseMetaData metaData = con.getMetaData();
    
                setInfo("Database Name", metaData.getDatabaseProductName());
                setInfo("Database Version", metaData.getDatabaseProductVersion() +"("+ metaData.getDatabaseMajorVersion() + "." + metaData.getDatabaseMinorVersion()+")");
            }
        } catch (Exception e) {
            setInfo("Database", "Can't access Database information " + e.getMessage());
        }
        
        setInfo("Visit https://documentation.bonitasoft.com/bonita/7.11/hardware-and-software-requirements","");
    }

    /**
     * to avoid the multiple dependency, copy this method from BonitaProperties.BonitaEngineConnection, waiting this method is maybe available at Bonita ?
     */
    protected final static String[] listDataSources = new String[] {
            "java:/comp/env/RawBonitaDS", // 7.
            "java:/comp/env/bonitaSequenceManagerDS", // tomcat
            "java:jboss/datasources/bonitaSequenceManagerDS" }; // jboss

    public static Connection getConnection() throws SQLException {
        // logger.info(loggerLabel+".getDataSourceConnection() start");

        List<String> listDatasourceToCheck = new ArrayList<String>();
        for (String dataSourceString : listDataSources)
            listDatasourceToCheck.add(dataSourceString);

        for (String dataSourceString : listDatasourceToCheck) {
            // logger.info(loggerLabel + ".getDataSourceConnection() check[" + dataSourceString + "]");
            try {
                final Context ctx = new InitialContext();
                final DataSource dataSource = (DataSource) ctx.lookup(dataSourceString);
                // logger.info(loggerLabel + ".getDataSourceConnection() [" + dataSourceString + "] isOk");
                return dataSource.getConnection();

            } catch (NamingException e) {
                // logger.info(loggerLabel + ".getDataSourceConnection() error[" + dataSourceString + "] : " + e.toString());
                // msg += "DataSource[" + dataSourceString + "] : error " + e.toString() + ";";
            }
        }
        // logger.severe(loggerLabel + ".getDataSourceConnection: Can't found a datasource : " + msg);
        return null;
    }
}
