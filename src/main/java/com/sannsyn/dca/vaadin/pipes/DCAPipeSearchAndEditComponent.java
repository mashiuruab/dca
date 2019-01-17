package com.sannsyn.dca.vaadin.pipes;

import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.DCAPipesService;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumbImpl;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * The main UI class that will be used to search for and crete/update pipes (recommender component).
 * <p>
 * Created by jobaer on 5/18/16.
 */
public class DCAPipeSearchAndEditComponent extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipeSearchAndEditComponent.class);

    private final UI ui;
    private final DCAPipeSearchResult searchResult;
    private DCAPipeSearchInput searchInput;
    private DCAPipeEditForm pipeEditForm;
    private Consumer<String> navigationHelper;

    private DCAUser loggedInUser;
    private DCAPipesService recService;

    public DCAPipeSearchAndEditComponent(UI ui, DCAUser loggedInUser, Consumer<String> navHelper) {
        this.ui = ui;
        this.loggedInUser = loggedInUser;
        searchInput = new DCAPipeSearchInput(this::doSearch);
        searchResult = new DCAPipeSearchResult(this::showItemDetails);
        pipeEditForm = new DCAPipeEditForm(loggedInUser, () -> {
            searchInput.refreshSearchResult();
        });
        recService = new DCAPipesService(loggedInUser);
        this.navigationHelper = navHelper;
        init();
    }

    public void init() {
        final CssLayout layout = new CssLayout();
        layout.setWidth(100, PERCENTAGE);

        Component pipeComponentCreator = pipeComponentCreator();
        layout.addComponent(pipeComponentCreator);

        setCompositionRoot(layout);

        recService.getExternalPipeData().subscribe(externalPipeData -> {
            pipeEditForm.setPipeExternalData(externalPipeData);
        }, throwable -> {
            logger.error("Error : ", throwable);
            layout.addComponent(new DCAError(throwable.getMessage()));
        }, () -> {
            doSearch("", "");
        });

    }

    private Component pipeComponentCreator() {
        CssLayout layout = new CssLayout();
        layout.setWidth(100, PERCENTAGE);
        layout.setStyleName("create-recommender-component-container");

        Component searchPipesComponent = createSearchInputAndResult();
        layout.addComponent(searchPipesComponent);
        layout.addComponent(pipeEditForm);

        return layout;
    }

    private void doSearch(String query, String type) {
        searchResult.clearResult();
        Observable<List<DCAPipe>> result = recService.search(query, type, this.loggedInUser);
        result.subscribe(res -> {
            ui.access(() -> searchResult.updateSearchResult(res));
        });
    }

    private void showItemDetails(DCAPipe item) {
        pipeEditForm.showItemDetails(item);
    }

    private Component createSearchInputAndResult() {
        CssLayout layout = new CssLayout();
        layout.setStyleName("create-recommender-component-search-wrapper");
        layout.setWidth(100, PERCENTAGE);

        setupBreadcrumb(layout);

        layout.addComponent(searchInput);
        layout.addComponent(searchResult);

        return layout;
    }

    private void setupBreadcrumb(CssLayout layout) {
        DCABreadCrumb breadCrumb = new DCABreadCrumbImpl();
        breadCrumb.addAction("Controller", navigationHelper);
        breadCrumb.addAction("Pipes", s -> {});
        layout.addComponent(breadCrumb.getView());
    }
}
