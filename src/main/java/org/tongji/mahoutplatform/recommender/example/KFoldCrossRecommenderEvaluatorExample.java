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
import org.tongji.mahoutplatform.recommender.data.PopularFileDataModel;
import org.tongji.mahoutplatform.recommender.evaluation.KFoldCrossRecommenderAADEvaluator;
import org.tongji.mahoutplatform.recommender.evaluation.KFoldCrossRecommenderRMSEvaluator;
import org.tongji.mahoutplatform.recommender.recommender.ImproveItemBasedRecommender;
import org.tongji.mahoutplatform.recommender.similarity.ImprovePearsonCorrelationAndGenreItemAndPopularItemSimilarity;

public class KFoldCrossRecommenderEvaluatorExample {
    public static void main(String[] args) throws IOException, TasteException{
        /**
         * 评价推荐引擎的步骤
         * Step1：创建数据模型实例
         * DataModel model = new FileDataModel(new File("data/ratings.dat"));
         * 
         * Step2： 创建评价类实例
         * RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
         * 
         * Step3: 创建推荐引擎构造器实例
         * RecommenderBuilder builder = new RecommenderBuilder(){
         *   public Recommender buildRecommender(DataModel model) throws TasteException {
         *     Step3.1 创建相似度计算实例
         *     ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
         *     
         *     return new GenericItemBasedRecommender(model, similarity);
         *   }
         * };
         * 
         * Step4： 调用评价类evaluate函数，产生评价结果
         * double score = evaluator.evaluate(builder, null, model, 0.9, 1.0);
         * 
         * 
         * 基于《一种优化的Item-based协同过滤推荐算法》论文的改进 
         * 
         * 
         * 
         * 
         * 
         * 
         */
        
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
        
        /*DataModel model = new KFoldCrossFileDataModel(new File("data/ratings.dat"));
        final DataModel genreModel = new GenreFileDataModel(new File("data/movies.dat"));
    
        KFoldCrossRecommenderAADEvaluator aadEvaluator = new KFoldCrossRecommenderAADEvaluator();
        KFoldCrossRecommenderRMSEvaluator rmsEvaluator = new KFoldCrossRecommenderRMSEvaluator();
    
        RecommenderBuilder builder = new RecommenderBuilder(){
          public Recommender buildRecommender(DataModel model) throws TasteException {
            ItemSimilarity similarity = new ImprovePearsonCorrelationAndGenreItemSimilarity(model, genreModel, 0.8, 80);
            return new ImproveItemBasedRecommender(model, similarity, 0.1);
          }
        };
        
        double addScore = aadEvaluator.evaluate(builder, null, model, 10);
        System.out.println(addScore);
        
        double rmsScore = rmsEvaluator.evaluate(builder, null, model, 10);
        System.out.println(rmsScore);*/
        
        DataModel model = new KFoldCrossFileDataModel(new File("data/ratings.dat"));
        final DataModel genreModel = new GenreFileDataModel(new File("data/movies.dat"));
        final DataModel popularModel = new PopularFileDataModel(model);
    
        KFoldCrossRecommenderAADEvaluator aadEvaluator = new KFoldCrossRecommenderAADEvaluator();
        KFoldCrossRecommenderRMSEvaluator rmsEvaluator = new KFoldCrossRecommenderRMSEvaluator();
    
        RecommenderBuilder builder = new RecommenderBuilder(){
          public Recommender buildRecommender(DataModel model) throws TasteException {
            ItemSimilarity similarity = new ImprovePearsonCorrelationAndGenreItemAndPopularItemSimilarity(model, genreModel, popularModel, 80);
            return new ImproveItemBasedRecommender(model, similarity, 0.1);
          }
        };
        
        double addScore = aadEvaluator.evaluate(builder, null, model, 10);
        System.out.println(addScore);
        
        double rmsScore = rmsEvaluator.evaluate(builder, null, model, 10);
        System.out.println(rmsScore);
    }
}
