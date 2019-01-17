package com.sannsyn.dca.service.analytics;

public class NumSales {
    private Integer sales;
    private Integer salesWithRecommendation;
    private String date;

    public NumSales(Integer sales, Integer salesWithRecommendation) {
        this.sales = sales;
        this.salesWithRecommendation = salesWithRecommendation;
    }

    public NumSales(Integer sales, Integer salesWithRecommendation, String date) {
        this.sales = sales;
        this.salesWithRecommendation = salesWithRecommendation;
        this.date = date;
    }

    public Integer getSales() {
        return sales;
    }

    public Integer getSalesWithRecommendation() {
        return salesWithRecommendation;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
