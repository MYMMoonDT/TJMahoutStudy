package org.tongji.mahoutplatform.recommender.sample;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

public class NormalItemBasedRecommendSample {
	
	public static void main(String[] args) throws IOException, TasteException{
	
		DataModel model = new FileDataModel(new File("data/ratings.dat"));
		
		ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
		
		Recommender recommender = new GenericItemBasedRecommender(model, similarity);
		
		List<RecommendedItem> recommendations = recommender.recommend(1, 10);
		
		System.out.println("---------------Normal ItemBased Recommender--------------------");
		for(RecommendedItem recommendation : recommendations){
			System.out.println(recommendation);
		}
		System.out.println("---------------------------------------------------------------");
	}
}
