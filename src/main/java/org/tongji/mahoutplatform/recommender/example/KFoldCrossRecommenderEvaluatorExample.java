package org.tongji.mahoutplatform.recommender.example;

import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.tongji.mahoutplatform.recommender.data.GenreFileDataModel;
import org.tongji.mahoutplatform.recommender.data.KFoldCrossFileDataModel;
import org.tongji.mahoutplatform.recommender.evaluation.KFoldCrossRecommenderAADEvaluator;
import org.tongji.mahoutplatform.recommender.evaluation.KFoldCrossRecommenderRMSEvaluator;
import org.tongji.mahoutplatform.recommender.recommender.ImproveItemBasedRecommender;
import org.tongji.mahoutplatform.recommender.similarity.CompositeSimilarity;

public class KFoldCrossRecommenderEvaluatorExample {
    public static void main(String[] args) throws IOException, TasteException{
        /*DataModel model = new KFoldCrossFileDataModel(new File("data/ratings.dat"));
    
        KFoldCrossRecommenderAADEvaluator aadEvaluator = new KFoldCrossRecommenderAADEvaluator();
        KFoldCrossRecommenderRMSEvaluator rmsEvaluator = new KFoldCrossRecommenderRMSEvaluator();
        
        RecommenderBuilder builder = new RecommenderBuilder(){
          public Recommender buildRecommender(DataModel model) throws TasteException {
            ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
            return new GenericItemBasedRecommender(model, similarity);
          }
        };
        
        double addScore = aadEvaluator.evaluate(builder, null, model, 10);
        System.out.println(addScore);
        
        double rmsScore = rmsEvaluator.evaluate(builder, null, model, 10);
        System.out.println(rmsScore);*/
        
        /*DataModel model = new KFoldCrossFileDataModel(new File("data/ratings.dat"));
        
        KFoldCrossRecommenderAADEvaluator aadEvaluator = new KFoldCrossRecommenderAADEvaluator();
        KFoldCrossRecommenderRMSEvaluator rmsEvaluator = new KFoldCrossRecommenderRMSEvaluator();
        
        RecommenderBuilder builder = new RecommenderBuilder(){
          public Recommender buildRecommender(DataModel model) throws TasteException {
            ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
            return new ImproveItemBasedRecommender(model, similarity, 0.1);
          }
        };
        
        double addScore = aadEvaluator.evaluate(builder, null, model, 10);
        System.out.println(addScore);
        
        double rmsScore = rmsEvaluator.evaluate(builder, null, model, 10);
        System.out.println(rmsScore);*/
        
        DataModel model = new KFoldCrossFileDataModel(new File("data/ratings.dat"));
        final DataModel genreModel = new GenreFileDataModel(new File("data/movies.dat"));
    
        KFoldCrossRecommenderAADEvaluator aadEvaluator = new KFoldCrossRecommenderAADEvaluator();
        KFoldCrossRecommenderRMSEvaluator rmsEvaluator = new KFoldCrossRecommenderRMSEvaluator();
    
        RecommenderBuilder builder = new RecommenderBuilder(){
          public Recommender buildRecommender(DataModel model) throws TasteException {
            ItemSimilarity similarity = new CompositeSimilarity(model, genreModel, 0.8, 80);
            return new ImproveItemBasedRecommender(model, similarity, 0.1);
          }
        };
        
        double addScore = aadEvaluator.evaluate(builder, null, model, 10);
        System.out.println(addScore);
        
        double rmsScore = rmsEvaluator.evaluate(builder, null, model, 10);
        System.out.println(rmsScore);
    }
}
