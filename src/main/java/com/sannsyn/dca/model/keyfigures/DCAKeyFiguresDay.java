package com.sannsyn.dca.model.keyfigures;

import java.time.LocalDate;

/**
 * Model class representing the latest numbers that we have
 * <p>
 * Created by jobaer on 1/18/17.
 */
public class DCAKeyFiguresDay {
    // For which date the data is valid for
    private LocalDate date;

    private float conversion;
    private float reminderFrequency;
    private int turnover;

    private int uniqueHits;
    private int purchases;

    public DCAKeyFiguresDay() {

    }

    public DCAKeyFiguresDay(float conversion, float coverage, int turnover) {
        this.conversion = conversion;
        this.reminderFrequency = coverage;
        this.turnover = turnover;
    }

    public float getConversion() {
        return conversion;
    }

    public void setConversion(float conversion) {
        this.conversion = conversion;
    }

    public float getReminderFrequency() {
        return reminderFrequency;
    }

    public void setReminderFrequency(float reminderFrequency) {
        this.reminderFrequency = reminderFrequency;
    }

    public int getTurnover() {
        return turnover;
    }

    public void setTurnover(int turnover) {
        this.turnover = turnover;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "DCAKeyFiguresDay{" +
            "date='" + date + '\'' +
            ", conversion=" + conversion +
            ", reminderFrequency=" + reminderFrequency +
            ", turnover=" + turnover +
            '}';
    }

    public int getUniqueHits() {
        return uniqueHits;
    }

    public void setUniqueHits(int uniqueHits) {
        this.uniqueHits = uniqueHits;
    }

    public int getPurchases() {
        return purchases;
    }

    public void setPurchases(int purchases) {
        this.purchases = purchases;
    }
}
