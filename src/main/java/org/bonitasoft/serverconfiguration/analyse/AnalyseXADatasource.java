package org.bonitasoft.serverconfiguration.analyse;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

import org.bonitasoft.properties.BonitaEngineConnection;
import org.bonitasoft.serverconfiguration.CollectResult;
import org.bonitasoft.serverconfiguration.CollectOperation.BonitaAccessor;
import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;
import org.bonitasoft.serverconfiguration.CollectResult.ClassCollect;
import org.bonitasoft.serverconfiguration.analyse.Analyse.AnalyseRecommendation;
import org.bonitasoft.serverconfiguration.analyse.Analyse.LEVELRECOMMENDATION;

import org.bonitasoft.serverconfiguration.content.ContentTypeText;
import org.bonitasoft.serverconfiguration.content.ContentTypeXml;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties.KeyPropertiesReader;
import org.w3c.dom.Element;

public class AnalyseXADatasource extends Analyse {

    public static AnalyseDatasource getInstance() {
        return new AnalyseDatasource();
    }

    /**
     * analyse the number of datasource in Bonita / Tomcat is enough
     */

    @Override
    public String getTitle() {
        return "XA DataSource connection";
    }

    @Override
    public String getName() {
        return "bonitaXADatasource";
    }

    @Override
    public void analyse(CollectResult collectResult, BonitaAccessor apiAccessor) {
          
        Connection conBonita = null;
        Connection conBusiness = null;
        try {
            /*
             * Note that javax.sql.XADataSource is used instead of a specific
             * driver implementation such as com.ibm.db2.jcc.DB2XADataSource.
             */
            conBonita = BonitaEngineConnection.getConnection();
            conBusiness = BonitaEngineConnection.getBusinessConnection();

            if (!(conBonita instanceof javax.sql.XADataSource)) {
            }
            if (!(conBusiness instanceof javax.sql.XADataSource)) {
            }
            XADataSource xaDS1 = (javax.sql.XADataSource) conBonita;
            XADataSource xaDS2 = (javax.sql.XADataSource) conBusiness;

            // The XADatasource contains the user ID and password.
            // Get the XAConnection object from each XADataSource
            XAConnection xaconn1 = xaDS1.getXAConnection();
            XAConnection xaconn2 = xaDS2.getXAConnection();

            // Get the XAResource object from each XAConnection
            XAResource xares1 = xaconn1.getXAResource();
            XAResource xares2 = xaconn2.getXAResource();
            // Create the Xid object for this distributed transaction.
            // This example uses the com.ibm.db2.jcc.DB2Xid implementation
            // of the Xid interface. This Xid can be used with any JDBC driver
            // that supports JTA.
            javax.transaction.xa.Xid xid1 = null;
            // new com.ibm.db2.jcc.DB2Xid(100, gtrid, bqual);

            // Start the distributed transaction on the two connections.
            // The two connections do NOT need to be started and ended together.
            // They might be done in different threads, along with their SQL operations.
            xares1.start(xid1, javax.transaction.xa.XAResource.TMNOFLAGS);
            xares2.start(xid1, javax.transaction.xa.XAResource.TMNOFLAGS);

            // Do the SQL operations on connection 1.
            // Do the SQL operations on connection 2.

            // Now end the distributed transaction on the two connections.
            xares1.end(xid1, javax.transaction.xa.XAResource.TMSUCCESS);
            xares2.end(xid1, javax.transaction.xa.XAResource.TMSUCCESS);

            // If connection 2 work had been done in another thread,
            // a thread.join() call would be needed here to wait until the
            // connection 2 work is done.

            /*
             * try
             * { // Now prepare both branches of the distributed transaction.
             * // Both branches must prepare successfully before changes
             * // can be committed.
             * // If the distributed transaction fails, an XAException is thrown.
             * rc1 = xares1.prepare(xid1);
             * if(rc1 == javax.transaction.xa.XAResource.XA_OK)
             * { // Prepare was successful. Prepare the second connection.
             * rc2 = xares2.prepare(xid1);
             * if(rc2 == javax.transaction.xa.XAResource.XA_OK)
             * { // Both connections prepared successfully and neither was read-only.
             * xares1.commit(xid1, false);
             * xares2.commit(xid1, false);
             * }
             * else if(rc2 == javax.transaction.xa.XAException.XA_RDONLY)
             * { // The second connection is read-only, so just commit the
             * // first connection.
             * xares1.commit(xid1, false);
             * }
             * }
             * else if(rc1 == javax.transaction.xa.XAException.XA_RDONLY)
             * { // SQL for the first connection is read-only (such as a SELECT).
             * // The prepare committed it. Prepare the second connection.
             * rc2 = xares2.prepare(xid1);
             * if(rc2 == javax.transaction.xa.XAResource.XA_OK)
             * { // The first connection is read-only but the second is not.
             * // Commit the second connection.
             * xares2.commit(xid1, false);
             * }
             * else if(rc2 == javax.transaction.xa.XAException.XA_RDONLY)
             * { // Both connections are read-only, and both already committed,
             * // so there is nothing more to do.
             * }
             * }
             */

        } catch (Exception e) {
            setErrorAnalyse("Calculation ", e);
        } finally {
            try {
                if (conBonita != null)
                    conBonita.close();
            } catch (Exception e) {
            }
            try {
                if (conBusiness != null)
                    conBusiness.close();
            } catch (Exception e) {
            }
        }
    }
}
