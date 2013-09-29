package org.tongji.mahoutplatform.mahout_recommender.recommender;

import org.apache.mahout.cf.taste.recommender.IDRescorer;

public class GenreRescorer implements IDRescorer{

	public double rescore(long id, double originalScore) {
		return 0;
	}

	public boolean isFiltered(long id) {
		return false;
	}

}
