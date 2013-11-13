package org.tongji.mahoutplatform.recommender.evaluation;

import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.model.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KFoldCrossRecommenderRMSEvaluator extends AbstractKFoldCrossRecommenderEvaluator{
    
	private static final Logger log = LoggerFactory.getLogger(KFoldCrossRecommenderRMSEvaluator.class);
	
    private RunningAverage average;
    
    @Override
    protected void reset() {
        average = new FullRunningAverage();
    }

    @Override
    protected void processOneEstimate(float estimatedPreference,
            Preference realPref) {
        double diff = realPref.getValue() - estimatedPreference;
        average.addDatum(diff * diff);
    }

    @Override
    protected double computeFinalEvaluation() {
        return Math.sqrt(average.getAverage());
    }
}
