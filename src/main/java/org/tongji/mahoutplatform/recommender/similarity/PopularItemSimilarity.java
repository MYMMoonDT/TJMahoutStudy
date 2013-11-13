package org.tongji.mahoutplatform.recommender.similarity;

import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tongji.mahoutplatform.recommender.data.PopularFileDataModel;

public class PopularItemSimilarity implements ItemSimilarity{

	private static final Logger log = LoggerFactory.getLogger(PopularItemSimilarity.class);
	
    private DataModel popularDataModel;
    
    public PopularItemSimilarity(DataModel popularFileDataModel){
        FastByIDMap<PreferenceArray> tempPopularForItems = new FastByIDMap<PreferenceArray>();
        PopularFileDataModel dateModel = (PopularFileDataModel)popularFileDataModel;
        FastByIDMap<FastByIDMap<Float>> popularForItems = dateModel.getPopularForItems();
        LongPrimitiveIterator iteratorItem = popularForItems.keySetIterator();
        while(iteratorItem.hasNext()){
            long itemID = iteratorItem.nextLong();
            FastByIDMap<Float> popularForItem = popularForItems.get(itemID);
            PreferenceArray popularForItemPreArray = new GenericUserPreferenceArray(popularForItem.size());
            popularForItemPreArray.setUserID(0, itemID);
            
            LongPrimitiveIterator iteratorPopular = popularForItem.keySetIterator();
            int count = 0;
            while(iteratorPopular.hasNext()){
                long timeStamp = iteratorPopular.nextLong();
                float value = popularForItem.get(timeStamp);
                popularForItemPreArray.setItemID(count, timeStamp);
                popularForItemPreArray.setValue(count, value);
                count++;
            }
            tempPopularForItems.put(itemID, popularForItemPreArray);
        }
        popularDataModel = new GenericDataModel(tempPopularForItems);
    }
    
    public void refresh(Collection<Refreshable> alreadyRefreshed) {
    }

    public double itemSimilarity(long itemID1, long itemID2)
            throws TasteException {
        ImprovePearsonCorrelationSimilarity similarity = new ImprovePearsonCorrelationSimilarity(popularDataModel);
        return similarity.userSimilarity(itemID1, itemID2);
    }

    public double[] itemSimilarities(long itemID1, long[] itemID2s)
            throws TasteException {
        int length = itemID2s.length;
        double[] result = new double[length];
        for (int i = 0; i < length; i++) {
          result[i] = itemSimilarity(itemID1, itemID2s[i]);
        }
        return result;
    }

    public long[] allSimilarItemIDs(long itemID) throws TasteException {
        FastIDSet allSimilarItemIDs = new FastIDSet();
        LongPrimitiveIterator allItemIDs = popularDataModel.getUserIDs();
        while (allItemIDs.hasNext()) {
          long possiblySimilarItemID = allItemIDs.nextLong();
          if (!Double.isNaN(itemSimilarity(itemID, possiblySimilarItemID))) {
            allSimilarItemIDs.add(possiblySimilarItemID);
          }
        }
        return allSimilarItemIDs.toArray();
    }

}
