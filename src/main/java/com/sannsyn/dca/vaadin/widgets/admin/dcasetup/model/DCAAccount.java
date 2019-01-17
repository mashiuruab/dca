package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model;

import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.util.DCAConfigProperties;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 6/27/16.
 */
public class DCAAccount {

    private transient Map<String, String> popularRecommenderMap = new HashMap<String, String>(){{
        put("ark", "MostPopularItemsWithScore");
        put("felk", "MostPopularBoughtItems");
    }};
    private String uuid;
    private String name;
    private String description;
    private Boolean active;
    private DCASelectedService selectedService;
    private List<DCASelectedService> services;
    private String logoUrl;
    private String mostPopularRecommender;
    private String presenceCheckAggregate;
    private String popularityCountAggregate;
    private String searchResultRecommender;
    private DCABinaryImage logo;

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return StringUtils.stripToEmpty(name);
    }

    public String getDescription() {
        return StringUtils.stripToEmpty(description);
    }

    public Boolean getActive() {
        return active == null ? Boolean.FALSE : active;
    }

    public com.sannsyn.dca.model.config.DCASelectedService getSelectedService() {
        return selectedService;
    }

    public List<DCASelectedService> getServices() {
        return services;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setSelectedService(DCASelectedService selectedService) {
        this.selectedService = selectedService;
    }

    public void setServices(List<DCASelectedService> services) {
        this.services = services;
    }

    public String getLogoUrl() {
        if (StringUtils.isNotEmpty(logoUrl) && !logoUrl.startsWith("http")) {
            logoUrl = String.format("%s/%s", DCAConfigProperties.getAdminServerUrl(), logoUrl);
        }
        return StringUtils.stripToEmpty(logoUrl);
    }

    public void setLogoUrl(String logoUrl) {
        String replaceString = String.format("%s/", DCAConfigProperties.getAdminServerUrl());
        this.logoUrl = StringUtils.stripToEmpty(logoUrl).replace(replaceString, "");
    }

    public String getMostPopularRecommender() {
        if (StringUtils.isEmpty(mostPopularRecommender)) {
            mostPopularRecommender = StringUtils.stripToEmpty(popularRecommenderMap.get(StringUtils.lowerCase(getName())));
        }
        return mostPopularRecommender;
    }

    public void setMostPopularRecommender(String mostPopularRecommender) {
        this.mostPopularRecommender = mostPopularRecommender;
    }

    public void setPopularityCountAggregate(String popularityCountAggregate) {
        this.popularityCountAggregate = popularityCountAggregate;
    }

    public String getPopularityCountAggregate() {
        return StringUtils.stripToEmpty(popularityCountAggregate);
    }

    public void setPresenceCheckAggregate(String presenceCheckAggregate) {
        this.presenceCheckAggregate = presenceCheckAggregate;
    }

    public String getPresenceCheckAggregate() {
        return StringUtils.stripToEmpty(presenceCheckAggregate);
    }

    public void setSearchResultRecommender(String searchResultRecommender) {
        this.searchResultRecommender = searchResultRecommender;
    }

    public String getSearchResultRecommender() {
        return StringUtils.stripToEmpty(searchResultRecommender);
    }

    public DCABinaryImage getLogo() {
        return logo;
    }

    public void setLogo(DCABinaryImage logo) {
        this.logo = logo;
    }
}
