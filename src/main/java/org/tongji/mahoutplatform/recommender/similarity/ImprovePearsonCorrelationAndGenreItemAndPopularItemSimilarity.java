package org.tongji.mahoutplatform.recommender.similarity;

import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.tongji.mahoutplatform.recommender.data.PopularFileDataModel;

public class ImprovePearsonCorrelationAndGenreItemAndPopularItemSimilarity implements ItemSimilarity{

    private DataModel dataModel;
    private DataModel genreFileDataModel;
    private DataModel popularFileDataModel;
    
    //private double Lambda = 0.8;
    private double alpha = 0.8;
    private double beta = 0.1;
    private double gamma = 0.1;
    
    private ItemSimilarity improvePearsonCorrelationSimilarity;
    private ItemSimilarity genreItemSimilarity;
    private ItemSimilarity popularItemSimilarity;
    
    public ImprovePearsonCorrelationAndGenreItemAndPopularItemSimilarity(DataModel dataModel, DataModel genreFileDataModel, DataModel popularFileDataModel) throws TasteException{
        this.dataModel = dataModel;
        this.genreFileDataModel = genreFileDataModel;
        this.popularFileDataModel = popularFileDataModel;
        this.improvePearsonCorrelationSimilarity = new ImprovePearsonCorrelationSimilarity(this.dataModel);
        this.genreItemSimilarity = new GenreItemSimilarity(this.genreFileDataModel);
        this.popularItemSimilarity = new PopularItemSimilarity(this.popularFileDataModel);
    }
    
    public ImprovePearsonCorrelationAndGenreItemAndPopularItemSimilarity(DataModel dataModel, DataModel genreFileDataModel, DataModel popularFileDataModel, int itemEta) throws TasteException{
        this.dataModel = dataModel;
        this.genreFileDataModel = genreFileDataModel;
        this.popularFileDataModel = popularFileDataModel;
        this.improvePearsonCorrelationSimilarity = new ImprovePearsonCorrelationSimilarity(this.dataModel, itemEta);
        this.genreItemSimilarity = new GenreItemSimilarity(this.genreFileDataModel);
        this.popularItemSimilarity = new PopularItemSimilarity(this.popularFileDataModel);
    }
    
    public ImprovePearsonCorrelationAndGenreItemAndPopularItemSimilarity(DataModel dataModel, 
            DataModel genreFileDataModel,
            DataModel popularFileDataModel,
            double alpha,
            double beta,
            double gamma) throws TasteException{
        this(dataModel, genreFileDataModel, popularFileDataModel);
        //this.Lambda = Lambda;
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }
    
    public ImprovePearsonCorrelationAndGenreItemAndPopularItemSimilarity(DataModel dataModel, 
            DataModel genreFileDataModel,
            DataModel popularFileDataModel,
            double alpha,
            double beta,
            double gamma,
            int itemEta) throws TasteException{
        this(dataModel, genreFileDataModel, popularFileDataModel, itemEta);
        //this.Lambda = Lambda;
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }
    
    public void refresh(Collection<Refreshable> alreadyRefreshed) {
    }

    public double itemSimilarity(long itemID1, long itemID2)
            throws TasteException {
        double result = improvePearsonCorrelationSimilarity.itemSimilarity(itemID1, itemID2);
        double genreResult = genreItemSimilarity.itemSimilarity(itemID1, itemID2);
        double popularResult = popularItemSimilarity.itemSimilarity(itemID1, itemID2);
        //result = result * Lambda + (1 - Lambda) * genreResult;
        result = result * alpha + genreResult * beta + popularResult * gamma;
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
