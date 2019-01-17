package com.sannsyn.dca.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.metadata.DCAMetadataServiceClient;
import com.sannsyn.dca.metadata.DCAMetadataServiceClientImpl;
import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.List;

import static com.sannsyn.dca.service.Status.FAILURE;
import static java.lang.Character.isDigit;

/**
 * Service needed for the customer targeting widget
 * <p>
 * Created by jobaer on 8/10/16.
 */
public class DCACustomerTargetingService extends DCAAbstractRestService<String> {
    private static final Logger logger = LoggerFactory.getLogger(DCACustomerTargetingService.class);
    private final DCAUser loggedInUser;
    private DCAMetadataServiceClient metadataServiceClient = new DCAMetadataServiceClientImpl();
    private DCARecommenderService recommenderService;
    private DCAAggregateService aggregateService = new DCAAggregateService();
    private DCAMailSenderService mailSender = new DCAMailSenderService();
    private DCAConfigService configService = new DCAConfigService();
    private String emptyResultMessage = "No recommended customer found for the selected item(s)";

    public DCACustomerTargetingService(DCAUser loggedInUser) {
        recommenderService = new DCARecommenderService(loggedInUser);
        this.loggedInUser = loggedInUser;
    }

    public Observable<DCAItem> search(String query) {
        return configService.getSelectedService(loggedInUser).flatMap(dcaSelectedService -> {
            String name = dcaSelectedService.getServiceIdentifier();
            logger.debug("Name is " + name);
            return search(query, dcaSelectedService);
        });
    }

    public Observable<Status> sendCustomerList(String recommenderName, int number, List<String> emails, List<String> itemIds, String format) {
        Observable<List<String>> customerIdResponse = getCustomerIdResponse(recommenderName, number, itemIds);
        return customerIdResponse.flatMap(customerIds -> sendCustomerList(emails, customerIds, format));
    }

    private Observable<Status> sendCustomerList(List<String> emails, List<String> customerIds, String format) {
        if (customerIds.isEmpty()) {
            logger.warn("Recommended customer list is empty, will not send any email.");
            return Observable.just(FAILURE);
        } else {
            String subject = getSubject();
            String body = getMessageBody(customerIds, format);
            Status status = mailSender.sendEmail(emails, subject, body);
            return Observable.just(status);
        }
    }

    private String getMessageBody(List<String> ids, String format) {
        return "csv".equals(format) ? convertToCsv(ids) : convertToJson(ids);
    }

    private Observable<List<String>> getCustomerIdResponse(String recommenderName, int numberOfCustomers, List<String> itemIds) {
        String ids = String.join(",", itemIds);
        logger.debug("Got call for finding recommended customers for " + ids);
        return recommenderService.getRecommendedCustomers(recommenderName, numberOfCustomers, itemIds);
    }

    public Observable<String> getCustomerIdResponseString(String recommenderName, int numberOfCustomers, List<String> itemIds, String format) {
        if ("csv".equals(format)) {
            return getCustomerIdResponse(recommenderName, numberOfCustomers, itemIds).map(this::convertToCsv);
        } else {
            return getCustomerIdResponse(recommenderName, numberOfCustomers, itemIds).map(this::convertToJson);
        }
    }

    private String convertToJson(List<String> customerIds) {
        if (customerIds.isEmpty()) {
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("msg", emptyResultMessage);
            return errorObject.toString();
        }

        JsonArray array = new JsonArray();
        customerIds.forEach(array::add);
        JsonObject object = new JsonObject();
        object.add("result", array);
        return object.toString();
    }

    private String convertToCsv(List<String> customerIds) {
        if (customerIds.isEmpty()) {
            return emptyResultMessage;
        }

        return javaslang.collection.List.of(customerIds).mkString(", ");
    }

    private Observable<DCAItem> search(String query, DCASelectedService dcaSelectedService) {
        return searchMetadata(query, dcaSelectedService)
            .onErrorResumeNext(Observable.empty())
            .switchIfEmpty(searchInRecommender(query))
            .flatMap(item -> addPopularity(item, dcaSelectedService));
    }

    private Observable<DCAItem> searchMetadata(String query, DCASelectedService dcaSelectedService) {
        return metadataServiceClient
            .search(query)
            .doOnNext(item -> logger.debug("Metadata response = " + item))
            .map(DCAItem::fromMetadata)
            .distinct()
            .flatMap(item -> takeIfPresent(item, dcaSelectedService));
    }

    private Boolean startsWithNumber(String str) {
        return !StringUtils.isBlank(str) && isDigit(str.charAt(0));
    }

    private Observable<DCAItem> searchInRecommender(String id) {
        if (!startsWithNumber(id)) {
            logger.debug("Doesn't start with number, will not search in recommender.");
            return Observable.empty();
        } else {
            logger.debug("Starts with a number, will search in recommender.");
            Observable<DCASelectedService> targetService = DCAUtils.getTargetService();
            return targetService.flatMap(selectedService -> {
                DCAItem dcaItem = new DCAItem();
                dcaItem.setId(id);
                logger.debug("Requesting for checking existence in the recommender " + id);
                return takeIfPresent(dcaItem, selectedService);
            });
        }
    }

    private Observable<DCAItem> addPopularity(DCAItem item, DCASelectedService selectedService) {
        return getPopularity(item, selectedService).map(val -> {
            item.setPopularity(val);
            return item;
        }).onErrorReturn(ignore -> {
            logger.error("Error occured while adding popularity value. " + ignore.getMessage());
            return item;
        });
    }

    private Observable<Double> getPopularity(DCAItem item, DCASelectedService selectedService) {
        return aggregateService.getPopularity(item.getId(), selectedService);
    }

    // If present in the recommender then it will return Observable.just(result) otherwise Observable.empty
    private Observable<DCAItem> takeIfPresent(DCAItem item, DCASelectedService dcaSelectedService) {
        return recommenderService.isPresentInRecommender(item, dcaSelectedService)
            .flatMap(b -> b ? Observable.just(item) : Observable.empty());
    }

    private String getSubject() {
        return "Target customers list";
    }
}
