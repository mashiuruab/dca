package com.sannsyn.dca.service;

import rx.Observable;

public interface DCAPopularityService {
    public Observable<String> getPopularItems();
    public Observable<String> getRecommenderName();
}
