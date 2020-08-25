package org.bonitasoft.serverconfiguration.analyse;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.serverconfiguration.CollectResult;
import org.bonitasoft.serverconfiguration.CollectOperation.BonitaAccessor;
import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;
import org.bonitasoft.serverconfiguration.CollectResult.ClassCollect;
import org.bonitasoft.serverconfiguration.CollectResult.TYPECOLLECTOR;
import org.bonitasoft.serverconfiguration.analyse.Analyse.AnalyseRecommendation;
import org.bonitasoft.serverconfiguration.analyse.Analyse.LEVELRECOMMENDATION;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties.KeyPropertiesReader;

public class AnalysePassword extends Analyse {

    @Override
    public String getTitle() {
        return "Password";
    }

    @Override
    public String getName() {
        return "Password";
    
}

    @Override
    public void analyse(CollectResult collectResult,BonitaAccessor apiAccessor) {
        // analyse the password
        ClassCollect classCollectSetup = collectResult.mapClassCollect.get(TYPECOLLECT.SETUP.toString());
        setInfo("bonita-platform-community-custom.properties:platformAdminPassword", "****");
        KeyPropertiesReader keyPropertiesReader = classCollectSetup.getKeyPropertiesInCollector(TYPECOLLECTOR.platform_engine, "bonita-platform-community-custom.properties");
        if (keyPropertiesReader == null)
            return;
        String passwordPlatform = keyPropertiesReader.getStringValue("platformAdminPassword", null);
        if (passwordPlatform==null || "platform".equals(passwordPlatform))
        {
            AnalyseRecommendation recommendation = new AnalyseRecommendation();
            recommendation.name = "password/Platform password";
            recommendation.value = "platformAdminPassword=<default password>";
            recommendation.level = LEVELRECOMMENDATION.DANGER;
            recommendation.explanation = "You must change the default password, to a more robust one";
            addRecommendations(recommendation);
        } else if (passwordPlatform.length() < 10) {
            AnalyseRecommendation recommendation = new AnalyseRecommendation();
            recommendation.name = "password/Platform password";
            recommendation.value = "platformAdminPassword=";
            recommendation.level = LEVELRECOMMENDATION.WARNING;
            recommendation.explanation = "The current password is not very robust. Set one with minimum 10 characters";
            addRecommendations(recommendation);
        }
        
        // check each tenant now
        Map<Long, List<KeyPropertiesReader>> listTenants =  classCollectSetup.getListTenantsReader();
        for (Long tenantId : listTenants.keySet()) {
            setInfo("Tenant["+tenantId+"] bonita-tenant-community-custom.properties:userPassword", "****");
            KeyPropertiesReader keyPropertiesTenant = classCollectSetup.getKeyPropertiesInTenantByFileName(tenantId, "bonita-tenant-community-custom.properties");
            if (keyPropertiesTenant == null)
                return;
            String passwordInstall = keyPropertiesTenant.getStringValue("userPassword", null);
            if (passwordInstall==null || "install".equals(passwordInstall))
            {
                AnalyseRecommendation recommendation = new AnalyseRecommendation();
                recommendation.name = "password/Tenant ["+tenantId+"] password";
                recommendation.value = "userPassword=<default password>";
                recommendation.level = LEVELRECOMMENDATION.DANGER;
                recommendation.explanation = "You must change the default password, to a more robust one";
                addRecommendations(recommendation);
            } else if (passwordInstall.length() < 10) {
                AnalyseRecommendation recommendation = new AnalyseRecommendation();
                recommendation.name = "password/Tenant ["+tenantId+"] password";
                recommendation.value = "userPassword=<default password>";
                recommendation.level = LEVELRECOMMENDATION.WARNING;
                recommendation.explanation = "The current password is not very robust. Set one with minimum 10 characters";
                addRecommendations(recommendation);
            }
        }

    }
}

