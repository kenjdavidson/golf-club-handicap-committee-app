package com.kenjdavidson.golf.handicap.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleFileUploadCardTest {

    @Test
    void uploadPanelDoesNotRenderBorderStyles() {
        UI ui = new UI();
        UI.setCurrent(ui);
        try {
            SingleFileUploadCard card = new SingleFileUploadCard();

            List<Component> children = card.getChildren().toList();
            HorizontalLayout uploadPanel = (HorizontalLayout) children.getFirst();

            assertTrue(isMissing(uploadPanel.getStyle().get("border-style")));
            assertTrue(isMissing(uploadPanel.getStyle().get("border-width")));
            assertTrue(isMissing(uploadPanel.getStyle().get("border-color")));
        } finally {
            UI.setCurrent(null);
        }
    }

    private boolean isMissing(String value) {
        return value == null || value.isBlank();
    }
}
