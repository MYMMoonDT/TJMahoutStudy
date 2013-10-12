package org.tongji.mahoutplatform.mahout_recommender.recommender;

import java.util.concurrent.Callable;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.impl.recommender.EstimatedPreferenceCapper;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.CandidateItemsStrategy;
import org.apache.mahout.cf.taste.recommender.MostSimilarItemsCandidateItemsStrategy;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

public class ImproveItemBasedRecommender extends GenericItemBasedRecommender{
	
	//private static final Logger log = LoggerFactory.getLogger(ImproveItemBasedRecommender.class);
	private RefreshHelper refreshHelper;
	private EstimatedPreferenceCapper capper;
	
	//private boolean useSimilarityThreshold = false;
	private double itemSimilarityThreshold = Double.NaN;
	
	public ImproveItemBasedRecommender(
			DataModel dataModel,
			ItemSimilarity similarity,
			double similarityThreshold){
		this(dataModel, similarity);
		this.itemSimilarityThreshold = similarityThreshold;
	}
	
	public ImproveItemBasedRecommender(
			DataModel dataModel,
			ItemSimilarity similarity){
		this(dataModel, 
			similarity,
			AbstractRecommender.getDefaultCandidateItemsStrategy(),
	        getDefaultMostSimilarItemsCandidateItemsStrategy());
	}
	
	public ImproveItemBasedRecommender(
			DataModel dataModel,
			ItemSimilarity similarity,
			CandidateItemsStrategy candidateItemsStrategy,
			MostSimilarItemsCandidateItemsStrategy mostSimilarItemsCandidateItemsStrategy) {
		super(dataModel, similarity, candidateItemsStrategy,
				mostSimilarItemsCandidateItemsStrategy);
		this.refreshHelper = new RefreshHelper(new Callable<Void>() {
	      public Void call() {
	        capper = buildCapper();
	        return null;
	      }
	    });
	    refreshHelper.addDependency(dataModel);
	    refreshHelper.addDependency(similarity);
	    refreshHelper.addDependency(candidateItemsStrategy);
	    refreshHelper.addDependency(mostSimilarItemsCandidateItemsStrategy);
	    capper = buildCapper();
	}
	
	private EstimatedPreferenceCapper buildCapper() {
	    DataModel dataModel = getDataModel();
	    if (Float.isNaN(dataModel.getMinPreference()) && Float.isNaN(dataModel.getMaxPreference())) {
	      return null;
	    } else {
	      return new EstimatedPreferenceCapper(dataModel);
	    }
	}

	/**
	 * Change the formula for calculating the preference of item
	 * Use the following formula
	 * 
	 * P(u,i) = Avg(i) + sum(sim(i,j)*(R(j) - Avg(i))) / sum(sim(i,j))
	 * 
	 */
	
	@Override
	public float doEstimatePreference(long userID, PreferenceArray preferencesFromUser, long itemID) throws TasteException{
		double preference = 0.0;
		double totalSimilarity = 0.0;
		double itemAveragePref = 0.0;
		int count = 0;
		
		PreferenceArray preferencesForItem = super.getDataModel().getPreferencesForItem(itemID);
		int itemPrefLength = preferencesForItem.length();
		for(int i = 0; i < itemPrefLength; i++){
			itemAveragePref += preferencesForItem.getValue(i);
		}
		if(itemPrefLength > 0){
			itemAveragePref = itemAveragePref / itemPrefLength;
		}else
			return Float.NaN;
		
		double[] similarities = super.getSimilarity().itemSimilarities(itemID, preferencesFromUser.getIDs());
		for(int i = 0; i < similarities.length; i++){
			double theSimilarity = similarities[i];
			if(!Double.isNaN(theSimilarity)){
				if(!Double.isNaN(itemSimilarityThreshold)){
					if(theSimilarity > itemSimilarityThreshold){
						preference += theSimilarity * (preferencesFromUser.getValue(i) - itemAveragePref);
						totalSimilarity += theSimilarity;
						count++;
					}else{
						continue;
					}
				}else{
					preference += theSimilarity * (preferencesFromUser.getValue(i) - itemAveragePref);
					totalSimilarity += theSimilarity;
					count++;
				}
			}
		}
		if(count <= 1){
			return Float.NaN;
		}
		float estimate = (float)(preference / totalSimilarity);
		estimate += itemAveragePref;
		if(capper != null){
			estimate = capper.capEstimate(estimate);
		}
		return estimate;
	}
}
