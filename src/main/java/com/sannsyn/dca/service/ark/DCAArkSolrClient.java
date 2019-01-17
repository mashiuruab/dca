package com.sannsyn.dca.service.ark;

import com.google.gson.*;
import com.sannsyn.dca.metadata.DCAMetadataResponse;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.sannsyn.dca.util.DCAConfigProperties.getArkSolrPassword;
import static com.sannsyn.dca.util.DCAConfigProperties.getArkSolrUser;

/**
 * A solr client service to talk to ark solr.
 * <p>
 * Created by jobaer on 6/14/17.
 */
public class DCAArkSolrClient {
    private static final Logger logger = LoggerFactory.getLogger(DCAArkSolrClient.class);

    private String solrServer;
    private String selectQuery;
    private String queryString;
    private String titleField;
    private String authorField;
    private String isbnField;
    private String thumbnailUrl;

    private static Client client;

    static {
        ClientConfig configuration = new ClientConfig();
        configuration.property(ClientProperties.CONNECT_TIMEOUT, 10000);
        configuration.property(ClientProperties.READ_TIMEOUT, 10000);
        client = ClientBuilder.newClient(configuration);
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(getArkSolrUser(), getArkSolrPassword());
        client.register(feature);
    }

    public DCAArkSolrClient(JsonObject solrConfig) {
        validate(solrConfig);
        initialize(solrConfig);
    }

    private void validate(JsonObject solrConfig) {
        List<String> properties = Arrays.asList("solrServer", "selectQuery", "queryString", "thumbnailUrl", "titleField", "authorField", "isbnField");
        properties.forEach(property -> checkProperty(solrConfig, property));
    }

    private void checkProperty(JsonObject solrConfig, String propertyName) {
        if (!solrConfig.has(propertyName)) {
            throwException(propertyName);
        }
    }

    private void throwException(String missingConfig) {
        String message = String.format("Invalid solrConfig. Missing %s config. Unable to create solr client", missingConfig);
        throw new IllegalArgumentException(message);
    }

    private void initialize(JsonObject solrConfig) {
        this.solrServer = solrConfig.get("solrServer").getAsString();
        this.selectQuery = solrConfig.get("selectQuery").getAsString();
        this.queryString = solrConfig.get("queryString").getAsString();
        this.thumbnailUrl = solrConfig.get("thumbnailUrl").getAsString();
        this.titleField = solrConfig.get("titleField").getAsString();
        this.authorField = solrConfig.get("authorField").getAsString();
        this.isbnField = solrConfig.get("isbnField").getAsString();
    }

    private Client getClient() {
        return client;
    }

    private Observable<String> doRxGet(String url, Function<Response, String> transformer) {
        Observable<Response> observable = RxObservable.from(getClient())
            .target(url)
            .request()
            .rx()
            .get();
        return observable.map(transformer::apply);
    }

    private String solrQuery(String searchString) {
        String keyword = searchString;
        if (StringUtils.containsWhitespace(searchString)) {
            try {
                keyword = URLEncoder.encode(searchString, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        logger.info("urlEncodedSearchString: " + keyword);

        String query = queryString.replace("<searchQuery>", keyword);
        logger.info(">> query=" + query);
        return query;
    }

    private Observable<String> search(String searchString) {
        final String solrUrl = solrServer + selectQuery;
        final String solrQuery = solrQuery(searchString);
        String searchUrlString = null;

        try {
            String encoded = URLEncoder.encode(solrQuery, "UTF-8");
            searchUrlString = solrUrl + encoded;
            logger.debug("Final search url: " + searchUrlString);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }

        if (searchUrlString == null) return Observable.empty();
        return doRxGet(searchUrlString, response -> response.readEntity(String.class));
    }

    public Observable<DCAMetadataResponse> searchProducts(String searchString) {
        return search(searchString).flatMap(solrString -> {
            List<DCAMetadataResponse> metadataResponseList = parseResult(solrString);
            return Observable.from(metadataResponseList);
        });
    }

    private List<DCAMetadataResponse> parseResult(String solrResponse) {
        List<DCAMetadataResponse> result = new ArrayList<>();

        try {
            JsonParser parser = new JsonParser();
            JsonObject jsonElement = parser.parse(solrResponse).getAsJsonObject();

            if (jsonElement.has("response")) {
                JsonObject response = jsonElement.get("response").getAsJsonObject();

                if(response.has("numFound")) {
                    int numFound = response.get("numFound").getAsInt();
                    logger.debug("Solr numFound: " + numFound);
                }

                if (response.has("docs")) {
                    JsonArray docs = response.getAsJsonArray("docs");
                    for (JsonElement doc : docs) {
                        DCAMetadataResponse metadata = parseDoc(doc.getAsJsonObject());
                        if (StringUtils.isNotBlank(metadata.getId())) {
                            result.add(metadata);
                        }
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            logger.error(e.getMessage());
        }

        return result;
    }

    private DCAMetadataResponse parseDoc(JsonObject doc) {
        DCAMetadataResponse result = new DCAMetadataResponse();
        if (doc.has(isbnField)) {
            String isbn = doc.get(isbnField).getAsString();
            result.setId(isbn);

            String thumbnail = thumbnailUrl.replace("<id>", isbn);
            result.addProperty("thumbnail", thumbnail);
        }

        if (doc.has(titleField)) {
            String title = doc.get(titleField).getAsString();
            result.addProperty("title", title);
        }

        if (doc.has(authorField)) {
            JsonArray authorArray = doc.get(authorField).getAsJsonArray();
            if (authorArray.size() > 0) {
                String firstAuthor = authorArray.get(0).getAsString();
                result.addProperty("author", firstAuthor);
            }
        }

        return result;
    }
}
