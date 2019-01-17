package com.sannsyn.dca.model.keyfigures;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Json mapping object for the KeyNumbers WebService response
 * <p>
 * Created by jobaer on 1/19/17.
 */
public class KeyFiguresWsResult {
    @SerializedName("msg")
    @Expose
    private String msg;

    @SerializedName("hits")
    @Expose
    private Integer hits;

    @SerializedName("unique_hits")
    @Expose
    private Integer uniqueHits;

    @SerializedName("purchases")
    @Expose
    private Integer purchases;

    public Integer getHits() {
        return hits;
    }

    public void setHits(Integer hits) {
        this.hits = hits;
    }

    public Integer getUniqueHits() {
        return uniqueHits;
    }

    public void setUniqueHits(Integer uniqueHits) {
        this.uniqueHits = uniqueHits;
    }

    public Integer getPurchases() {
        return purchases;
    }

    public void setPurchases(Integer purchases) {
        this.purchases = purchases;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
