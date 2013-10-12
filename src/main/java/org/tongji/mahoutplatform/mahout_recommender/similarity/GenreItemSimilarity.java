package org.tongji.mahoutplatform.mahout_recommender.similarity;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.common.Weighting;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.transforms.PreferenceTransform;
import org.apache.mahout.cf.taste.transforms.SimilarityTransform;
import org.tongji.mahoutplatform.mahout_recommender.data.Genre;

import com.google.common.base.Preconditions;

public class GenreItemSimilarity implements UserSimilarity, ItemSimilarity{

	private PreferenceInferrer inferrer;
	private PreferenceTransform prefTransform;
	private SimilarityTransform similarityTransform;
	private boolean weighted;
	private boolean centerData;
	private int cachedNumItems;
	private int cachedNumUsers;
	
	private DataModel dataModel;
	private RefreshHelper refreshHelper;
	
	
	private final double Lambda = 0.8; 
	private int itemEta = 0;
	private DataModel genreDataModel = null;
	private int GenreNum = Genre.getGenreNum();
	
	public GenreItemSimilarity(DataModel dataModel) throws TasteException {
		this(dataModel, Weighting.UNWEIGHTED);
	}
	
	public GenreItemSimilarity(
			DataModel dataModel,
			DataModel genreDataModel,
			int itemEta) throws TasteException{
		this(dataModel);
		this.genreDataModel = genreDataModel;
		this.itemEta = itemEta;
	}
	
	public GenreItemSimilarity(
			DataModel dataModel,
			DataModel genreDataModel) throws TasteException{
		this(dataModel);
		this.genreDataModel = genreDataModel;
	}
	
	public GenreItemSimilarity(DataModel dataModel, int itemEta) throws TasteException {
		this(dataModel);
		this.itemEta = itemEta;
	}
	
	public GenreItemSimilarity(DataModel dataModel, Weighting weighting) throws TasteException {
		this(dataModel, weighting, true);
		Preconditions.checkArgument(dataModel.hasPreferenceValues(),
				"DataModel doesn't have preference values");
	}
	
	public GenreItemSimilarity(final DataModel dataModel, Weighting weighting, boolean centerData) throws TasteException{
		Preconditions.checkArgument(dataModel != null, "dataModel is null");
	    this.dataModel = dataModel;
	    this.weighted = weighting == Weighting.WEIGHTED;
	    this.centerData = centerData;
	    this.cachedNumItems = dataModel.getNumItems();
	    this.cachedNumUsers = dataModel.getNumUsers();
	    this.refreshHelper = new RefreshHelper(new Callable<Object>() {
	        public Object call() throws TasteException {
	          cachedNumItems = dataModel.getNumItems();
	          cachedNumUsers = dataModel.getNumUsers();
	          return null;
	        }
	      });
	}
	
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		refreshHelper.refresh(alreadyRefreshed);
	}

	public long[] allSimilarItemIDs(long itemID) throws TasteException {
		FastIDSet allSimilarItemIDs = new FastIDSet();
	    LongPrimitiveIterator allItemIDs = dataModel.getItemIDs();
	    while (allItemIDs.hasNext()) {
	      long possiblySimilarItemID = allItemIDs.nextLong();
	      if (!Double.isNaN(itemSimilarity(itemID, possiblySimilarItemID))) {
	        allSimilarItemIDs.add(possiblySimilarItemID);
	      }
	    }
	    return allSimilarItemIDs.toArray();
	}

	public void setPreferenceInferrer(PreferenceInferrer inferrer) {
		Preconditions.checkArgument(inferrer != null, "inferrer is null");
	    refreshHelper.addDependency(inferrer);
	    refreshHelper.removeDependency(this.inferrer);
	    this.inferrer = inferrer;
	}
	
	PreferenceInferrer getPreferenceInferrer() {
		return inferrer;
	}
	
	public DataModel getDataModel(){
		return dataModel;
	}
	
	public PreferenceTransform getPrefTransform() {
	    return prefTransform;
	}
	
	public void setPrefTransform(PreferenceTransform prefTransform) {
	    refreshHelper.addDependency(prefTransform);
	    refreshHelper.removeDependency(this.prefTransform);
	    this.prefTransform = prefTransform;
	}
	
	public SimilarityTransform getSimilarityTransform() {
	    return similarityTransform;
	}
	
	public void setSimilarityTransform(SimilarityTransform similarityTransform) {
	    refreshHelper.addDependency(similarityTransform);
	    refreshHelper.removeDependency(this.similarityTransform);
	    this.similarityTransform = similarityTransform;
	}
	
	boolean isWeighted() {
	    return weighted;
	}
	
	double computeResult(int n, double sumXY, double sumX2, double sumY2, double sumXYdiff2){
		if (n == 0) {
			return Double.NaN;
		}
		// Note that sum of X and sum of Y don't appear here since they are
		// assumed to be 0;
		// the data is assumed to be centered.
		double denominator = Math.sqrt(sumX2) * Math.sqrt(sumY2);
		if (denominator == 0.0) {
			// One or both parties has -all- the same ratings;
			// can't really say much similarity under this measure
			return Double.NaN;
		}
		return sumXY / denominator;
	}
	
	public double userSimilarity(long userID1, long userID2)
			throws TasteException {
		DataModel dataModel = getDataModel();
		PreferenceArray xPrefs = dataModel.getPreferencesFromUser(userID1);
		PreferenceArray yPrefs = dataModel.getPreferencesFromUser(userID2);
		int xLength = xPrefs.length();
		int yLength = yPrefs.length();

		if (xLength == 0 || yLength == 0) {
			return Double.NaN;
		}

		long xIndex = xPrefs.getItemID(0);
		long yIndex = yPrefs.getItemID(0);
		int xPrefIndex = 0;
		int yPrefIndex = 0;

		double sumX = 0.0;
		double sumX2 = 0.0;
		double sumY = 0.0;
		double sumY2 = 0.0;
		double sumXY = 0.0;
		double sumXYdiff2 = 0.0;
		int count = 0;

		boolean hasInferrer = inferrer != null;
		boolean hasPrefTransform = prefTransform != null;

		while (true) {
			int compare = xIndex < yIndex ? -1 : xIndex > yIndex ? 1 : 0;
			if (hasInferrer || compare == 0) {
				double x;
				double y;
				if (xIndex == yIndex) {
					// Both users expressed a preference for the item
					if (hasPrefTransform) {
						x = prefTransform.getTransformedValue(xPrefs.get(xPrefIndex));
						y = prefTransform.getTransformedValue(yPrefs.get(yPrefIndex));
					} else {
						x = xPrefs.getValue(xPrefIndex);
						y = yPrefs.getValue(yPrefIndex);
					}
				} else {
					// Only one user expressed a preference, but infer the other
					// one's preference and tally
					// as if the other user expressed that preference
					if (compare < 0) {
						// X has a value; infer Y's
						x = hasPrefTransform ? prefTransform.getTransformedValue(xPrefs.get(xPrefIndex))
								: xPrefs.getValue(xPrefIndex);
						y = inferrer.inferPreference(userID2, xIndex);
					} else {
						// compare > 0
						// Y has a value; infer X's
						x = inferrer.inferPreference(userID1, yIndex);
						y = hasPrefTransform ? prefTransform.getTransformedValue(yPrefs.get(yPrefIndex))
								: yPrefs.getValue(yPrefIndex);
					}
				}
				sumXY += x * y;
				sumX += x;
				sumX2 += x * x;
				sumY += y;
				sumY2 += y * y;
				double diff = x - y;
				sumXYdiff2 += diff * diff;
				count++;
			}
			if (compare <= 0) {
				if (++xPrefIndex >= xLength) {
					if (hasInferrer) {
						// Must count other Ys; pretend next X is far away
						if (yIndex == Long.MAX_VALUE) {
							// ... but stop if both are done!
							break;
						}
						xIndex = Long.MAX_VALUE;
					} else {
						break;
					}
				} else {
					xIndex = xPrefs.getItemID(xPrefIndex);
				}
			}
			if (compare >= 0) {
				if (++yPrefIndex >= yLength) {
					if (hasInferrer) {
						// Must count other Xs; pretend next Y is far away
						if (xIndex == Long.MAX_VALUE) {
							// ... but stop if both are done!
							break;
						}
						yIndex = Long.MAX_VALUE;
					} else {
						break;
					}
				} else {
					yIndex = yPrefs.getItemID(yPrefIndex);
				}
			}
		}

		// "Center" the data. If my math is correct, this'll do it.
		double result;
		if (centerData) {
			double meanX = sumX / count;
			double meanY = sumY / count;
			// double centeredSumXY = sumXY - meanY * sumX - meanX * sumY + n *
			// meanX * meanY;
			double centeredSumXY = sumXY - meanY * sumX;
			// double centeredSumX2 = sumX2 - 2.0 * meanX * sumX + n * meanX *
			// meanX;
			double centeredSumX2 = sumX2 - meanX * sumX;
			// double centeredSumY2 = sumY2 - 2.0 * meanY * sumY + n * meanY *
			// meanY;
			double centeredSumY2 = sumY2 - meanY * sumY;
			result = computeResult(count, centeredSumXY, centeredSumX2,
					centeredSumY2, sumXYdiff2);
		} else {
			result = computeResult(count, sumXY, sumX2, sumY2, sumXYdiff2);
		}

		if (similarityTransform != null) {
			result = similarityTransform.transformSimilarity(userID1, userID2, result);
		}

		if (!Double.isNaN(result)) {
			result = normalizeWeightResult(result, count, cachedNumItems);
		}
		return result;
	}
	
	public double itemSimilarity(long itemID1, long itemID2)
			throws TasteException {
		DataModel dataModel = getDataModel();
		PreferenceArray xPrefs = dataModel.getPreferencesForItem(itemID1);
		PreferenceArray yPrefs = dataModel.getPreferencesForItem(itemID2);
		int xLength = xPrefs.length();
		int yLength = yPrefs.length();

		if (xLength == 0 || yLength == 0) {
			return Double.NaN;
		}

		long xIndex = xPrefs.getUserID(0);
		long yIndex = yPrefs.getUserID(0);
		int xPrefIndex = 0;
		int yPrefIndex = 0;

		double sumX = 0.0;
		double sumX2 = 0.0;
		double sumY = 0.0;
		double sumY2 = 0.0;
		double sumXY = 0.0;
		double sumXYdiff2 = 0.0;
		int count = 0;

		// No, pref inferrers and transforms don't appy here. I think.

		while (true) {
			int compare = xIndex < yIndex ? -1 : xIndex > yIndex ? 1 : 0;
			if (compare == 0) {
				// Both users expressed a preference for the item
				double x = xPrefs.getValue(xPrefIndex);
				double y = yPrefs.getValue(yPrefIndex);
				sumXY += x * y;
				sumX += x;
				sumX2 += x * x;
				sumY += y;
				sumY2 += y * y;
				double diff = x - y;
				sumXYdiff2 += diff * diff;
				count++;
			}
			if (compare <= 0) {
				if (++xPrefIndex == xLength) {
					break;
				}
				xIndex = xPrefs.getUserID(xPrefIndex);
			}
			if (compare >= 0) {
				if (++yPrefIndex == yLength) {
					break;
				}
				yIndex = yPrefs.getUserID(yPrefIndex);
			}
		}

		double result;
		if (centerData) {
			// See comments above on these computations
			double n = (double) count;
			double meanX = sumX / n;
			double meanY = sumY / n;
			// double centeredSumXY = sumXY - meanY * sumX - meanX * sumY + n *
			// meanX * meanY;
			double centeredSumXY = sumXY - meanY * sumX;
			// double centeredSumX2 = sumX2 - 2.0 * meanX * sumX + n * meanX *
			// meanX;
			double centeredSumX2 = sumX2 - meanX * sumX;
			// double centeredSumY2 = sumY2 - 2.0 * meanY * sumY + n * meanY *
			// meanY;
			double centeredSumY2 = sumY2 - meanY * sumY;
			result = computeResult(count, centeredSumXY, centeredSumX2,
					centeredSumY2, sumXYdiff2);
		} else {
			result = computeResult(count, sumXY, sumX2, sumY2, sumXYdiff2);
		}

		if (similarityTransform != null) {
			result = similarityTransform.transformSimilarity(itemID1, itemID2, result);
		}

		if (!Double.isNaN(result)) {
			result = normalizeWeightResult(result, count, cachedNumUsers);
		}
		
		if(genreDataModel != null){
			int bothInItem1AndItem2 = 0;
			int inItem1OrItem2 = 0;
			PreferenceArray prefs1 = genreDataModel.getPreferencesFromUser(itemID1);
			PreferenceArray prefs2 = genreDataModel.getPreferencesFromUser(itemID2);
			int[] genreArray = new int[GenreNum];
			for(int i = 0; i < prefs1.length(); i++){
				genreArray[(int)prefs1.getItemID(i)]++;
			}
			for(int i = 0; i < prefs2.length(); i++){
				genreArray[(int)prefs2.getItemID(i)]++;
			}
			for(int i = 0; i < genreArray.length; i++){
				if(genreArray[i] == 2){
					bothInItem1AndItem2++;
					inItem1OrItem2++;
				}
				if(genreArray[i] == 1){
					inItem1OrItem2++;
				}
			}
			double genreResult = (double)bothInItem1AndItem2 / (double)inItem1OrItem2;
			result = result * Lambda + (1 - Lambda) * genreResult;
		}
		
		if(itemEta != 0){
			result *= ((double)Math.min(count, itemEta)) / (double)itemEta;
		}
		
		return result;
	}
	
	public double[] itemSimilarities(long itemID1, long[] itemID2s)
			throws TasteException {
		int length = itemID2s.length;
		double[] result = new double[length];
		for (int i = 0; i < length; i++) {
			result[i] = itemSimilarity(itemID1, itemID2s[i]);
		}
		return result;
	}
	
	double normalizeWeightResult(double result, int count, int num) {
		double normalizedResult = result;
		if (weighted) {
			double scaleFactor = 1.0 - (double) count / (double) (num + 1);
			if (normalizedResult < 0.0) {
				normalizedResult = -1.0 + scaleFactor
						* (1.0 + normalizedResult);
			} else {
				normalizedResult = 1.0 - scaleFactor * (1.0 - normalizedResult);
			}
		}
		// Make sure the result is not accidentally a little outside [-1.0, 1.0]
		// due to rounding:
		if (normalizedResult < -1.0) {
			normalizedResult = -1.0;
		} else if (normalizedResult > 1.0) {
			normalizedResult = 1.0;
		}
		return normalizedResult;
	}
	
}