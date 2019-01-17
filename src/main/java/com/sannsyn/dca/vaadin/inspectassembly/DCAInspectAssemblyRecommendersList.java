package com.sannsyn.dca.vaadin.inspectassembly;

import com.sannsyn.dca.model.inspectassembly.DCAInspectAssemblyResult;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.vaadin.server.Sizeable.Unit.PIXELS;

/**
 * This component will show the list of recommenders and their result. It consists of child component for each
 * of the recommenders.
 * <p>
 * Created by jobaer on 1/4/17.
 */
class DCAInspectAssemblyRecommendersList extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAInspectAssemblyRecommendersList.class);
    private HorizontalLayout resultList = new HorizontalLayout();

    DCAInspectAssemblyRecommendersList() {
        Component componentRoot = buildComponentRoot();
        setCompositionRoot(componentRoot);
    }

    private Component buildComponentRoot() {
        Panel panel = new Panel();
        panel.setSizeFull();
        resultList.setSizeFull();
        panel.setContent(resultList);
        return panel;
    }

    void updateResult(List<DCAInspectAssemblyResult> results) {
        logger.debug("Got request for updating result list.");
        UI.getCurrent().access(() -> {
            resultList.removeAllComponents();
            resultList.setWidth(400 * results.size(), PIXELS);
            results.forEach(this::createResultRow);
        });
    }

    private void createResultRow(DCAInspectAssemblyResult res) {
        DCAInspectAssemblyRecommenderItems components = new DCAInspectAssemblyRecommenderItems(res);
        components.setWidth(400, PIXELS);
        resultList.addComponent(components);
    }
}