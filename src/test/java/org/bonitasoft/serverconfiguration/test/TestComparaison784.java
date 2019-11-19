package org.bonitasoft.serverconfiguration.test;

import java.io.File;

import org.bonitasoft.serverconfiguration.ComparaisonResult;
import org.bonitasoft.serverconfiguration.ComparaisonResult.LOGSTRATEGY;
import org.bonitasoft.serverconfiguration.ComparaisonResultDecoLog;
import org.bonitasoft.serverconfiguration.ConfigAPI;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigBundle;
import org.junit.Test;

public class TestComparaison784 {

    // @Test
    public void testPull() {
        File localFolder=new File("D:/pym/Google Drive/consulting/20190429 Verizon Migration/analysis/7.8.4/BonitaSubscription-7.8.4_SIT_110119-2");

        ConfigAPI currentConfig = ConfigAPI.getInstance(localFolder);
        currentConfig.setupPull();
    }

    // @Test
    public void testSIT784() {
        File localFolder=new File("D:/pym/Google Drive/consulting/20190429 Verizon Migration/analysis/7.8.4/BonitaSubscription-7.8.4_SIT_110119-2");
        ConfigAPI currentConfig = ConfigAPI.getInstance(localFolder);

        File referentielFolder = new File("D:/pym/Google Drive/consulting/20190429 Verizon Migration/analysis/7.8.4/0-referentiel");
        BonitaConfigBundle bonitaReferentiel = BonitaConfigBundle.getInstance(referentielFolder);

        ComparaisonResult comparaison = currentConfig.compareWithReferentiel(bonitaReferentiel, getComparaisonParameter(), LOGSTRATEGY.NOLOG);

        ComparaisonResultDecoLog decoLog = new ComparaisonResultDecoLog(comparaison, "BonitaSubscription-7.8.4_SIT_110119-2",localFolder, referentielFolder );
        decoLog.log(LOGSTRATEGY.OUTALL);

    }
    
    // @Test
    public void testUAT2() {
        File localFolder=new File("D:/pym/Google Drive/consulting/20190429 Verizon Migration/analysis/7.8.4/BonitaSubscription-7.8.4_uat2_110119");

        ConfigAPI currentConfig = ConfigAPI.getInstance(localFolder);

        File referentielFolder = new File("D:/pym/Google Drive/consulting/20190429 Verizon Migration/analysis/7.8.4/0-referentiel");
        BonitaConfigBundle bonitaReferentiel = BonitaConfigBundle.getInstance(referentielFolder);

        ComparaisonResult comparaison = currentConfig.compareWithReferentiel(bonitaReferentiel, getComparaisonParameter(), LOGSTRATEGY.NOLOG);

        ComparaisonResultDecoLog decoLog = new ComparaisonResultDecoLog(comparaison, "BonitaSubscription-7.8.4_uat2_110119",localFolder, referentielFolder );
        decoLog.log(LOGSTRATEGY.OUTALL);

    }
    // @Test
    public void test333() {
        File localFolder=new File("D:/pym/Google Drive/consulting/20190429 Verizon Migration/analysis/7.3.3/BonitaBPMSubscription-7.3.3_PROD_110119");
        
        ConfigAPI currentConfig = ConfigAPI.getInstance(localFolder);

        File referentielFolder = new File("D:/pym/Google Drive/consulting/20190429 Verizon Migration/analysis/7.3.3/0-referentiel");
        BonitaConfigBundle bonitaReferentiel = BonitaConfigBundle.getInstance(referentielFolder);

        ComparaisonResult comparaison = currentConfig.compareWithReferentiel(bonitaReferentiel, getComparaisonParameter(), LOGSTRATEGY.NOLOG);

        ComparaisonResultDecoLog decoLog = new ComparaisonResultDecoLog(comparaison, "BonitaSubscription-7.3.3_PROD",localFolder, referentielFolder );
        decoLog.log(LOGSTRATEGY.OUTALL);

    }
    @Test
    public void testWidlflyBBVA() {
        File localFolder=new File("D:/pym/Google Drive/consulting/20191115 BBVA/BBVA-Wildfly-7.7.2-cantstart");
        
        ConfigAPI currentConfig = ConfigAPI.getInstance(localFolder);

        File referentielFolder = new File("D:/pym/Google Drive/consulting/20191115 BBVA/Bonita-wildfly-7.7.2-referentiel");
        BonitaConfigBundle bonitaReferentiel = BonitaConfigBundle.getInstance(referentielFolder);

        ComparaisonResult comparaison = currentConfig.compareWithReferentiel(bonitaReferentiel, getComparaisonParameter(), LOGSTRATEGY.NOLOG);

        ComparaisonResultDecoLog decoLog = new ComparaisonResultDecoLog(comparaison, "BonitaSubscription-7.7.2_Wildfly_BBVA",localFolder, referentielFolder );
        decoLog.log(LOGSTRATEGY.OUTALL);

    }
    
    private ComparaisonParameter getComparaisonParameter() {
        
        ComparaisonParameter comparaisonParameter= new ComparaisonParameter();
        comparaisonParameter.ignoreTemp=true;
        comparaisonParameter.ignoreLog=true;
        return comparaisonParameter;
    }
}
