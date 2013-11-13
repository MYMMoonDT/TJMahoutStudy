package org.tongji.mahoutplatform.recommender.similarity;

import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tongji.mahoutplatform.recommender.data.GenreType;

public class GenreItemSimilarity implements ItemSimilarity{

	private static final Logger log = LoggerFactory.getLogger(GenreItemSimilarity.class);
	
    private DataModel genreDataModel;
    private final int GenreNum = GenreType.values().length;
    
    public GenreItemSimilarity(DataModel genreDataModel){
        this.genreDataModel = genreDataModel;
    }
    
    public void refresh(Collection<Refreshable> alreadyRefreshed) {
    }

    public double itemSimilarity(long itemID1, long itemID2)
            throws TasteException {
        int bothInItem1AndItem2 = 0;
        int inItem1OrItem2 = 0;
        PreferenceArray prefs1 = genreDataModel.getPreferencesFromUser(itemID1);
        PreferenceArray prefs2 = genreDataModel.getPreferencesFromUser(itemID2);
        int[] genreArray = new int[GenreNum];
        for(int i = 0; i < prefs1.length(); i++){
          genreArray[(int)prefs1.getItemID(i)]++;
        }
        for(int i = 0; i < prefs2.length(); i++){
          genreArray[(int)prefs2.getItemID(i)]++;
        }
        for(int i = 0; i < genreArray.length; i++){
          if(genreArray[i] == 2){
            bothInItem1AndItem2++;
            inItem1OrItem2++;
          }
          if(genreArray[i] == 1){
            inItem1OrItem2++;
          }
        }
        double genreResult = (double)bothInItem1AndItem2 / (double)inItem1OrItem2;
        return genreResult;
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
        LongPrimitiveIterator allItemIDs = genreDataModel.getUserIDs();
        while (allItemIDs.hasNext()) {
          long possiblySimilarItemID = allItemIDs.nextLong();
          if (!Double.isNaN(itemSimilarity(itemID, possiblySimilarItemID))) {
            allSimilarItemIDs.add(possiblySimilarItemID);
          }
        }
        return allSimilarItemIDs.toArray();
    }

}