package org.tongji.mahoutplatform.mahout_recommender.sample;

import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.common.RandomUtils;
import org.tongji.mahoutplatform.mahout_recommender.data.GenreDataModel;
import org.tongji.mahoutplatform.mahout_recommender.data.ImproveFileDataModel;
import org.tongji.mahoutplatform.mahout_recommender.evaluation.KFoldCrossRecommenderEvaluator;
import org.tongji.mahoutplatform.mahout_recommender.recommender.ImproveItemBasedRecommender;
import org.tongji.mahoutplatform.mahout_recommender.similarity.GenreItemSimilarity;

public class GenreDataModelSample {

	public static void main(String[] args) throws IOException, TasteException {
		//RandomUtils.useTestSeed();
		
		DataModel model = new ImproveFileDataModel(new File("data/ratings.dat"));
		final DataModel genreModel = new GenreDataModel(new File("data/movies.dat"));
		
		KFoldCrossRecommenderEvaluator evaluator = new KFoldCrossRecommenderEvaluator();
		//RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
		
		RecommenderBuilder builder = new RecommenderBuilder(){
			public Recommender buildRecommender(DataModel model) throws TasteException {
				ItemSimilarity similarity = new GenreItemSimilarity(model,genreModel,80);
				return new ImproveItemBasedRecommender(model, similarity, 0.1);
			}
		};
		double score = evaluator.evaluate(builder, null, model, 10, 1.0);
		System.out.println(score);
	}

}
