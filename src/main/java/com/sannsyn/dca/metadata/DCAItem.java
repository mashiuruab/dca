package com.sannsyn.dca.metadata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

public class DCAItem {
    private String id;
    private String title;
    private String author;
    private String thumbnail;
    private int count;
    private Double popularity;
    private int size;
    private int boughtTogether;
    private float boost;
    private float score;

    public String getId() {
        return id;
    }

    public DCAItem setId(String id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public DCAItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public DCAItem setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public DCAItem setThumbnailUrl(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    @Override
    public int hashCode() {
        if(id == null) return -1;
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DCAItem && ((DCAItem) obj).getId().equals(this.getId());
    }

    @Override
    public String toString() {
        return this.getId() + " " + this.getTitle() + " " + this.getAuthor() + " " + this.getThumbnail();
    }

    public static DCAItem valueOf(String json) {
        JsonParser parser = new JsonParser();
        JsonObject asJsonObject = parser.parse(json).getAsJsonObject();
        DCAItem b = new DCAItem();
        String rawId = asJsonObject.get("isbn").getAsString();
        b.setId(rawId);
        b.setTitle(asJsonObject.get("title").getAsString());
        b.setThumbnailUrl(asJsonObject.get("thumbnail").getAsString());
        b.setAuthor(asJsonObject.get("author").getAsString());
        return b;
    }

    public static DCAItem fromMetadata(DCAMetadataResponse resp) {
        DCAItem item = new DCAItem();

        resp.getProperty("id").ifPresent(item::setId);
        resp.getProperty("title").ifPresent(item::setTitle);
        resp.getProperty("thumbnail").ifPresent(item::setThumbnailUrl);
        resp.getProperty("author").ifPresent(item::setAuthor);

        return item;
    }

    public int getCount() {
        return count;
    }

    public DCAItem setCount(int count) {
        this.count = count;
        return this;
    }

    public Double getPopularity() {
        return popularity;
    }

    public DCAItem setPopularity(Double popularity) {
        this.popularity = popularity;
        return this;
    }

    public int getSize() {
        return size;
    }

    public DCAItem setSize(int size) {
        this.size = size;
        return this;
    }

    public int getBoughtTogether() {
        return boughtTogether;
    }

    public DCAItem setBoughtTogether(int boughtTogether) {
        this.boughtTogether = boughtTogether;
        return this;
    }

    public float getBoost() {
        return boost;
    }

    public DCAItem setBoost(float boost) {
        this.boost = boost;
        return this;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
