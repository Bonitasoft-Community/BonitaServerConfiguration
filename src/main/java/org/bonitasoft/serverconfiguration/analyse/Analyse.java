package org.bonitasoft.serverconfiguration.analyse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.serverconfiguration.CollectOperation.BonitaAccessor;
import org.bonitasoft.serverconfiguration.CollectResult;

public abstract class Analyse {

    public abstract String getTitle();

    public abstract String getName();

    public static List<Analyse> instanciateAllAnalyses() {
        List<Analyse> listAnalyses = new ArrayList<>();
        listAnalyses.add(new AnalyseEnvironment());
        listAnalyses.add(new AnalyseMemory());
        listAnalyses.add(new AnalyseDatasource());
        listAnalyses.add(new AnalyseCluster());
        listAnalyses.add(new AnalysePassword());
        listAnalyses.add(new AnalyseWorkerLoad());
        listAnalyses.add(new AnalyseXADatasource());
        return listAnalyses;
    }
    /* ******************************************************************************** */
    /*                                                                                  */
    /* Analyse */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    public abstract void analyse(CollectResult collectResult, BonitaAccessor apiAccessor);

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Information */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    Map<String, Object> infos = new LinkedHashMap<>();

    public Map<String, Object> getInfos() {
        return infos;
    }

    public void setInfo(String name, Object value) {
        infos.put(name, value);
    }

    public void setErrorAnalyse(String information, Exception e) {
        infos.put("Error ", "Exception : " + e.toString());
    }
    /* ******************************************************************************** */
    /*                                                                                  */
    /* Recommendation */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    public static enum LEVELRECOMMENDATION {

        DANGER(3), WARNING(2), INFO(1), SUCCESS(0);

        public int indice;

        LEVELRECOMMENDATION(int indice) {
            this.indice = indice;
        }

    };

    public static class AnalyseRecommendation {

        public String name;
        public Object value;
        public String explanation;
        public LEVELRECOMMENDATION level;
        public String whatToDo;
    }

    List<AnalyseRecommendation> recommendations = new ArrayList<>();

    public List<AnalyseRecommendation> getRecommendations() {
        return recommendations;
    }

    public void addRecommendations(AnalyseRecommendation value) {
        recommendations.add(value);
    }

}
