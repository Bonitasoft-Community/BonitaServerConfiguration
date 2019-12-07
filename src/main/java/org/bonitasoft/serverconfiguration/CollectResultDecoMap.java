package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.serverconfiguration.Analyse.AnalyseRecommendation;
import org.bonitasoft.serverconfiguration.Analyse.LEVELRECOMMENDATION;
import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;
import org.bonitasoft.serverconfiguration.CollectResult.ClassCollect;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties.KeyPropertiesReader;

public class CollectResultDecoMap {

    private CollectResult collectResult;
    private String title;
    private File localFolderName;

    boolean isLineFeedToHtml = false;

    public CollectResultDecoMap(CollectResult collectResult, String title, File localFolderName) {
        this.collectResult = collectResult;
        this.title = title;
        this.localFolderName = localFolderName;

    }

    public void setLineFeedToHtml(boolean isLineFeedToHtml) {
        this.isLineFeedToHtml = isLineFeedToHtml;
    }

    public File getLocalFolderName() {
        return localFolderName;
    }

    /**
     * get map of the collect operation
     * 
     * @return
     */
    public Map<String, Object> getMap(TYPECOLLECT typeCollect) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("title", title);

        ClassCollect classCollect = collectResult.getClassCollect(typeCollect.toString());
        if (classCollect==null)
            return result;
        if (typeCollect == TYPECOLLECT.ANALYSIS) {
            List<Map<String, Object>> listAnalysisMap = new ArrayList<Map<String, Object>>();
            result.put("indicators", listAnalysisMap);
            if (classCollect.listAnalyses==null)
                return result;
            for (Analyse analysis : classCollect.listAnalyses) {
                Map<String, Object> analysisMap = new HashMap<String, Object>();
                listAnalysisMap.add(analysisMap);
                analysisMap.put("title", analysis.getTitle());
                analysisMap.put("name", analysis.getName());

                List<Map<String, Object>> listInfoMap = new ArrayList<Map<String, Object>>();
                analysisMap.put("infos", analysis.getInfos());
                List<Map<String, Object>> listRecommendataionMap = new ArrayList<Map<String, Object>>();

                analysisMap.put("recommendations", listRecommendataionMap);
                LEVELRECOMMENDATION indicatorLevel = LEVELRECOMMENDATION.SUCCESS;
                for (AnalyseRecommendation recommendation : analysis.getRecommendations()) {
                    Map<String, Object> recommendationMap = new HashMap<String, Object>();
                    listRecommendataionMap.add(recommendationMap);
                    recommendationMap.put("name", recommendation.name);
                    recommendationMap.put("value", recommendation.value);
                    recommendationMap.put("level", recommendation.level.toString());
                    recommendationMap.put("explanation", recommendation.explanation);
                    recommendationMap.put("whattodo", recommendation.whatToDo);
                    if (recommendation.level.indice > indicatorLevel.indice)
                        indicatorLevel = recommendation.level;
                }
                analysisMap.put("level", indicatorLevel.toString());

            }

            return result;
        }

        if (classCollect == null)
            return result;

        Map<String, Object> listPropertiesMap = new HashMap<String, Object>();
        result.put("properties", listPropertiesMap);

        for (String name : classCollect.listPropertiesReader.keySet()) {
            List<Map<String, Object>> listEngineKeys = new ArrayList<Map<String, Object>>();
            listPropertiesMap.put(name, listEngineKeys);
            for (KeyPropertiesReader keyPropertiesReader : classCollect.listPropertiesReader.get(name)) {
                listEngineKeys.add(keyPropertiesReader.getMap(isLineFeedToHtml));
            }
        }
        // tenants now ?
        if (classCollect.listTenantsReader.size() > 0) {
            Map<String, Object> listTenantMap = new HashMap<String, Object>();
            result.put("tenants", listTenantMap);

            for (String tenantid : classCollect.listTenantsReader.keySet()) {
                List<Map<String, Object>> listEngineKeys = new ArrayList<Map<String, Object>>();
                listTenantMap.put(tenantid, listEngineKeys);
                for (KeyPropertiesReader keyPropertiesReader : classCollect.listTenantsReader.get(tenantid)) {
                    listEngineKeys.add(keyPropertiesReader.getMap(isLineFeedToHtml));
                }
            }
        }

        // content
        List<Map<String, Object>> listContents = new ArrayList<Map<String, Object>>();
        result.put("contents", listContents);

        for (String contentName : classCollect.mapContentReader.keySet()) {
            Map<String, Object> content = new HashMap<String, Object>();
            content.put("name", contentName);
            content.put("content", classCollect.mapContentReader.get(contentName));
            listContents.add(content);
        }

        return result;
    }
}
