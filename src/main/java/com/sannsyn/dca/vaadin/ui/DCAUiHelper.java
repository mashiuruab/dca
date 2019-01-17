package com.sannsyn.dca.vaadin.ui;

import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.recommenders.DCACloseIconComponent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * Utility class for different ui related stuffs.
 * <p>
 * Created by jobaer on 6/17/16.
 */
public class DCAUiHelper {
    public static Label createSpacer(float width) {
        Label spacer = new Label();
        spacer.setWidth(width, PERCENTAGE);
        return spacer;
    }

    public static void addSeparatorWithPadding(Layout layout) {
        CssLayout wrapper = new CssLayout();
        wrapper.addStyleName("bottom-border-wrapper");
        Label c = new Label();
        c.addStyleName("bottom-border-with-padding");
        wrapper.addComponent(c);
        layout.addComponent(wrapper);
    }

    public static CssLayout wrapWithCssLayout(Component component, String styleName) {
        CssLayout cssLayout = new CssLayout();
        cssLayout.setStyleName(styleName);
        cssLayout.addComponent(component);
        return cssLayout;
    }

    public static CssLayout wrapWithCssLayout(Component component, String styleName, float widthPercentage) {
        CssLayout cssLayout = wrapWithCssLayout(component, styleName);
        cssLayout.setWidth(widthPercentage, PERCENTAGE);
        return cssLayout;
    }

    public static CssLayout createRemoveIcon(LayoutEvents.LayoutClickListener listener) {
        String randomId = UUID.randomUUID().toString();
        DCACloseIconComponent removeComponent = new DCACloseIconComponent();
        removeComponent.setId(randomId);
        removeComponent.addLayoutClickListener(listener);
        return removeComponent;
    }

    public static CssLayout createCssLayout(String styleName, float width) {
        CssLayout layout = new CssLayout();
        layout.setWidth(width, PERCENTAGE);
        layout.addStyleName(styleName);
        return layout;
    }

    public static Layout wrapLabelForSelection(Label label) {
        VerticalLayout c4 = new VerticalLayout();
        c4.setSpacing(true);
        c4.addComponent(label);
        return c4;
    }

    public static CssLayout makeImageComponent(String styleName, String imageUrl) {
        Resource imageResource = new ExternalResource(imageUrl);
        Image image = new Image("", imageResource);

        final CssLayout imageLayout = new CssLayout();
        imageLayout.setStyleName(styleName);
        imageLayout.addComponent(image);
        return imageLayout;
    }

    private static Image createImage(String imageUrl, int widthInPixel, Sizeable.Unit unit) {
        Image image = new Image();
        image.setWidth(widthInPixel, unit);
        if (StringUtils.isNotBlank(imageUrl)) {
            ExternalResource resource = new ExternalResource(imageUrl);
            image.setSource(resource);
        }
        return image;
    }

    public static Component createImageLabel(String thumbnailUrl, Integer width) {
        CssLayout imageLayout = new CssLayout();

        Image image = createImage(thumbnailUrl, 40, Sizeable.Unit.PIXELS);
        imageLayout.addComponent(image);

        imageLayout.setWidth(5, PERCENTAGE);
        return imageLayout;
    }

    public static Component createImageLabel(String thumbnailUrl, Integer width, Sizeable.Unit unit) {
        CssLayout imageLayout = new CssLayout();

        Image image = createImage(thumbnailUrl, 100, unit);
        imageLayout.addComponent(image);

        imageLayout.setWidth(width, unit);
        return imageLayout;
    }

    public static CssLayout createErrorLayout(float width) {
        CssLayout errorLayout = new CssLayout();
        errorLayout.addStyleName("no-result-found");
        errorLayout.setWidth(width, PERCENTAGE);

        CssLayout imageSad = makeImageComponent("error-logo", "static/img/sad-face_128.png");
        CssLayout layout = wrapWithCssLayout(imageSad, "error-logo-wrapper");
        errorLayout.addComponent(layout);

        Label firstLabel = new Label("It looks like desert in here");
        CssLayout firstLabelWrapper = wrapWithCssLayout(firstLabel, "first-error-label");
        Label secondLabel = new Label("Your search do not have any result");
        CssLayout secondLabelWrapper = wrapWithCssLayout(secondLabel, "second-error-label");

        errorLayout.addComponent(firstLabelWrapper);
        errorLayout.addComponent(secondLabelWrapper);
        return errorLayout;
    }

    public static Component wrapWithLabelPipe(Component editor, PipeProperty property) {
        Layout cssLayout = new CssLayout();
        cssLayout.setStyleName("create-recommender-form-field");
        cssLayout.setWidth(100, PERCENTAGE);

        String propertyName = property.getPropertyName();
        Label label = new Label(propertyName + ": ");
        label.setWidth(30, PERCENTAGE);

        editor.setWidth(69, PERCENTAGE);

        cssLayout.addComponent(label);
        cssLayout.addComponent(editor);

        return cssLayout;
    }

    public static Component wrapWithEmptyLabel(Component editor, PipeProperty property) {
        Layout cssLayout = new CssLayout();
        cssLayout.setStyleName("create-recommender-form-field");
        cssLayout.setWidth(100, PERCENTAGE);

        Label label = new Label();
        label.setWidth(30, PERCENTAGE);

        editor.setWidth(69, PERCENTAGE);

        cssLayout.addComponent(label);
        cssLayout.addComponent(editor);

        return cssLayout;
    }

    public static CssLayout groupComponentWithLabel(String labelText, Component component) {
        Label label = new Label(labelText);
        label.addStyleName("textfield-label");
        label.setWidthUndefined();

        component.addStyleName("textfield-input");
        component.setWidthUndefined();

        CssLayout layout = wrapWithCssLayout(label, "textfield-with-label-wrapper");
        layout.addComponent(component);
        return layout;
    }


    public static void addSeparator(ComponentContainer comp) {
        CssLayout separator = new CssLayout();
        separator.addStyleName("dca-separator");
        separator.setWidth(100, PERCENTAGE);
        comp.addComponent(separator);
    }

    public static void addComponentAsLast(final Component pComponent, final CssLayout layout) {
        runInUiThread(() -> {
            layout.addComponent(pComponent);
        });
    }

    /**
     * Tries to run the given runnable inside ui thread, if the ui thread is available.
     * If the UI thread is not available then just run the code.
     *
     * @param runnable The code fragment to be run inside UI thread.
     */
    public static void runInUiThread(Runnable runnable) {
        UI currentUi = UI.getCurrent();
        if (currentUi != null) {
            currentUi.access(runnable);
        } else {
            runnable.run();
        }
    }
}
