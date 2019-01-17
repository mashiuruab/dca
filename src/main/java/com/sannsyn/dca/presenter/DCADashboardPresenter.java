package com.sannsyn.dca.presenter;

import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.form.create.DCAAggregateCreateResponse;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAAggregateItem;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAGlobalFilter;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfigWrapper;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCATaskObject;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.info.DCAAggregateInfo;
import rx.Observable;

import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created by mashiur on 2/24/16.
 */
public class DCADashboardPresenter extends DCASectionPresenter {
    public Observable<DCAServiceConfigWrapper> getServiceConfig(DCAUser loggedInUser) {
        return configService.getServiceConfig(loggedInUser);
    }

    public Observable<DCAAggregateCreateResponse> createAggregate(DCAAggregateItem dcaAggregateItem,
                                                                  String aggregateName, DCAUser loggedInUser) {
        return configService.createAggregate(dcaAggregateItem, aggregateName, loggedInUser);
    }

    public Observable<String> createPipeLine(DCATaskObject taskObject, String pipeName, DCAUser loggedInUser, Map<String, Object> allPipeLineExternalInfo) {
        return configService.createPipeLine(taskObject, pipeName, loggedInUser, allPipeLineExternalInfo);
    }

    public Observable<DCAAggregateInfo> getServiceConfig(String aggregateName, String type, Integer number) {
        return configService.getServiceConfig(aggregateName, type, number);
    }

    public String updatePipeLine(DCATaskObject taskObject, String pipeName, DCASelectedService selectedService) {
        return configService.updatePipeLine(taskObject, pipeName, selectedService);
    }

    public Observable<Response> updateGlobalFilter(DCAGlobalFilter globalFilter) {
        return configService.updateGlobalFilter(globalFilter);
    }
}
