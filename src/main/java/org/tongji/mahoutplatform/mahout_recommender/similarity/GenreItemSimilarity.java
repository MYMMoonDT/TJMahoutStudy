package org.tongji.mahoutplatform.mahout_recommender.similarity;

import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

public class GenreItemSimilarity implements ItemSimilarity{

	public void refresh(Collection<Refreshable> alreadyRefreshed) {
	}

	public double itemSimilarity(long itemID1, long itemID2)
			throws TasteException {
		return 0;
	}

	public double[] itemSimilarities(long itemID1, long[] itemID2s)
			throws TasteException {
		return null;
	}

	public long[] allSimilarItemIDs(long itemID) throws TasteException {
		return null;
	}

}
