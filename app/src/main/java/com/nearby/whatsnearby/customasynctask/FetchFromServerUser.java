package com.nearby.whatsnearby.customasynctask;

import com.nearby.whatsnearby.utilities.AlertType;

public interface FetchFromServerUser {
    void onPreFetch(AlertType alertType);
    void onFetchCompletion(String string, int id, AlertType alertType);
}