package com.sannsyn.dca.service.analytics;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.DCAAbstractRestService;
import com.sannsyn.dca.service.DCACommonService;
import com.sannsyn.dca.service.Status;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.function.Function;

import static com.sannsyn.dca.vaadin.widgets.analytics.DCAAnalyticsDataParser.ACCOUNT;
import static com.sannsyn.dca.vaadin.widgets.analytics.DCAAnalyticsDataParser.KEY_UUID;

/**
 * A service class for CRUD operations of analytics object.
 * <p>
 * Created by jobaer on 4/27/17.
 */
public class DCAAnalyticsBackendService extends DCAAbstractRestService<String> {
    private static final Logger logger = LoggerFactory.getLogger(DCAAnalyticsBackendService.class);
    private final DCACommonService adminService = new DCACommonService();

    private static final String URL = DCACommonService.ADMIN_SERVER_URL + "/dca/data/analytics";

    private Function<Response, String> responseStringFunction = response -> {
        int status = response.getStatus();
        if (status == Response.Status.OK.getStatusCode()) {
            return response.readEntity(String.class);
        } else {
            String errorMsg = response.readEntity(String.class);
            throw new RuntimeException(errorMsg);
        }
    };

    public Observable<Pair<Status, String>> saveUpdateAnalytics(JsonObject item) {
        Observable<String> accountNameObservable = getAccountName();
        return accountNameObservable.flatMap(
            accountName -> hasId(item) ? requestUpdate(item, accountName) : requestCreate(item, accountName));
    }

    private Observable<Pair<Status, String>> requestCreate(JsonObject item, String accountName) {
        item.addProperty(ACCOUNT, accountName);
        Gson gson = new Gson();
        String s = gson.toJson(item);

        logger.debug("this is the data for posting = " + s);
        return doRxPost(s, URL);
    }

    private Observable<Pair<Status, String>> requestUpdate(JsonObject item, String accountName) {
        item.addProperty(ACCOUNT, accountName);
        String uuid = item.get(KEY_UUID).getAsString();
        Gson gson = new Gson();
        String s = gson.toJson(item);

        logger.debug("this is the data for posting = " + s);

        String url = URL + "/" + uuid;
        logger.debug("Requesting update for " + url);

        return doRxPut(s, url);
    }

    private Observable<String> getAccountName() {
        DCAUser loggedInUser = DCAUserPreference.getLoggedInUser();
        Observable<DCASelectedService> selectedService = adminService.getSelectedService(loggedInUser);
        return selectedService.map(service -> service.getAccount().getName());
    }

    public Observable<String> getAllAnalyticsForCurrentAccount() {
        return getAccountName().flatMap(accountName -> {
            String url = URL + "/" + accountName;
            return doRxGet(url, responseStringFunction);
        });
    }

    private boolean hasId(JsonObject item) {
        if (item != null && item.has(KEY_UUID)) {
            String uuidStr = item.get(KEY_UUID).getAsString();
            if (StringUtils.isNotBlank(uuidStr)) {
                return true;
            }
        }
        return false;
    }

    // Format specification
    //date - tab - number of sales - tab - number of sales by recommendations - line break
    public String formatData(List<NumSales> numSales) {
        StringBuilder result = new StringBuilder();

        for (NumSales numSale : numSales) {
            String lineBuilder =
                numSale.getDate() + "\t" + numSale.getSales() + "\t" + numSale.getSalesWithRecommendation() + "\n";
            result.append(lineBuilder);
        }

        return result.toString();
    }
}
