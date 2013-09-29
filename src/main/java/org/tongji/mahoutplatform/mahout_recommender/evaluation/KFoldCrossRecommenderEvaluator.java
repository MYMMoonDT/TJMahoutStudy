package org.tongji.mahoutplatform.mahout_recommender.evaluation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.common.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class KFoldCrossRecommenderEvaluator implements RecommenderEvaluator {
  
  private static final Logger log = LoggerFactory.getLogger(KFoldCrossRecommenderEvaluator.class);
  
  private final Random random;
  private float maxPreference;
  private float minPreference;
  private int kFold;
  private EvalType evalType;
  
  private RunningAverage average;
  
  public enum EvalType {
	  AAD, RMS
  }
  
  public KFoldCrossRecommenderEvaluator() {
    random = RandomUtils.getRandom();
    maxPreference = Float.NaN;
    minPreference = Float.NaN;
    setEvalType(EvalType.AAD);
  }
  
  public final float getMaxPreference() {
    return maxPreference;
  }
  
  public final void setMaxPreference(float maxPreference) {
    this.maxPreference = maxPreference;
  }
  
  public final float getMinPreference() {
    return minPreference;
  }
  
  public final void setMinPreference(float minPreference) {
    this.minPreference = minPreference;
  }
  
  public double evaluate(RecommenderBuilder recommenderBuilder,
          DataModelBuilder dataModelBuilder,
          DataModel dataModel,
          int kFold) throws TasteException{
	  return this.evaluate(recommenderBuilder, dataModelBuilder, dataModel, kFold, 1.0);
  }
  
  public double evaluate(RecommenderBuilder recommenderBuilder,
          DataModelBuilder dataModelBuilder,
          DataModel dataModel,
          int kFold,
          EvalType evalType) throws TasteException{
	  this.setEvalType(evalType);
	  return this.evaluate(recommenderBuilder, dataModelBuilder, dataModel, kFold, 1.0);
  }
  
  public double evaluate(RecommenderBuilder recommenderBuilder,
          DataModelBuilder dataModelBuilder,
          DataModel dataModel,
          int kFold,
          double evaluationPercentage) throws TasteException{
	  
	  this.kFold = kFold;
	  if(kFold != 0){
		  double trainingPercentage = 1 - 1.0 / kFold;
		  return this.evaluate(recommenderBuilder,
				  dataModelBuilder, dataModel, trainingPercentage, evaluationPercentage);
	  }else
		  return 0;
  }
  
  public double evaluate(RecommenderBuilder recommenderBuilder,
                         DataModelBuilder dataModelBuilder,
                         DataModel dataModel,
                         double trainingPercentage,
                         double evaluationPercentage) throws TasteException {
    Preconditions.checkNotNull(recommenderBuilder);
    Preconditions.checkNotNull(dataModel);
    Preconditions.checkArgument(trainingPercentage >= 0.0 && trainingPercentage <= 1.0,
      "Invalid trainingPercentage: " + trainingPercentage);
    Preconditions.checkArgument(evaluationPercentage >= 0.0 && evaluationPercentage <= 1.0,
      "Invalid evaluationPercentage: " + evaluationPercentage);

    log.info("Beginning evaluation using {} of {}", trainingPercentage, dataModel);
    
    double result = 0;
    for(int i = 1; i <= kFold; i++){
	    int numUsers = dataModel.getNumUsers();
	    FastByIDMap<PreferenceArray> trainingPrefs = new FastByIDMap<PreferenceArray>(
	        1 + (int) (evaluationPercentage * numUsers));
	    FastByIDMap<PreferenceArray> testPrefs = new FastByIDMap<PreferenceArray>(
	        1 + (int) (evaluationPercentage * numUsers));
	    
	    LongPrimitiveIterator it = dataModel.getUserIDs();
	    while (it.hasNext()) {
	      long userID = it.nextLong();
	      if (random.nextDouble() < evaluationPercentage) {
	        splitOneUsersPrefs(trainingPercentage, i, trainingPrefs, testPrefs, userID, dataModel);
	      }
	    }
	    
	    DataModel trainingModel = dataModelBuilder == null ? new GenericDataModel(trainingPrefs)
	        : dataModelBuilder.buildDataModel(trainingPrefs);
	    
	    Recommender recommender = recommenderBuilder.buildRecommender(trainingModel);
	    
	    result += getEvaluation(testPrefs, recommender);
	    log.info("Evaluation result: {}", result);
    }
    return result / kFold;
  }
  
  private void splitOneUsersPrefs(double trainingPercentage,
		  						  int currentFold,
                                  FastByIDMap<PreferenceArray> trainingPrefs,
                                  FastByIDMap<PreferenceArray> testPrefs,
                                  long userID,
                                  DataModel dataModel) throws TasteException {
    List<Preference> oneUserTrainingPrefs = null;
    List<Preference> oneUserTestPrefs = null;
    PreferenceArray prefs = dataModel.getPreferencesFromUser(userID);
    int size = prefs.length();
    double testPercentage = 1 - trainingPercentage;
    double testStart = (currentFold - 1) * testPercentage * size;
    double testEnd = currentFold * testPercentage * size;
    for (int i = 0; i < size; i++) {
      Preference newPref = new GenericPreference(userID, prefs.getItemID(i), prefs.getValue(i));
      if(i >= testStart && i < testEnd){
    	  if (oneUserTestPrefs == null) {
              oneUserTestPrefs = Lists.newArrayListWithCapacity(3);
          }
          oneUserTestPrefs.add(newPref);
      }else{
    	  if (oneUserTrainingPrefs == null) {
              oneUserTrainingPrefs = Lists.newArrayListWithCapacity(3);
          }
          oneUserTrainingPrefs.add(newPref);
      }
      /*if (random.nextDouble() < trainingPercentage) {
        if (oneUserTrainingPrefs == null) {
          oneUserTrainingPrefs = Lists.newArrayListWithCapacity(3);
        }
        oneUserTrainingPrefs.add(newPref);
      } else {
        if (oneUserTestPrefs == null) {
          oneUserTestPrefs = Lists.newArrayListWithCapacity(3);
        }
        oneUserTestPrefs.add(newPref);
      }*/
      
    }
    if (oneUserTrainingPrefs != null) {
      trainingPrefs.put(userID, new GenericUserPreferenceArray(oneUserTrainingPrefs));
      if (oneUserTestPrefs != null) {
        testPrefs.put(userID, new GenericUserPreferenceArray(oneUserTestPrefs));
      }
    }
  }

  private float capEstimatedPreference(float estimate) {
    if (estimate > maxPreference) {
      return maxPreference;
    }
    if (estimate < minPreference) {
      return minPreference;
    }
    return estimate;
  }

  private double getEvaluation(FastByIDMap<PreferenceArray> testPrefs, Recommender recommender)
    throws TasteException {
    reset();
    Collection<Callable<Void>> estimateCallables = Lists.newArrayList();
    AtomicInteger noEstimateCounter = new AtomicInteger();
    for (Map.Entry<Long,PreferenceArray> entry : testPrefs.entrySet()) {
      estimateCallables.add(
          new PreferenceEstimateCallable(recommender, entry.getKey(), entry.getValue(), noEstimateCounter));
    }
    log.info("Beginning evaluation of {} users", estimateCallables.size());
    RunningAverageAndStdDev timing = new FullRunningAverageAndStdDev();
    execute(estimateCallables, noEstimateCounter, timing);
    return computeFinalEvaluation();
  }
  
  protected static void execute(Collection<Callable<Void>> callables,
                                AtomicInteger noEstimateCounter,
                                RunningAverageAndStdDev timing) throws TasteException {

    Collection<Callable<Void>> wrappedCallables = wrapWithStatsCallables(callables, noEstimateCounter, timing);
    int numProcessors = Runtime.getRuntime().availableProcessors();
    ExecutorService executor = Executors.newFixedThreadPool(numProcessors);
    log.info("Starting timing of {} tasks in {} threads", wrappedCallables.size(), numProcessors);
    try {
      List<Future<Void>> futures = executor.invokeAll(wrappedCallables);
      // Go look for exceptions here, really
      for (Future<Void> future : futures) {
        future.get();
      }
    } catch (InterruptedException ie) {
      throw new TasteException(ie);
    } catch (ExecutionException ee) {
      throw new TasteException(ee.getCause());
    }
    executor.shutdown();
  }
  
  private static Collection<Callable<Void>> wrapWithStatsCallables(Iterable<Callable<Void>> callables,
                                                                   AtomicInteger noEstimateCounter,
                                                                   RunningAverageAndStdDev timing) {
    Collection<Callable<Void>> wrapped = Lists.newArrayList();
    int count = 0;
    for (Callable<Void> callable : callables) {
      boolean logStats = count++ % 1000 == 0; // log every 1000 or so iterations
      wrapped.add(new StatsCallable(callable, logStats, timing, noEstimateCounter));
    }
    return wrapped;
  }
  
  protected void reset(){
	  average = new FullRunningAverage();
  }
  
  protected void processOneEstimate(float estimatedPreference, Preference realPref){
	 switch(evalType){
	 case AAD:
		 average.addDatum(Math.abs(realPref.getValue() - estimatedPreference));
		 break;
	 case RMS:
		 double diff = realPref.getValue() - estimatedPreference;
		 average.addDatum(diff * diff);
		 break;
	 }
  }
  
  protected double computeFinalEvaluation(){
	  double result = 0;
	  switch(evalType){
		 case AAD:
			 result = average.getAverage();
		 case RMS:
			 result = Math.sqrt(average.getAverage());
		 }
	  return result;
  }

  public int getkFold() {
	return kFold;
}

public void setkFold(int kFold) {
	this.kFold = kFold;
}

public EvalType getEvalType() {
	return evalType;
}

public void setEvalType(EvalType evalType) {
	this.evalType = evalType;
}

public final class PreferenceEstimateCallable implements Callable<Void> {

    private final Recommender recommender;
    private final long testUserID;
    private final PreferenceArray prefs;
    private final AtomicInteger noEstimateCounter;

    public PreferenceEstimateCallable(Recommender recommender,
                                      long testUserID,
                                      PreferenceArray prefs,
                                      AtomicInteger noEstimateCounter) {
      this.recommender = recommender;
      this.testUserID = testUserID;
      this.prefs = prefs;
      this.noEstimateCounter = noEstimateCounter;
    }

    public Void call() throws TasteException {
      for (Preference realPref : prefs) {
        float estimatedPreference = Float.NaN;
        try {
          estimatedPreference = recommender.estimatePreference(testUserID, realPref.getItemID());
        } catch (NoSuchUserException nsue) {
          // It's possible that an item exists in the test data but not training data in which case
          // NSEE will be thrown. Just ignore it and move on.
          log.info("User exists in test data but not training data: {}", testUserID);
        } catch (NoSuchItemException nsie) {
          log.info("Item exists in test data but not training data: {}", realPref.getItemID());
        }
        if (Float.isNaN(estimatedPreference)) {
          noEstimateCounter.incrementAndGet();
        } else {
          estimatedPreference = capEstimatedPreference(estimatedPreference);
          processOneEstimate(estimatedPreference, realPref);
        }
      }
      return null;
    }

  }

}
