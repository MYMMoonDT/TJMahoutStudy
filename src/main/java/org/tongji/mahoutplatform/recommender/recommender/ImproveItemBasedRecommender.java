package org.tongji.mahoutplatform.recommender.recommender;

import java.util.concurrent.Callable;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.impl.recommender.EstimatedPreferenceCapper;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImproveItemBasedRecommender extends GenericItemBasedRecommender{
    private static final Logger log = LoggerFactory.getLogger(ImproveItemBasedRecommender.class);
    
    private final RefreshHelper refreshHelper;
    private EstimatedPreferenceCapper capper;
    
    private double itemSimilarityThreshold = Double.NaN;
    
    public ImproveItemBasedRecommender(DataModel dataModel,
            ItemSimilarity similarity) {
        super(dataModel, similarity);
        this.refreshHelper = new RefreshHelper(new Callable<Void>() {
            public Void call() throws Exception {
                capper = buildCapper();
                return null;
            }
        });
        capper = buildCapper();
    }
    
    public ImproveItemBasedRecommender(DataModel dataModel,
            ItemSimilarity similarity,
            double itemSimilarityThreshold){
        this(dataModel, similarity);
        this.itemSimilarityThreshold = itemSimilarityThreshold;
    }
    
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
          itemAveragePref = 0;
        
        double[] similarities = super.getSimilarity().itemSimilarities(itemID, preferencesFromUser.getIDs());
        for(int i = 0; i < similarities.length; i++){
          double theSimilarity = similarities[i];
          if(!Double.isNaN(theSimilarity)){
            if(!Double.isNaN(itemSimilarityThreshold)){
              if(theSimilarity > itemSimilarityThreshold){
                preference += theSimilarity * (preferencesFromUser.getValue(i) - itemAveragePref);
                totalSimilarity += Math.abs(theSimilarity);
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
    
    private EstimatedPreferenceCapper buildCapper() {
        DataModel dataModel = getDataModel();
        if (Float.isNaN(dataModel.getMinPreference()) && Float.isNaN(dataModel.getMaxPreference())) {
          return null;
        } else {
          return new EstimatedPreferenceCapper(dataModel);
        }
    }
}
