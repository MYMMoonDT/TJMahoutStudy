package org.tongji.mahoutplatform.mahout_recommender.sample;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.tongji.mahoutplatform.mahout_recommender.recommender.ImproveItemBasedRecommender;

public class ImproveItemBasedRecommendSample {
	public static void main(String[] args) throws IOException, TasteException{
		
		DataModel model = new FileDataModel(new File("data/ratings.dat"));
		
		ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
		
		Recommender recommender = new ImproveItemBasedRecommender(model, similarity);
		
		List<RecommendedItem> recommendations = recommender.recommend(1, 10);
		
		System.out.println("---------------Improve ItemBased Recommender-------------------");
		for(RecommendedItem recommendation : recommendations){
			System.out.println(recommendation);
		}
		System.out.println("---------------------------------------------------------------");
	}
}
