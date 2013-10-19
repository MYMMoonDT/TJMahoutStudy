package org.tongji.mahoutplatform.recommender.sample;

import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.tongji.mahoutplatform.recommender.data.ImproveFileDataModel;
import org.tongji.mahoutplatform.recommender.evaluation.KFoldCrossRecommenderEvaluator;
import org.tongji.mahoutplatform.recommender.evaluation.KFoldCrossRecommenderEvaluator.EvalType;
import org.tongji.mahoutplatform.recommender.recommender.ImproveItemBasedRecommender;

public class KFoldCrossRecommenderEvaluatorSample {
	public static void main(String[] args) throws IOException, TasteException{
		DataModel model = new ImproveFileDataModel(new File("data/ratings.dat"));
		
		KFoldCrossRecommenderEvaluator evaluator = new KFoldCrossRecommenderEvaluator();
		
		RecommenderBuilder builder = new RecommenderBuilder(){
			public Recommender buildRecommender(DataModel model) throws TasteException {
				ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
				return new GenericItemBasedRecommender(model, similarity);
			}
		};
		
		RecommenderBuilder improveBuilder = new RecommenderBuilder(){
			public Recommender buildRecommender(DataModel model) throws TasteException {
				ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
				return new ImproveItemBasedRecommender(model, similarity);
			}
		};
		
		RecommenderBuilder improveBuilder2 = new RecommenderBuilder(){
			public Recommender buildRecommender(DataModel model) throws TasteException {
				ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
				return new ImproveItemBasedRecommender(model, similarity, 0.35);
			}
		};
		
		double score = evaluator.evaluate(builder, null, model, 10, EvalType.AAD);
		System.out.println("----------10-fold cross result using AAD using normal itembased recommender----------");
		System.out.println(score);
		System.out.println("--------------------------------------------------");
		
		score = evaluator.evaluate(builder, null, model, 10, EvalType.RMS);
		System.out.println("----------10-fold cross result using RMS using normal itembased recommender----------");
		System.out.println(score);
		System.out.println("--------------------------------------------------");
		
		score = evaluator.evaluate(improveBuilder, null, model, 10, EvalType.AAD);
		System.out.println("----------10-fold cross result using AAD using improve itembased recommender----------");
		System.out.println(score);
		System.out.println("--------------------------------------------------");
		
		score = evaluator.evaluate(improveBuilder, null, model, 10, EvalType.RMS);
		System.out.println("----------10-fold cross result using RMS using improve itembased recommender----------");
		System.out.println(score);
		System.out.println("--------------------------------------------------");
		
		score = evaluator.evaluate(improveBuilder2, null, model, 10, EvalType.AAD);
		System.out.println("----------10-fold cross result using AAD using improve itembased recommender with threshold----------");
		System.out.println(score);
		System.out.println("--------------------------------------------------");
		
		score = evaluator.evaluate(improveBuilder2, null, model, 10, EvalType.RMS);
		System.out.println("----------10-fold cross result using RMS using improve itembased recommender with threshold----------");
		System.out.println(score);
		System.out.println("--------------------------------------------------");
	}
}
