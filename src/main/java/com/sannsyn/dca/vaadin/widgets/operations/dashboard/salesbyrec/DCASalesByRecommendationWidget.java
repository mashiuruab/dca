package com.sannsyn.dca.vaadin.widgets.operations.dashboard.salesbyrec;

import com.sannsyn.dca.service.analytics.DCAKeyFiguresService;
import com.sannsyn.dca.service.analytics.NumSales;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.List;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.wrapWithCssLayout;

/**
 * Sales by recommendation widget.
 * <p>
 * Created by jobaer on 11/2/16.
 */
public class DCASalesByRecommendationWidget extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCASalesByRecommendationWidget.class);
    private final UI ui;
    private DCASalesByRecommenderChart salesByRecommenderChart = new DCASalesByRecommenderChart();

    public DCASalesByRecommendationWidget(UI current) {
        this.ui = current;
        initialize();

        DCAKeyFiguresService keyFiguresService = new DCAKeyFiguresService();
        Observable<List<NumSales>> salesObservable = keyFiguresService.getSalesByRecData();
        salesObservable.subscribe(sales -> {
            ui.access(() -> {
                salesByRecommenderChart.addToDataSeries(sales);
            });
        }, e -> {
            logger.error("Error while fetching sales data for SalesByRecommenderWidget", e);
        });
    }

    private void initialize() {
        this.setStyleName("sales-by-recommendation-wrapper");
        CssLayout rootLayout = new CssLayout();
        rootLayout.addStyleName("sales-by-recommendation");
        rootLayout.setWidth(100, Unit.PERCENTAGE);

        Label widgetTitle = new Label("Sales by Recommendations");
        widgetTitle.addStyleName("title-label");
        CssLayout titleWrapper = wrapWithCssLayout(widgetTitle, "title-wrapper");
        titleWrapper.setWidth(100, Unit.PERCENTAGE);

        rootLayout.addComponent(titleWrapper);

        rootLayout.addComponent(salesByRecommenderChart);

        setCompositionRoot(rootLayout);
    }
}
