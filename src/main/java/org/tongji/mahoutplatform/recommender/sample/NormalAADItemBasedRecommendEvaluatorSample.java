package org.tongji.mahoutplatform.recommender.sample;

import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

public class NormalAADItemBasedRecommendEvaluatorSample {
	public static void main(String[] args) throws IOException, TasteException{
		//RandomUtils.useTestSeed();
	
		DataModel model = new FileDataModel(new File("data/ratings.dat"));
		
		RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
		
		RecommenderBuilder builder = new RecommenderBuilder(){
			public Recommender buildRecommender(DataModel model) throws TasteException {
				ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
				return new GenericItemBasedRecommender(model, similarity);
			}
		};
		double score = evaluator.evaluate(builder, null, model, 0.9, 1.0);
		System.out.println("----------random 0.9 training percentage result using AAD----------");
		System.out.println(score);
		System.out.println("-------------------------------------------------------------------");
	}
}
