package net.lezhang.udacity.movie;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhale on 1/2/17.
 */

public enum SortOrder {
    POPULAR(1), TOP_RATED(2);

    private static final String LOG_TAG = SortOrder.class.getSimpleName();

    private final int value;

    SortOrder(int value) {
        this.value = value;
    }

    public int getValue() {
        return  value;
    }

    private static final Map<Integer, SortOrder> intToSortOrderMap = new HashMap<>();
    static {
        for (SortOrder sortOrder : SortOrder.values()) {
            intToSortOrderMap.put(sortOrder.value, sortOrder);
        }
    }

    public static SortOrder fromInt(int i) {
        SortOrder sortOrder = intToSortOrderMap.get(i);
        if (sortOrder == null) {
            Log.w(LOG_TAG, "invalid sort order, using default");
            return SortOrder.POPULAR;
        }
        return sortOrder;
    }

}
