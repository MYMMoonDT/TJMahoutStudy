package org.tongji.mahoutplatform.recommender.evaluation;

import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.model.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KFoldCrossRecommenderAADEvaluator extends AbstractKFoldCrossRecommenderEvaluator{

	private static final Logger log = LoggerFactory.getLogger(KFoldCrossRecommenderAADEvaluator.class);
	  
    private RunningAverage average;
    
    @Override
    protected void reset() {
        average = new FullRunningAverage();
    }

    @Override
    protected void processOneEstimate(float estimatedPreference,
            Preference realPref) {
        average.addDatum(Math.abs(realPref.getValue() - estimatedPreference));
    }

    @Override
    protected double computeFinalEvaluation() {
        return average.getAverage();
    }

}
