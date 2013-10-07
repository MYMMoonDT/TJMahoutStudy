package org.tongji.mahoutplatform.mahout_recommender.sample;

import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.model.DataModel;
import org.tongji.mahoutplatform.mahout_recommender.data.GenreDataModel;

public class GenreDataModelSample {

	public static void main(String[] args) throws IOException {
		DataModel model = new GenreDataModel(new File("data/movies.dat"));
		System.out.println("----------test----------");
	}

}
