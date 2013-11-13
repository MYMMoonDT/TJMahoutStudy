package org.tongji.mahoutplatform.recommender.similarity;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.common.Weighting;
import org.apache.mahout.cf.taste.model.DataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class ImprovePearsonCorrelationSimilarity extends AbstractSimilarity{
    
	private static final Logger log = LoggerFactory.getLogger(ImprovePearsonCorrelationSimilarity.class);
	
    private int itemEta = 0;
    
    /**
     * @throws IllegalArgumentException if {@link DataModel} does not have preference values
     */
    public ImprovePearsonCorrelationSimilarity(DataModel dataModel) throws TasteException {
      this(dataModel, Weighting.UNWEIGHTED);
    }
    
    public ImprovePearsonCorrelationSimilarity(DataModel dataModel, int itemEta) throws TasteException {
      this(dataModel, Weighting.UNWEIGHTED);
      this.itemEta = itemEta;
    }

    /**
     * @throws IllegalArgumentException if {@link DataModel} does not have preference values
     */
    public ImprovePearsonCorrelationSimilarity(DataModel dataModel, Weighting weighting) throws TasteException {
      super(dataModel, weighting, true);
      Preconditions.checkArgument(dataModel.hasPreferenceValues(), "DataModel doesn't have preference values");
    }
    
    @Override
    double computeResult(int n, double sumXY, double sumX2, double sumY2, double sumXYdiff2) {
      double result = 0;
      if (n == 0) {
        return Double.NaN;
      }
      // Note that sum of X and sum of Y don't appear here since they are assumed to be 0;
      // the data is assumed to be centered.
      double denominator = Math.sqrt(sumX2) * Math.sqrt(sumY2);
      if (denominator == 0.0) {
        // One or both parties has -all- the same ratings;
        // can't really say much similarity under this measure
        return Double.NaN;
      }
      result = sumXY / denominator;
      if(itemEta != 0){
          result *= ((double)Math.min(n, itemEta)) / (double)itemEta;
      }
      return result;
    }
}
