package org.tongji.mahoutplatform.recommender.evaluation;

import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.model.Preference;

public class KFoldCrossRecommenderAADEvaluator extends AbstractKFoldCrossRecommenderEvaluator{

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
