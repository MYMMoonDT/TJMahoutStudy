package org.tongji.mahoutplatform.recommender.similarity;

import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

public class ImprovePearsonCorrelationAndGenreItemSimilarity implements ItemSimilarity{

    private DataModel dataModel;
    private DataModel genreDataModel;
    
    private double Lambda = 0.8;
    
    private ItemSimilarity improvePearsonCorrelationSimilarity;
    private ItemSimilarity genreItemSimilarity;
    
    public ImprovePearsonCorrelationAndGenreItemSimilarity(DataModel dataModel, DataModel genreDataModel) throws TasteException{
        this.dataModel = dataModel;
        this.genreDataModel = genreDataModel;
        this.improvePearsonCorrelationSimilarity = new ImprovePearsonCorrelationSimilarity(this.dataModel);
        this.genreItemSimilarity = new GenreItemSimilarity(this.genreDataModel);
    }
    
    public ImprovePearsonCorrelationAndGenreItemSimilarity(DataModel dataModel, DataModel genreDataModel, int itemEta) throws TasteException{
        this.dataModel = dataModel;
        this.genreDataModel = genreDataModel;
        this.improvePearsonCorrelationSimilarity = new ImprovePearsonCorrelationSimilarity(this.dataModel, itemEta);
        this.genreItemSimilarity = new GenreItemSimilarity(this.genreDataModel);
    }
    
    public ImprovePearsonCorrelationAndGenreItemSimilarity(DataModel dataModel, 
            DataModel genreDataModel,
            double Lambda) throws TasteException{
        this(dataModel, genreDataModel);
        this.Lambda = Lambda;
    }
    
    public ImprovePearsonCorrelationAndGenreItemSimilarity(DataModel dataModel, 
            DataModel genreDataModel,
            double Lambda,
            int itemEta) throws TasteException{
        this(dataModel, genreDataModel, itemEta);
        this.Lambda = Lambda;
    }
    
    public void refresh(Collection<Refreshable> alreadyRefreshed) {
    }

    public double itemSimilarity(long itemID1, long itemID2)
            throws TasteException {
        double result = improvePearsonCorrelationSimilarity.itemSimilarity(itemID1, itemID2);
        double genreResult = genreItemSimilarity.itemSimilarity(itemID1, itemID2);
        result = result * Lambda + (1 - Lambda) * genreResult;
        return result;
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
        LongPrimitiveIterator allItemIDs = dataModel.getItemIDs();
        while (allItemIDs.hasNext()) {
          long possiblySimilarItemID = allItemIDs.nextLong();
          if (!Double.isNaN(itemSimilarity(itemID, possiblySimilarItemID))) {
            allSimilarItemIDs.add(possiblySimilarItemID);
          }
        }
        return allSimilarItemIDs.toArray();
    }
    
}
