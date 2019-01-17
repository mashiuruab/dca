package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.form.view;

import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.info.DCAAggregateInfo;
import com.vaadin.ui.CssLayout;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

/**
 * Created by mashiur on 4/29/16.
 */
public class DCAAggregateInfoComponent extends DCAAggregateViewComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAAggregateInfoComponent.class);

    public DCAAggregateInfoComponent() {
        this.setStyleName("aggregate-info-container");
    }

    public void onNext(DCAAggregateInfo dcaAggregateInfo) {
        try {

            String updates = String.format("%s/%s", dcaAggregateInfo.getUpdatesDone().isEmpty() ? "0" : dcaAggregateInfo.getUpdatesDone(),
                    dcaAggregateInfo.getUpdatesSubmitted().isEmpty() ? "0" : dcaAggregateInfo.getUpdatesSubmitted());
            String memoryConsumed = dcaAggregateInfo.getMemoryConsumed().isEmpty() ? "0" : dcaAggregateInfo.getMemoryConsumed();

            CssLayout aggregateInfoUpdates = new CssLayout();
            aggregateInfoUpdates.setStyleName("aggregate-info");
            aggregateInfoUpdates.addComponent(new DCALabel("<span>Aggregate Info</span>", "header dca-widget-title"));
            aggregateInfoUpdates.addComponent(createViewItem("item-row", "Updates: ", updates));

            long consumedMemoryInByte = Long.parseLong(memoryConsumed);
            String memorySize = readableFileSize(consumedMemoryInByte);
            aggregateInfoUpdates.addComponent(createViewItem("item-row", "Memory consumed: ", memorySize));

            addComponentAsLast(aggregateInfoUpdates, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error("Error : ", throwable.getMessage());
        addComponentAsLast(new DCAError("Error Happened While fetching Aggregate Info"), this);
    }
}
