package org.bonitasoft.serverconfiguration;


public class AnalyseMemory extends Analyse {

    @Override
    public String getTitle() {
       return "Memory";
    }

    @Override
    public String getName() {
     return "Memory";
    }

    @Override
    public void analyse(CollectResult collectResult) {
        long totalMemoryInMb = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        
        setInfo("Memory (Mb)", totalMemoryInMb);
        
        // recomendation : 8Mb
        AnalyseRecommendation recommendation = new AnalyseRecommendation();
        recommendation.name = "memory";
        recommendation.value = "8000 Mb (8Gb) expected, "+totalMemoryInMb+" Mb detected";
        if (totalMemoryInMb > 8000)
            recommendation.level = LEVELRECOMMENDATION.SUCCESS;
        else if (totalMemoryInMb > 6000)
            recommendation.level = LEVELRECOMMENDATION.WARNING;
        else 
            recommendation.level = LEVELRECOMMENDATION.DANGER;
        
        recommendation.explanation="Bonita recommand 8Gb memory for a server";
        addRecommendations( recommendation );
        
    }

}