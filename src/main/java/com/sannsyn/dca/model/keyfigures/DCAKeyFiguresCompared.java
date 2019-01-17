package com.sannsyn.dca.model.keyfigures;

/**
 * Key numbers comparing the latest date and the day before that
 * Created by jobaer on 1/18/17.
 */
public class DCAKeyFiguresCompared {
    private DCAKeyFiguresChangeStatus conversionStatus;
    private DCAKeyFiguresChangeStatus coverageStatus;
    private DCAKeyFiguresChangeStatus turnoverStatus;

    public DCAKeyFiguresChangeStatus getConversionStatus() {
        return conversionStatus;
    }

    public void setConversionStatus(DCAKeyFiguresChangeStatus conversionStatus) {
        this.conversionStatus = conversionStatus;
    }

    public DCAKeyFiguresChangeStatus getCoverageStatus() {
        return coverageStatus;
    }

    public void setCoverageStatus(DCAKeyFiguresChangeStatus coverageStatus) {
        this.coverageStatus = coverageStatus;
    }

    public DCAKeyFiguresChangeStatus getTurnoverStatus() {
        return turnoverStatus;
    }

    public void setTurnoverStatus(DCAKeyFiguresChangeStatus turnoverStatus) {
        this.turnoverStatus = turnoverStatus;
    }

    @Override
    public String toString() {
        return "DCAKeyFiguresCompared{" +
            "conversionStatus=" + conversionStatus +
            ", coverageStatus=" + coverageStatus +
            ", turnoverStatus=" + turnoverStatus +
            '}';
    }
}
