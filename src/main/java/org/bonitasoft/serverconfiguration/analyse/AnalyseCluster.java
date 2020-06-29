package org.bonitasoft.serverconfiguration.analyse;

import java.util.List;
import java.util.Map.Entry;

import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;
import org.bonitasoft.serverconfiguration.CollectResult;
import org.bonitasoft.serverconfiguration.CollectResult.ClassCollect;
import org.bonitasoft.serverconfiguration.CollectResult.TYPECOLLECTOR;
import org.bonitasoft.serverconfiguration.analyse.Analyse.AnalyseRecommendation;
import org.bonitasoft.serverconfiguration.analyse.Analyse.LEVELRECOMMENDATION;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties.KeyPropertiesReader;

/* ******************************************************************************** */
/*                                                                                  */
/* Analyse Cluser */
/*                                                                                  */
/*
 * According
 * https://documentation.bonitasoft.com/bonita/7.11/install-a-bonita-bpm-cluster?searchRequest=ehcache
 * if the cluster is ON, the cache must be off
 */
/*                                                                                  */
/* ******************************************************************************** */

public class AnalyseCluster extends Analyse {

    @Override
    public String getTitle() {
        return "Cluster";
    }

    @Override
    public String getName() {
        return "Cluster";
    }

    private final static String KEY_MULTICAST = "bonita.platform.cluster.hazelcast.multicast.enabled";
    private final static String KEY_TCPIP = "bonita.platform.cluster.hazelcast.tcpip.enabled";
    private final static String KEY_AWS = "bonita.platform.cluster.hazelcast.aws.enabled";

    @Override
    public void analyse(CollectResult collectResult) {
        // Edit the file setup/platform_conf/initial/platform_engine/bonita-platform-sp-custom.properties: Uncomment and set the bonita.cluster property to true, as follows: bonita.cluster=true
        boolean cluster;
        ClassCollect classCollectSetup = collectResult.mapClassCollect.get(TYPECOLLECT.SETUP.toString());

        // get the bonitaPlatform information
        KeyPropertiesReader keyPropertiesReader = classCollectSetup.getKeyPropertiesInCollector(TYPECOLLECTOR.platform_engine, "bonita-platform-sp-cluster-custom.properties");
        if (keyPropertiesReader == null)
            return;
        boolean isCluster = keyPropertiesReader.getBooleanValue("bonita.cluster", false);
        setInfo("Cluster", isCluster);

        if (!isCluster) {
            return;
        }
        boolean useSecondLevelCache = keyPropertiesReader.getBooleanValue("bonita.platform.persistence.use_second_level_cache", true);
        if (useSecondLevelCache) {
            // this is an error, this value must set to false
            AnalyseRecommendation recommendation = new AnalyseRecommendation();
            recommendation.name = "cluster/level cache";
            recommendation.value = "bonita.platform.persistence.use_second_level_cache=false expected";
            recommendation.level = LEVELRECOMMENDATION.DANGER;
            recommendation.explanation = "Visit https://documentation.bonitasoft.com/bonita/7.11/install-a-bonita-bpm-cluster";
            addRecommendations(recommendation);

        }
        // only one must be enabled
        // Edit the file setup/platform_conf/initial/platform_engine/bonita-platform-sp-cluster-custom.properties: *
        // set to true one of bonita.platform.cluster.hazelcast.multicast.enabled 
        // or bonita.platform.cluster.hazelcast.tcpip.enabled 
        // or bonita.platform.cluster.hazelcast.aws.enabled
        int countMechanism = 1;
        String mechanism="";
        if (Boolean.TRUE.equals(keyPropertiesReader.getBooleanValue(KEY_MULTICAST, false))) {
            countMechanism++;
            mechanism+="MULTICAST,";
        }
        if (Boolean.TRUE.equals(keyPropertiesReader.getBooleanValue(KEY_TCPIP, false))) {
            countMechanism++;
            mechanism+="TCPIP,";
        }
        if (Boolean.TRUE.equals(keyPropertiesReader.getBooleanValue(KEY_AWS, false))) {
            countMechanism++;
            mechanism+="AWS,";
        }

        if (countMechanism != 1) {
            AnalyseRecommendation recommendation = new AnalyseRecommendation();
            recommendation.name = "cluster/mechanism cache";
            recommendation.value = "Only one mechanism must be enable, " + countMechanism + " detected :"+mechanism;
            recommendation.level = LEVELRECOMMENDATION.DANGER;

            recommendation.explanation = "Visit https://documentation.bonitasoft.com/bonita/7.11/install-a-bonita-bpm-cluster. Only one beetween "
                    + KEY_MULTICAST + ", "
                    + KEY_TCPIP + ", "
                    + KEY_AWS;

            addRecommendations(recommendation);
        }

    }
}
