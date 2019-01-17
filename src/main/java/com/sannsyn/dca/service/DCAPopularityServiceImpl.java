package com.sannsyn.dca.service;

import com.sannsyn.dca.model.config.DCAServiceEndpoint;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * This will fetch the popularity data from the webservice.
 * <p/>
 * Created by jobaer on 3/8/16.
 */
public class DCAPopularityServiceImpl extends DCAAbstractRestService<String> implements DCAPopularityService {
    private static final Logger logger = LoggerFactory.getLogger(DCAPopularityServiceImpl.class);

    private DCAConfigService configService = new DCAConfigService();

    private DCAUser loggedInUser;

    public DCAPopularityServiceImpl(DCAUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public Observable<String> getPopularItems() {
        return
            getUrl()
                .flatMap(
                    url ->
                        doRxGet(url, response -> response.readEntity(String.class)));
    }

    public Observable<String> getRecommenderName() {
        return configService.getSelectedService(loggedInUser).map(
            dcaSelectedService -> dcaSelectedService.getAccount().getMostPopularRecommender());
    }

    private Observable<String> getUrl() {
        return configService.getSelectedService(loggedInUser).flatMap(dcaSelectedService -> {
            DCAServiceEndpoint endpoint = dcaSelectedService.getServiceEndpoint();
            DCAAccount account = dcaSelectedService.getAccount();
            String mostPopularRecommender = account.getMostPopularRecommender();
            String url = buildUrl(endpoint.getEndpointAddress(), dcaSelectedService.getServiceIdentifier(), mostPopularRecommender);
            return Observable.just(url);
        });
    }

    private String buildUrl(String endpoint, String serviceIdentifier, String recommenderName) {
        String s = endpoint + "/recapi/1.0/recommend/" + serviceIdentifier + "/" + recommenderName + "/anything/20";
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Popularity Url %s", s));
        }
        return s;
    }
}
