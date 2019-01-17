package com.sannsyn.dca.vaadin.icons;

import com.vaadin.server.FontIcon;

/**
 * A list of custom icon fonts
 * <p>
 * Created by jobaer on 6/2/16.
 */
public enum SannsynIcons implements FontIcon {
    ADD_NEW(0XE901),
    COLLAPSE_OUT(0XE91C),
    DOWN(0XE918),
    LEFT(0XE919),
    RIGHT(0XE91A),
    UP(0XE91B),
    ACCOUNTS(0XE900),
    ANALYTICS(0XE902),
    CAMPAIGNS(0XE903),
    CLOSE_LINE(0XE904),
    CLOSE(0XE905),
    COLLAPSE_IN(0XE906),
    COMPLETED(0XE907),
    CONTROLLER(0XE908),
    DASHBOARD(0xE909),
    EDIT(0xE90A),
    FULL_SCREEN(0xE90B),
    HELP(0XE90C),
    IMAGE(0XE90D),
    INSPECT_ITEM(0XE90E),
    MENU(0XE90F),
    NEWSLETTER(0XE910),
    PREFERENCES(0XE911),
    RIGHT_LEFT(0XE912),
    ROLES(0XE913),
    SEARCH(0XE914),
    SETTINGS(0XE915),
    TOP_BOTTOM(0XE916),
    USERS(0XE917);


    public static final String FONT_FAMILY = "SannsynIcons";
    private int codepoint;

    SannsynIcons(int codepoint) {
        this.codepoint = codepoint;
    }

    @Override
    public String getMIMEType() {
        throw new UnsupportedOperationException("Fonts should not be used where a MIME type is needed.");
    }

    @Override
    public String getFontFamily() {
        return SannsynIcons.FONT_FAMILY;
    }

    @Override
    public int getCodepoint() {
        return codepoint;
    }

    @Override
    public String getHtml() {
        return "<span class=\"v-icon SannsynIcons\">&#x"
            + Integer.toHexString(getCodepoint()) + ";</span>";
    }

    public static SannsynIcons fromCodepoint(final int codepoint) {
        for (SannsynIcons f : values()) {
            if (f.getCodepoint() == codepoint) {
                return f;
            }
        }
        throw new IllegalArgumentException("Codepoint " + codepoint + " not found in FontAwesome");
    }

}
