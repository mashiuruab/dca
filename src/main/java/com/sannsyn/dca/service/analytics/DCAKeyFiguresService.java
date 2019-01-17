package com.sannsyn.dca.service.analytics;

import com.google.gson.Gson;
import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.model.keyfigures.DCAKeyFiguresChangeStatus;
import com.sannsyn.dca.model.keyfigures.DCAKeyFiguresCompared;
import com.sannsyn.dca.model.keyfigures.DCAKeyFiguresDay;
import com.sannsyn.dca.model.keyfigures.KeyFiguresWsResult;
import com.sannsyn.dca.util.DCAConfigProperties;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;
import rx.observables.MathObservable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sannsyn.dca.model.keyfigures.DCAKeyFiguresChangeStatus.*;

/**
 * Service for fetching and calculating the key numbers
 * <p>
 * Created by jobaer on 1/18/17.
 */
public class DCAKeyFiguresService {
    private static final Logger logger = LoggerFactory.getLogger(DCAKeyFiguresService.class);
    private static final String URL_TEMPLATE =
        "<analytics-service>/results/<serviceid>/<serviceid>/<date>";

    private static Client client;
    private LocalDate latestDay;

    static {
        ClientConfig configuration = new ClientConfig();
        configuration.property(ClientProperties.CONNECT_TIMEOUT, 5000);
        configuration.property(ClientProperties.READ_TIMEOUT, 5000);
        client = ClientBuilder.newClient(configuration);
    }

    public DCAKeyFiguresService() {
        latestDay = findLatestDayWithData();
        logger.debug("Latest date for which we have data is " + latestDay.toString());
    }

    public DCAKeyFiguresService(Boolean skipLatest) {

    }

    /**
     * We will proble last seven days. Among them the latest day that has data will be returned.
     *
     * @return The latest date for which the analytics server has data
     */
    private LocalDate findLatestDayWithData() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Observable<LocalDate> datesWithResult =
            Observable.range(0, 7)
                .map(yesterday::minusDays)
                .flatMap(this::getResultFor)
                .filter(kf -> kf.getUniqueHits() > 0)
                .map(DCAKeyFiguresDay::getDate);

        List<LocalDate> dateList = datesWithResult.toList().toBlocking().first();
        if (dateList.isEmpty()) {
            // If no data is available for last 7 days, just return yesterday
            return yesterday;
        } else {
            dateList.sort(Collections.reverseOrder());
            return dateList.get(0);
        }
    }

    private Client getClient() {
        return client;
    }

    public Observable<DCAKeyFiguresDay> getLatestResult() {
        LocalDate latestDay = getLatestDay();
        return getResultFor(latestDay);
    }

    public Observable<DCAKeyFiguresCompared> getComparision() {
        return getLatestResult().flatMap(
            day -> getPreviousResult().flatMap(
                previous -> Observable.just(compareResults(day, previous))));
    }

    public Observable<DCAKeyFiguresDay> getMaxNumbers() {
        Func1<Float, Boolean> filterNan = fl -> !Float.isNaN(fl);

        Observable<DCAKeyFiguresDay> historicResult = get30DaysResult();
        Observable<Float> allConversion = historicResult.map(DCAKeyFiguresDay::getConversion);
        Observable<Float> filteredConversions = allConversion.filter(filterNan);
        Observable<Float> maxConversion = MathObservable.max(filteredConversions);

        Observable<Float> allCoverage = historicResult.map(DCAKeyFiguresDay::getReminderFrequency);
        Observable<Float> filtered = allCoverage.filter(filterNan);
        Observable<Float> maxCoverage = MathObservable.averageFloat(filtered);

        Observable<Integer> allTurnover = historicResult.map(DCAKeyFiguresDay::getTurnover);
        Observable<Integer> maxTurnover = MathObservable.max(allTurnover);

        return maxConversion.flatMap(
            conversion -> maxCoverage.flatMap(
                coverage -> maxTurnover.flatMap(
                    turnover -> Observable.just(new DCAKeyFiguresDay(conversion, coverage, turnover)))));
    }

    public Observable<List<NumSales>> getSalesByRecData() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Observable<DCAKeyFiguresDay> daysResult = get30DaysResultFrom(yesterday);
        return convertKeyFiguresToNumSales(daysResult);
    }

    public Observable<List<NumSales>> getSalesByRecData(LocalDate fromDate, LocalDate toDate) {
        Observable<DCAKeyFiguresDay> daysResult = getDateRangeResult(fromDate, toDate);
        return convertKeyFiguresToNumSales(daysResult);
    }

    private Observable<List<NumSales>> convertKeyFiguresToNumSales(Observable<DCAKeyFiguresDay> daysResult) {
        return daysResult.map(dayResult -> {
            int purchases = dayResult.getPurchases();
            int uniqueHits = dayResult.getUniqueHits();
            LocalDate date = dayResult.getDate();
            String dateString = makeDateString(date);
            return new NumSales(purchases, uniqueHits, dateString);
        }).toSortedList((numSales, numSales2) -> {
            LocalDate firstDate = getDateFromString(numSales.getDate());
            LocalDate secondDate = getDateFromString(numSales2.getDate());
            if (secondDate != null && firstDate != null) {
                return secondDate.compareTo(firstDate);
            } else {
                return 0;
            }
        });
    }

    private LocalDate getDateFromString(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(dateString, formatter);
    }

    private LocalDate getLatestDay() {
        return latestDay;
    }

    private Observable<DCAKeyFiguresDay> get30DaysResult() {
        LocalDate latestDay = getLatestDay();
        return generate30DatesFrom(latestDay).flatMap(this::getResultFor);
    }

    private Observable<DCAKeyFiguresDay> get30DaysResultFrom(LocalDate fromDate) {
        return generate30DatesFrom(fromDate).flatMap(this::getResultFor);
    }

    private Observable<DCAKeyFiguresDay> getDateRangeResult(LocalDate fromDate, LocalDate toDate) {
        return generateDateRange(fromDate, toDate).flatMap(this::getResultFor);
    }

    private Observable<String> doRxGet(String url) {
        Observable<Response> observable = RxObservable.from(getClient())
            .target(url)
            .request()
            .rx()
            .get();
        return observable.map(response -> response.readEntity(String.class));
    }

    private Observable<LocalDate> generate30DatesFrom(LocalDate date) {
        return Observable.range(0, 29).map(date::minusDays);
    }

    private Observable<LocalDate> generateDateRange(LocalDate fromDate, LocalDate toDate) {
        int count = 0;
        LocalDate currentDate = fromDate;
        for (int i = 0; currentDate.isBefore(toDate); i++, count++) {
            currentDate = currentDate.plusDays(1);
        }

        return Observable.range(0, count).map(toDate::minusDays);
    }

    private Observable<DCAKeyFiguresDay> getPreviousResult() {
        LocalDate previousDay = getLatestDay().minusDays(1);
        return getResultFor(previousDay);
    }

    private Observable<DCAKeyFiguresDay> getResultFor(LocalDate date) {
        String dateStr = makeDateString(date);
        Observable<DCAKeyFiguresDay> keyFiguresDayObservable = buildUrl(dateStr).flatMap(
            url ->
                doRxGet(url).map(res ->
                    parseResult(res, date)));

        return keyFiguresDayObservable.onErrorResumeNext(Observable.empty());
    }

    private Observable<String> buildUrl(String dateStr) {
        Observable<DCASelectedService> targetService = DCAUtils.getTargetService();
        return getEndpoint().flatMap(
            analyticsEndpoint ->
                targetService.flatMap(service -> {
                    String serviceIdentifier = service.getServiceIdentifier();
                    String url = replacePlaceholdersInUrl(dateStr, analyticsEndpoint, serviceIdentifier);
                    logger.debug("Requesting " + url);
                    return Observable.just(url);
                }));
    }

    private String replacePlaceholdersInUrl(String dateStr, String analyticsDefaultUrl, String serviceIdentifier) {
        return URL_TEMPLATE
            .replace("<analytics-service>", analyticsDefaultUrl)
            .replace("<serviceid>", serviceIdentifier)
            .replace("<date>", dateStr);
    }

    /**
     * First look into the selected service, then in the properties file. If neither is present through Observable.error
     *
     * @return A String Observable
     */
    private Observable<String> getEndpoint() {
        Optional<String> analyticsServerUrl = DCAConfigProperties.getAnalyticsServerUrl();
        Observable<DCASelectedService> targetService = DCAUtils.getTargetService();
        return targetService.flatMap(service -> {
            String urlFromConfig = service.getAnalyticsServerUrl();
            if (StringUtils.isNotBlank(urlFromConfig)) {
                return Observable.just(urlFromConfig);
            } else if (analyticsServerUrl.isPresent()) {
                return Observable.just(analyticsServerUrl.get());
            } else {
                return Observable.error(
                    new IllegalStateException("No analytics server url found in config service or in the properties file"));
            }
        });
    }

    private String makeDateString(LocalDate day) {
        if (day == null) return "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return day.format(formatter);
    }

    private DCAKeyFiguresDay parseResult(String res, LocalDate date) {
        DCAKeyFiguresDay result = new DCAKeyFiguresDay();
        result.setDate(date);

        Gson gson = new Gson();
        KeyFiguresWsResult keyFiguresWsResult = gson.fromJson(res, KeyFiguresWsResult.class);
        if ("No results".equals(keyFiguresWsResult.getMsg())) {
            logger.warn("No result found for the day " + date);
            return result;
        }
        return populateData(result, keyFiguresWsResult);
    }

    private DCAKeyFiguresDay populateData(DCAKeyFiguresDay result, KeyFiguresWsResult keyFiguresWsResult) {
        /*
         * Formula for calculating the numbers
         *  Conversion (previously called sales) = unique hits/sales * 100
         *  Reminder frequency = hits / unique_hits
         *  Turnover = unique hits
         */

        Integer uniqueHits = keyFiguresWsResult.getUniqueHits();
        result.setUniqueHits(uniqueHits);

        Integer purchases = keyFiguresWsResult.getPurchases();
        result.setPurchases(purchases);

        Integer hits = keyFiguresWsResult.getHits();

        if (uniqueHits != null) {
            result.setTurnover(uniqueHits);
        }

        // avoid div by zero and NPE
        if ((purchases != null && purchases > 0) && hits != null && uniqueHits != null) {
            float conversion = uniqueHits * 1.0f / purchases * 100;
            float remFreq = hits * 1.0f / uniqueHits * 1.0f;
            result.setConversion(conversion);
            result.setReminderFrequency(remFreq);
        }
        return result;
    }

    private DCAKeyFiguresCompared compareResults(DCAKeyFiguresDay day, DCAKeyFiguresDay previous) {
        DCAKeyFiguresCompared keyFiguresCompared = new DCAKeyFiguresCompared();

        DCAKeyFiguresChangeStatus conversionStatus = compareFloatValues(day.getConversion(), previous.getConversion());
        keyFiguresCompared.setConversionStatus(conversionStatus);

        DCAKeyFiguresChangeStatus converageStatus = compareFloatValues(day.getReminderFrequency(), previous.getReminderFrequency());
        keyFiguresCompared.setCoverageStatus(converageStatus);

        DCAKeyFiguresChangeStatus turnoverStatus = compareIntValues(day.getTurnover(), previous.getTurnover());
        keyFiguresCompared.setTurnoverStatus(turnoverStatus);

        return keyFiguresCompared;
    }

    private DCAKeyFiguresChangeStatus compareFloatValues(float conversion, float previousConversion) {
        if (isEqual(conversion, previousConversion)) {
            return SAME;
        } else if (conversion > previousConversion) {
            return INC;
        } else {
            return DEC;
        }
    }

    private DCAKeyFiguresChangeStatus compareIntValues(int day, int previous) {
        if (day == previous) {
            return SAME;
        } else if (day > previous) {
            return INC;
        } else {
            return DEC;
        }
    }

    private boolean isEqual(float a, float b) {
        float epsilon = 0.0001f;
        return Math.abs(a - b) < epsilon;
    }
}

