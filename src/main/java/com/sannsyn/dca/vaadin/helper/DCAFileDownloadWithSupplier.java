package com.sannsyn.dca.vaadin.helper;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Special download button for the customer targeting widget
 * <p>
 * Created by jobaer on 2/3/17.
 */
public class DCAFileDownloadWithSupplier extends OnDemandFileDownloader {
    private static final Logger logger = LoggerFactory.getLogger(DCAFileDownloadWithSupplier.class);
    private final MutableStreamSource onDemandStreamResource;
    private final Supplier<String> supplier;

    public DCAFileDownloadWithSupplier(MutableStreamSource onDemandStreamResource, Supplier<String> supplier) {
        super(onDemandStreamResource);
        this.onDemandStreamResource = onDemandStreamResource;
        this.supplier = supplier;
    }

    @Override
    public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path) throws IOException {
        logger.debug("File downloader got request ...");
        String content = supplier.get();
        onDemandStreamResource.setContent(content);
        return super.handleConnectorRequest(request, response, path);
    }
}
