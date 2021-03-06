package org.bonitasoft.serverconfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;
import org.bonitasoft.serverconfiguration.CollectResult.ClassCollect;
import org.bonitasoft.serverconfiguration.CollectResult.TYPECOLLECTOR;
import org.bonitasoft.serverconfiguration.analyse.Analyse;
import org.bonitasoft.serverconfiguration.analyse.Analyse.AnalyseRecommendation;
import org.bonitasoft.serverconfiguration.analyse.Analyse.LEVELRECOMMENDATION;
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
            List<Map<String, Object>> listAnalysisMap = new ArrayList<>();
            result.put("indicators", listAnalysisMap);
            if (classCollect.listAnalyses==null)
                return result;
            for (Analyse analysis : classCollect.listAnalyses) {
                Map<String, Object> analysisMap = new HashMap<>();
                listAnalysisMap.add(analysisMap);
                analysisMap.put("title", analysis.getTitle());
                analysisMap.put("name", analysis.getName());
                
                analysisMap.put("infos", analysis.getInfos());
                List<Map<String, Object>> listRecommendataionMap = new ArrayList<>();

                analysisMap.put("recommendations", listRecommendataionMap);
                LEVELRECOMMENDATION indicatorLevel = LEVELRECOMMENDATION.SUCCESS;
                for (AnalyseRecommendation recommendation : analysis.getRecommendations()) {
                    Map<String, Object> recommendationMap = new HashMap<>();
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

        Map<String, Object> listPropertiesMap = new HashMap<>();
        result.put("properties", listPropertiesMap);

        for (TYPECOLLECTOR name : classCollect.mapKeyPropertiesReader.keySet()) {
            List<Map<String, Object>> listEngineKeys = new ArrayList<>();
            listPropertiesMap.put(name.toString(), listEngineKeys);
            for (KeyPropertiesReader keyPropertiesReader : classCollect.mapKeyPropertiesReader.get(name)) {
                listEngineKeys.add(keyPropertiesReader.getMap(isLineFeedToHtml));
            }
        }
        // tenants now ?
        if (classCollect.listTenantsReader.size() > 0) {
            Map<String, Object> listTenantMap = new HashMap<>();
            result.put("tenants", listTenantMap);

            for (Long tenantid : classCollect.listTenantsReader.keySet()) {
                List<Map<String, Object>> listEngineKeys = new ArrayList<>();
                listTenantMap.put(String.valueOf(tenantid), listEngineKeys);
                for (KeyPropertiesReader keyPropertiesReader : classCollect.listTenantsReader.get(tenantid)) {
                    listEngineKeys.add(keyPropertiesReader.getMap(isLineFeedToHtml));
                }
            }
        }

        // content
        List<Map<String, Object>> listContents = new ArrayList<>();
        result.put("contents", listContents);

        for (String contentName : classCollect.mapContentText.keySet()) {
            Map<String, Object> content = new HashMap<>();
            content.put("name", contentName);
            content.put("content", classCollect.mapContentText.get(contentName).getContent());
            listContents.add(content);
        }

        return result;
    }
}
