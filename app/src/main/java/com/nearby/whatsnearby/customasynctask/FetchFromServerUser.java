package com.nearby.whatsnearby.customasynctask;

public interface FetchFromServerUser {
    void onPreFetch();

    void onFetchCompletion(String string, int id);
}