package com.sannsyn.dca.vaadin.component.custom.navigation;

import com.sannsyn.dca.model.config.DCASelectedService;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Image;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * Created by mashiur on 8/4/16.
 */
public class DCAExternalImageComponent extends CssLayout {
    private static  final String customerLogoUrl = "static/img/logo.png";

    public DCAExternalImageComponent(DCASelectedService selectedService) {
        this.setStyleName("menu-image-wrapper logo-holder");

        String customerImageUrl = StringUtils.isEmpty(selectedService.getAccount().getLogoUrl()) ? customerLogoUrl :
                String.format("%s?%s", selectedService.getAccount().getLogoUrl(), UUID.randomUUID().toString());

        Resource imageResource = new ExternalResource(customerImageUrl);
        Image image = new Image("", imageResource);

        this.addComponent(image);
    }

    public DCAExternalImageComponent(String imageUrl) {
        this.setStyleName("bottom-menu-image-wrapper");

        Resource imageResource = new ExternalResource(imageUrl);
        Image image = new Image("", imageResource);

        this.addComponent(image);
    }
}
