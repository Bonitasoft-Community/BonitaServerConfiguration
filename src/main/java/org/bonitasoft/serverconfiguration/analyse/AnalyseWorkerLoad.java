package org.bonitasoft.serverconfiguration.analyse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.SortOrder;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowElementInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.Sort;
import org.bonitasoft.serverconfiguration.CollectOperation.BonitaAccessor;
import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;
import org.bonitasoft.serverconfiguration.CollectResult.ClassCollect;
import org.bonitasoft.serverconfiguration.analyse.Analyse.AnalyseRecommendation;
import org.bonitasoft.serverconfiguration.analyse.Analyse.LEVELRECOMMENDATION;
import org.bonitasoft.serverconfiguration.content.ContentTypeProperties.KeyPropertiesReader;
import org.bonitasoft.serverconfiguration.CollectResult;
import org.bonitasoft.serverconfiguration.ConfigAPI;

/**
 * Analyse the number of activity per tenant, and calculated the amount of work. Compare that with the number of workers.
 */
public class AnalyseWorkerLoad extends Analyse {

    Logger logger = Logger.getLogger(AnalyseWorkerLoad.class.getName());

    @Override
    public String getTitle() {
        return "WorkerLoad";
    }

    @Override
    public String getName() {
        return "WorkerLoad";
    }

    @Override
    public void analyse(CollectResult collectResult, BonitaAccessor apiAccessor) {

        // search the number of workers
        ClassCollect classCollectSetup = collectResult.mapClassCollect.get(TYPECOLLECT.SETUP.toString());
        KeyPropertiesReader keyPropertiesTenant = classCollectSetup.getKeyPropertiesInTenantByFileName(apiAccessor.tenantId, "bonita-tenant-community-custom.properties");
        long nbWorkers = keyPropertiesTenant.getLongValue("bonita.tenant.work.maximumPoolSize", 10L);

        setInfo("Tenant[" + apiAccessor.tenantId + "] bonita-tenant-community-custom.properties:bonita.tenant.work.maximumPoolSize", nbWorkers);

        // Based on the Activity
        long baseTime = System.currentTimeMillis();
        baseTime -= 1000 * 60 * 60 * 24; // 24 hours left
        long totalCount = 0;
        int startIndex = 0;
        try {
            do {
                SearchOptionsBuilder sob = new SearchOptionsBuilder(startIndex, 10000);
                sob.greaterThan(ArchivedFlowNodeInstanceSearchDescriptor.REACHED_STATE_DATE, baseTime);
                sob.sort(ArchivedFlowNodeInstanceSearchDescriptor.PARENT_ACTIVITY_INSTANCE_ID, Order.ASC);
                sob.sort(ArchivedFlowNodeInstanceSearchDescriptor.REACHED_STATE_DATE, Order.ASC);

                SearchResult<ArchivedFlowNodeInstance> searchResult = apiAccessor.processAPI.searchArchivedFlowNodeInstances(sob.done());
                totalCount = searchResult.getCount();
                ArchivedFlowNodeInstance previousAct = null;

                for (ArchivedFlowNodeInstance archActivity : searchResult.getResult()) {

                    if (previousAct == null) {
                        previousAct = archActivity;
                        continue;
                    }
                    if (previousAct.getSourceObjectId() != archActivity.getSourceObjectId()) {
                        previousAct = archActivity;

                        continue;
                    }
                    if (previousAct.getType().equals(FlowNodeType.USER_TASK) && previousAct.getState().equalsIgnoreCase("initializing")) {
                        previousAct = archActivity;
                        continue;
                    }
                    // ok, we can register this one
                    long startTime = archActivity.getReachedStateDate().getTime();
                    long startTimeSlot = startTime / 1000 / 60 / 10;
                    RegisterSlot registerSlot = mapRegisterSlot.computeIfAbsent(startTimeSlot, s -> new RegisterSlot(startTimeSlot));
                    registerSlot.nbHits++;
                    registerSlot.cumulateTime += archActivity.getReachedStateDate().getTime() - previousAct.getReachedStateDate().getTime();
                    previousAct = archActivity;

                }
                startIndex += 10000;
            } while (startIndex < totalCount);
        } catch (SearchException e) {

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionDetails = sw.toString();
            logger.severe("Error during search " + e.getMessage() + " at " + exceptionDetails);
        }

        // Ok, now search the pic in case of accumulate load
        RegisterSlot pic = null;
        for (RegisterSlot registerSlot : mapRegisterSlot.values()) {
            if (pic == null)
                pic = registerSlot;
            if (registerSlot.cumulateTime > pic.cumulateTime)
                pic = registerSlot;
        }
        if (pic == null) {
            setInfo("No activity detected in the last 24H", "");
            return;
        }

        // ok, we get the pic
        long loadOneWorkerInTheSlot = 1000 * 60 * 10;
        long maximumAvaibility = nbWorkers * loadOneWorkerInTheSlot;
        long percentageOccupancy = (int) (100.0 * pic.cumulateTime / maximumAvaibility);
        AnalyseRecommendation recommendation = new AnalyseRecommendation();
        recommendation.name = "Nb of workers";

        // calcul the theory to have 80% 
        long capacityTarget = pic.cumulateTime / 80 * 100;
        long recommendedWorkers = (int) (1.0 * capacityTarget / loadOneWorkerInTheSlot) + 1;

        recommendation.value = recommendedWorkers + " workers to handle " + ((int) (capacityTarget / 1000)) + " second CPU in 10 mn";
        if (percentageOccupancy > 95) {
            recommendation.level = LEVELRECOMMENDATION.DANGER;
            recommendation.explanation = "You may consider to increase the number of worker, if the CPU/Memory can handle the new workload ";
        } else if (percentageOccupancy > 85) {
            recommendation.level = LEVELRECOMMENDATION.WARNING;
            recommendation.explanation = "Monitor the number of workers, and prepare to increase it if the CPU/Memory can handle the new workload ";
        } else {
            recommendation.level = LEVELRECOMMENDATION.INFO;
            if (nbWorkers > 10) {
                recommendation.explanation = "You have a lot of workers, you may consider to reduce them";
            } else {
                recommendation.explanation = "Number of workers is correct";
            }
        }

        addRecommendations(recommendation);

    }

    private Map<Long, RegisterSlot> mapRegisterSlot = new HashMap<Long, AnalyseWorkerLoad.RegisterSlot>();

    private class RegisterSlot {

        public long slotNumber;
        public int nbHits = 0;
        public long cumulateTime = 0;

        public RegisterSlot(long slotNumber) {
            this.slotNumber = slotNumber;
        }
    }

}
