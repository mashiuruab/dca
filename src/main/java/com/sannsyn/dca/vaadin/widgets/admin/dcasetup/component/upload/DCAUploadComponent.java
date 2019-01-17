package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component.upload;

import com.sannsyn.dca.util.DCAConfigProperties;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCABinaryImage;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;

/**
 * Created by mashiur on 5/9/16.
 */
public class DCAUploadComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAUploadComponent.class);
    private static final String TMP_UPLOAD_DIR = DCAConfigProperties.getTemporaryDirectory().get();

    private DCALabel successMessageComponent = new DCALabel("Uploaded Successfully", "successMessage");
    private DCALabel errorMessageComponent = new DCALabel("Failed To upload", "errorMessage");

    private Embedded imagePlaceHolder;
    private Upload uploadComponent = new Upload();
    private DCAUploadComponent dcaUploadComponent;
    private File file;
    private DCABinaryImage binaryImage;

    public DCAUploadComponent(String styleName, boolean isBinaryImage) {
        this.dcaUploadComponent = this;
        this.setStyleName(styleName);

        if (isBinaryImage) {
            imagePlaceHolder = new Embedded();
            imagePlaceHolder.setStyleName("logo-place-holder");
            imagePlaceHolder.setVisible(false);

            this.addComponent(imagePlaceHolder);
        }

        this.addComponent(uploadComponent);

        this.uploadComponent.setButtonCaption("Edit Logo");
        this.uploadComponent.setImmediate(true);

        this.uploadComponent.setReceiver(new Upload.Receiver() {
            @Override
            public OutputStream receiveUpload(String filename, String mimeType) {
                deleteFile();
                if (logger.isDebugEnabled()) {
                    logger.debug("Upload Received");
                }

                dcaUploadComponent.removeComponent(successMessageComponent);
                dcaUploadComponent.removeComponent(errorMessageComponent);

                if (isBinaryImage) {
                    binaryImage = new DCABinaryImage();
                    binaryImage.setFileName(filename);
                    binaryImage.setMimeType(mimeType);
                }

                FileOutputStream fos = null;

                try {
                    String generatedFileName = String.format("%s_%s", System.currentTimeMillis(), filename);
                    file = new File(TMP_UPLOAD_DIR, generatedFileName);
                    fos = new FileOutputStream(file);
                } catch (Exception e) {
                    logger.error("Error : ", e);
                    dcaUploadComponent.addComponent(errorMessageComponent);
                    uploadComponent.interruptUpload();
                    return new NullOutputStream();
                }

                return fos;
            }
        });

        this.uploadComponent.addSucceededListener(new Upload.SucceededListener() {
            @Override
            public void uploadSucceeded(Upload.SucceededEvent event) {
                if (logger.isDebugEnabled()) {
                    logger.debug("File Upload Successfully done");
                }

                dcaUploadComponent.addComponent(successMessageComponent);

                if (isBinaryImage) {
                    dcaUploadComponent.removeComponent(imagePlaceHolder);
                    dcaUploadComponent.addComponentAsFirst(imagePlaceHolder);
                    if (file == null) {
                        logger.info("fileResource null found");
                    } else if (!file.exists()) {
                        logger.info("File  does not exists");
                    }

                    FileResource fileResource = new FileResource(file);
                    UI.getCurrent().access(() -> {
                        imagePlaceHolder.setVisible(true);
                        imagePlaceHolder.setSource(fileResource);
                    });
                }
            }
        });

    }

    public DCABinaryImage getBinaryImage() {
        if (file == null) {
            return null;
        }
        try {
            binaryImage.setBase64EncodeString(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(file)));
            return binaryImage;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void detach() {
        super.detach();
        deleteFile();
    }

    private void deleteFile() {
        if (file != null) {
            file.delete();
        }
    }
}
