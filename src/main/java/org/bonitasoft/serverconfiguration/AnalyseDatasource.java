package org.bonitasoft.serverconfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.serverconfiguration.Analyse.AnalyseRecommendation;
import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;
import org.bonitasoft.serverconfiguration.CollectResult.ClassCollect;
import org.bonitasoft.serverconfiguration.content.ContentType;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties.KeyProperties;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties.KeyPropertiesReader;
import org.bonitasoft.serverconfiguration.content.ContentTypeText;
import org.bonitasoft.serverconfiguration.content.ContentTypeXml;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class AnalyseDatasource extends Analyse{
    
    
    public static AnalyseDatasource getInstance() {
        return new AnalyseDatasource();
    }
    /**
     * analyse the number of datasource in Bonita / Tomcat is enough
     * 
     */

    @Override
    public String getTitle() {
       return "DataSource Bonita Engine connection";
    }

    @Override
    public String getName() {
        return "bonitadatasource";    
                }

    private class AnalyseTenant {
        public long worker=0;
    }
    @Override
    public void analyse(CollectResult collectResult) {
        
        ClassCollect classCollect = collectResult.getClassCollect( TYPECOLLECT.ANALYSIS.toString());

        try
        {
            
            // Assuming server.xml is a XML
            long maxThreads=0;
            ContentTypeText contentTextServer = classCollect.getContentTextByFileName("server.xml");
            if (contentTextServer !=null) {
                ContentTypeXml contentXML=  new ContentTypeXml( contentTextServer.getContent() );
            
                Element nodeConnector= contentXML.getXmlElement("Connector", "protocol","HTTP/1.1");
                if (nodeConnector!=null) {
                    maxThreads = Long.valueOf( nodeConnector.getAttribute("maxThreads"));
                }
                setInfo("Tomcat Thread", maxThreads);
            }
            // get datasource now
            long maxTotalDatasource=0;
            String infoDatasource="";
            ContentTypeText contentDatasource = classCollect.getContentTextByFileName("bonita.xml");
            if (contentDatasource !=null) {
                ContentTypeXml contentXML= new ContentTypeXml( contentDatasource.getContent() );
                Element nodeResource= contentXML.getXmlElement("Resource", "name", "bonitaDS");
                if (nodeResource == null)  {
                    infoDatasource+="Can't found the [bonita.xml] file";
                }else {
                    String factory = nodeResource.getAttribute("factory");
                    if (factory !=null && "bitronix.tm.resource.ResourceObjectFactory".equals(factory)) 
                    {
                        infoDatasource+="Bitronix usage [bitronix-resources.properties/resource.ds1.maxPoolSize];";
                        KeyPropertiesReader contentBitronix = classCollect.getKeyPropertiesByFileName("bitronix-resources.properties");
                        if (contentBitronix == null)
                            infoDatasource+="Can't found the [bitronix-resources.properties] file";
                        else
                            maxTotalDatasource=contentBitronix.getLongValue("resource.ds1.maxPoolSize", 0L);
                    } else {
                        
                        maxTotalDatasource =  Long.valueOf( nodeResource.getAttribute("maxTotal"));
                        infoDatasource+="Direct datasource usage [bonita.xml/resource.ds1.maxPoolSize/maxTotal]";
                    }
                } 
            }
            Map<Long, AnalyseTenant> mapPerTenant = new HashMap<Long,AnalyseTenant>();
            List<KeyPropertiesReader> keyProperties = classCollect.mapKeyPropertiesReader.get("conf");
            // search ""
            long totalQuartz=0;
            
            for (KeyPropertiesReader keyPropertiesReader : keyProperties)
            {
                  
                if (keyPropertiesReader.getFileName().equals("bonita-platform-community-custom.properties" ) && ! keyPropertiesReader.isInitial())
                {
                    // search bonita.platform.scheduler.quartz.threadpool.size=
                    
                    totalQuartz = keyPropertiesReader.getLongValue( "bonita.platform.scheduler.quartz.threadpool.size", 0L );
                    
                }
                if (keyPropertiesReader.getFileName().equals("bonita-tenant-community-custom.properties") && ! keyPropertiesReader.isInitial()) 
                {
                    AnalyseTenant analysePerTenant = mapPerTenant.get(keyPropertiesReader.getTenantId());
                    if (analysePerTenant == null)
                        analysePerTenant = new AnalyseTenant();
                    mapPerTenant.put(keyPropertiesReader.getTenantId(), analysePerTenant);
                 
                    analysePerTenant.worker = keyPropertiesReader.getLongValue( "bonita.tenant.work.maximumPoolSize", 0L);
                }
            }
            
            long totalWorker=0;
                   
            for (Long tenantId : mapPerTenant.keySet())
            {
                AnalyseTenant analysePerTenant = mapPerTenant.get(tenantId);
                setInfo("Workers Thread ("+tenantId+") [bonita-tenant-community-custom.properties/bonita.tenant.work.maximumPoolSize]", analysePerTenant.worker);
                
                totalWorker+=analysePerTenant.worker;
            }
            setInfo("Quartz Thread [bonita-platform-community-custom.properties/bonita.platform.scheduler.quartz.threadpool.size]", totalQuartz);
            setInfo("Database Max connection "+infoDatasource, maxTotalDatasource);
            
        
            // recomendation : size
            AnalyseRecommendation recommendation = new AnalyseRecommendation();
            recommendation.name = "Data Source Pool size";
            recommendation.value = Long.valueOf( maxThreads+totalWorker+totalQuartz );
            long totalUsage = maxThreads+totalWorker+totalQuartz;
            
            if ( maxTotalDatasource >= totalUsage ) {
                recommendation.level= LEVELRECOMMENDATION.SUCCESS;
                recommendation.whatToDo="You have enough connection (required "+totalUsage+") Server Data Source Pool size has "+maxTotalDatasource+" connections";
            }
            else if ( maxTotalDatasource >= totalUsage * 0.8 ) {
                recommendation.level= LEVELRECOMMENDATION.INFO;
                recommendation.whatToDo="You have about 80 to 100 % connection. It should be enougth. You can set "+totalUsage+" to be sure to have enougth connection";
            }
            else if ( maxTotalDatasource >= totalUsage * 0.5 ) {
                recommendation.level= LEVELRECOMMENDATION.WARNING;
                recommendation.whatToDo="You shoud Increase the number of datasource to "+totalUsage+ " (you have currently "+maxTotalDatasource+")";
            }
            else {
                recommendation.level= LEVELRECOMMENDATION.DANGER;
                recommendation.whatToDo="You shoud Increase the number of datasource to "+totalUsage+ " (you have currently "+maxTotalDatasource+")";
            }
            recommendation.explanation="Total usage of datasource is "+totalUsage+", when Server Data Source Pool size is "+maxTotalDatasource;
            
            
            addRecommendations( recommendation );
        }
        catch(Exception e) {
            setErrorAnalyse( "Calculation ",e);
        }
    }

}
