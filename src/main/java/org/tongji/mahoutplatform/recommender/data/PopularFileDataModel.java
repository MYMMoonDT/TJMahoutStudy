package org.tongji.mahoutplatform.recommender.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.AbstractDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PopularFileDataModel extends AbstractDataModel{
	
	private static final Logger log = LoggerFactory.getLogger(PopularFileDataModel.class);

    private FastByIDMap<FastByIDMap<Float>> popularForItems;
    
    private DataModel dataModel;

    public PopularFileDataModel(DataModel dataModel) throws IOException, TasteException {
        this.dataModel = dataModel;
        buildPopularForItem();
    }

    public FastByIDMap<FastByIDMap<Float>> getPopularForItems() {
        return popularForItems;
    }
    
    private void buildPopularForItem() throws TasteException {
        LongPrimitiveIterator itemIDs = dataModel.getItemIDs();
        while (itemIDs.hasNext()) {
            long itemID = itemIDs.nextLong();
            PreferenceArray preferencesForItem = dataModel.getPreferencesForItem(itemID);
            long[] userIDs = preferencesForItem.getIDs();
            ArrayList<ItemTimeStamp> itemTimeStamps = new ArrayList<ItemTimeStamp>();
            for (int i = 0; i < userIDs.length; i++) {
                long timeStamp = dataModel.getPreferenceTime(userIDs[i], itemID);
                float value = dataModel.getPreferenceValue(userIDs[i], itemID);
                ItemTimeStamp itemTimeStamp = new ItemTimeStamp(timeStamp,
                        value);
                itemTimeStamps.add(itemTimeStamp);
            }
            Collections.sort(itemTimeStamps);

            if (itemTimeStamps.size() > 0) {
                Date currentTime = new Date();
                currentTime
                        .setTime(1000 * itemTimeStamps.get(0).getTimeStamp());

                float totalPreferenceValuePerMonth = itemTimeStamps.get(0)
                        .getValue();
                int totalPreferenceNumPerMonth = 1;
                int currentYear = currentTime.getYear();
                int currentMonth = currentTime.getMonth();

                for (int i = 1; i < itemTimeStamps.size(); i++) {
                    long timeStamp = itemTimeStamps.get(i).getTimeStamp();
                    Date date = new Date();
                    date.setTime(timeStamp * 1000);
                    if ((date.getYear() > currentYear)
                            || ((date.getYear() == currentYear)&&(date.getMonth() > currentMonth))) {
                        float averagePreferenceValuePerMonth = totalPreferenceValuePerMonth
                                / totalPreferenceNumPerMonth;
                        long monthID = currentYear * 100 + currentMonth;

                        if (popularForItems == null) {
                            popularForItems = new FastByIDMap<FastByIDMap<Float>>();
                        }

                        FastByIDMap<Float> popularPreMap = popularForItems
                                .get(itemID);
                        if (popularPreMap == null) {
                            popularPreMap = new FastByIDMap<Float>();
                            popularForItems.put(itemID, popularPreMap);
                        }
                        popularPreMap.put(monthID,
                                averagePreferenceValuePerMonth);

                        totalPreferenceValuePerMonth = itemTimeStamps.get(i)
                                .getValue();
                        totalPreferenceNumPerMonth = 1;
                        currentYear = date.getYear();
                        currentMonth = date.getMonth();

                    } else {
                        totalPreferenceValuePerMonth += itemTimeStamps.get(i)
                                .getValue();
                        totalPreferenceNumPerMonth++;
                    }
                }
                float averagePreferenceValuePerMonth = totalPreferenceValuePerMonth
                        / totalPreferenceNumPerMonth;
                long monthID = currentYear * 100 + currentMonth;

                if (popularForItems == null) {
                    popularForItems = new FastByIDMap<FastByIDMap<Float>>();
                }

                FastByIDMap<Float> popularPreMap = popularForItems.get(itemID);
                if (popularPreMap == null) {
                    popularPreMap = new FastByIDMap<Float>();
                    popularForItems.put(itemID, popularPreMap);
                }
                popularPreMap.put(monthID, averagePreferenceValuePerMonth);
            }

        }
    }
    class ItemTimeStamp implements Comparable<ItemTimeStamp> {
        private long timeStamp;
        private float value;

        public ItemTimeStamp(long timeStamp, float value) {
            this.timeStamp = timeStamp;
            this.value = value;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
        }

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }

        public int compareTo(ItemTimeStamp other) {
            return (int) (timeStamp - other.getTimeStamp());
        }
    }
    public LongPrimitiveIterator getUserIDs() throws TasteException {
        return dataModel.getUserIDs();
    }

    public PreferenceArray getPreferencesFromUser(long userID)
            throws TasteException {
        return dataModel.getPreferencesFromUser(userID);
    }

    public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
        return dataModel.getItemIDsFromUser(userID);
    }

    public LongPrimitiveIterator getItemIDs() throws TasteException {
        return dataModel.getItemIDs();
    }

    public PreferenceArray getPreferencesForItem(long itemID)
            throws TasteException {
        return dataModel.getPreferencesForItem(itemID);
    }

    public Float getPreferenceValue(long userID, long itemID)
            throws TasteException {
        return dataModel.getPreferenceValue(userID, itemID);
    }

    public Long getPreferenceTime(long userID, long itemID)
            throws TasteException {
        return dataModel.getPreferenceTime(userID, itemID);
    }

    public int getNumItems() throws TasteException {
        return dataModel.getNumItems();
    }

    public int getNumUsers() throws TasteException {
        return dataModel.getNumUsers();
    }

    public int getNumUsersWithPreferenceFor(long itemID) throws TasteException {
        return dataModel.getNumUsersWithPreferenceFor(itemID);
    }

    public int getNumUsersWithPreferenceFor(long itemID1, long itemID2)
            throws TasteException {
        return dataModel.getNumUsersWithPreferenceFor(itemID1, itemID2);
    }

    public void setPreference(long userID, long itemID, float value)
            throws TasteException {
        dataModel.setPreference(userID, itemID, value);
    }

    public void removePreference(long userID, long itemID)
            throws TasteException {
        dataModel.removePreference(userID, itemID);
    }

    public boolean hasPreferenceValues() {
        return dataModel.hasPreferenceValues();
    }

    public void refresh(Collection<Refreshable> alreadyRefreshed) {
        dataModel.refresh(alreadyRefreshed);
    }
}
