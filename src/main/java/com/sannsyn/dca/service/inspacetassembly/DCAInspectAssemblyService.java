package com.sannsyn.dca.service.inspacetassembly;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sannsyn.dca.model.inspectassembly.DCAInspectAssemblyResult;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.DCAAbstractRestService;
import com.sannsyn.dca.service.DCAConfigService;
import com.sannsyn.dca.service.Status;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfigWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for inspecting assemblies and queries them for result.
 * <p>
 * Created by jobaer on 1/10/17.
 */
public class DCAInspectAssemblyService extends DCAAbstractRestService<String> {
    private static final Logger logger = LoggerFactory.getLogger(DCAInspectAssemblyService.class);
    private static final String URL_PATTERN = "<endpoint>/recapi/1.0/compoundrecommend/<serviceId>/<assemblyName>";

    public Observable<JsonObject> getAssemblyObjects() {
        return getAssembles().flatMap(assembles -> {
            if (assembles.containsKey("tasks") && assembles.get("tasks").isJsonObject()) {
                return Observable.just(assembles.get("tasks").getAsJsonObject());
            } else {
                logger.debug("No tasks object ");
                return Observable.empty();
            }
        });
    }

    public Observable<List<DCAInspectAssemblyResult>> search(String inputValue, String assemblyName) {
        if (StringUtils.isBlank(assemblyName) || StringUtils.isBlank(inputValue)) {
            logger.warn("Assembly name or Id can not be blank. Returning empty result");
            return Observable.empty();
        }

        return getInspectAssemblyResponse(inputValue, assemblyName).flatMap(res -> {
            if (res.getLeft().equals(Status.SUCCESS)) {
                String responseString = res.getValue();
                return Observable.just(parseResult(responseString));
            } else {
                logger.warn("Search result status is failure. Will return empty result.");
                return Observable.empty();
            }
        });
    }

    private Observable<Pair<Status, String>> getInspectAssemblyResponse(String id, String assemblyName) {
        String data = prepareData(id);
        return getUrl(assemblyName).flatMap(url -> doRxPut(data, url));
    }

    private String prepareData(String searchInput) {
        String[] ids = searchInput.split(";");

        JsonArray array = new JsonArray();
        for (String id : ids) {
            array.add(id);
        }
        JsonObject object = new JsonObject();
        object.add("externalIds", array);
        return object.toString();
    }

    private List<DCAInspectAssemblyResult> parseResult(String inputString) {
        DCAInspectAssemblyResult dcaInspectAssemblyResult = new Gson().fromJson(inputString, DCAInspectAssemblyResult.class);
        ArrayList<DCAInspectAssemblyResult> results = new ArrayList<>();
        prepareResult(results, dcaInspectAssemblyResult);
        return results;
    }

    private void prepareResult(final List<DCAInspectAssemblyResult> results, DCAInspectAssemblyResult parent) {
        // Only add the if it contains result
        if (parent.getResult() != null && !parent.getResult().isEmpty()) {
            results.add(parent);
        }

        if (parent.getChildren() == null) return; // sanity check for recursion

        // Add path properties
        for (DCAInspectAssemblyResult child : parent.getChildren()) {
            child.addToPath(parent.getPath(), parent.getName());
            prepareResult(results, child);
        }
    }

    private Observable<String> getUrl(String assemblyName) {
        return DCAUtils.getTargetService().flatMap(dcaSelectedService -> {
            String url = buildUrl(dcaSelectedService.getServiceEndpoint().getEndpointAddress(),
                dcaSelectedService.getServiceIdentifier(), assemblyName);
            return Observable.just(url);
        });
    }

    private String buildUrl(String endpointAddress, String serviceIdentifier, String assemblyName) {
        return URL_PATTERN
            .replace("<endpoint>", endpointAddress)
            .replace("<serviceId>", serviceIdentifier)
            .replace("<assemblyName>", assemblyName);
    }


    private Observable<Map<String, JsonElement>> getAssembles() {
        DCAConfigService configService = new DCAConfigService();
        DCAUser loggedInUser = DCAUserPreference.getLoggedInUser();
        Observable<DCAServiceConfigWrapper> aggregateInfo = configService.getServiceConfig(loggedInUser);
        return aggregateInfo.map(resp -> resp.getService().getEnsembles().getAssembles());
    }
}
