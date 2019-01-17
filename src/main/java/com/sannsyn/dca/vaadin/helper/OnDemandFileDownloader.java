package com.sannsyn.dca.vaadin.helper;

/**
 * Most part taken from - https://vaadin.com/wiki/-/wiki/Main/Letting%20the%20user%20download%20a%20file
 */

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This specializes {@link FileDownloader} in a way, such that both the file name and content can be determined
 * on-demand, i.e. when the user has clicked the component.
 */
public class OnDemandFileDownloader extends FileDownloader {

    private static final long serialVersionUID = 1L;
    private final MutableStreamSource onDemandStreamResource;

    public OnDemandFileDownloader(MutableStreamSource onDemandStreamResource) {
        super(new StreamResource(onDemandStreamResource, ""));
        this.onDemandStreamResource = checkNotNull(onDemandStreamResource,
            "The given on-demand stream resource may never be null!");
    }

    @Override
    public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path)
        throws IOException {
        getResource().setFilename(onDemandStreamResource.getFilename());
        return super.handleConnectorRequest(request, response, path);
    }

    private StreamResource getResource() {
        return (StreamResource) this.getResource("dl");
    }

    /**
     * Provide both the StreamSource and the filename in an on-demand way.
     */
    public static class MutableStreamSource implements StreamResource.StreamSource {
        private String content;
        private String fileName;

        public MutableStreamSource() {
            this.content = "{}"; // by default empty content
            this.fileName = "default.json"; // by default filename
        }

        String getFilename() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        void setContent(String content) {
            this.content = content;
        }

        @Override
        public InputStream getStream() {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            return new ByteArrayInputStream(bytes);
        }
    }
}