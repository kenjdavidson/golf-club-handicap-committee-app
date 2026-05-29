package com.kenjdavidson.golf.handicap.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import kotlin.Unit;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleFileUploadCardTest {

    @Test
    void uploadPanelDoesNotRenderBorderStyles() {
        UI ui = new UI();
        UI.setCurrent(ui);
        try {
            SingleFileUploadCard card = new SingleFileUploadCard();

            List<Component> children = card.getChildren().toList();
            assertEquals(1, children.size(), "Upload card should only render the upload panel");
            HorizontalLayout uploadPanel = (HorizontalLayout) children.getFirst();

            assertTrue(isMissing(uploadPanel.getStyle().get("border-style")));
            assertTrue(isMissing(uploadPanel.getStyle().get("border-width")));
            assertTrue(isMissing(uploadPanel.getStyle().get("border-color")));
        } finally {
            UI.setCurrent(null);
        }
    }

    @Test
    void setVerifyHandlerImmediatelyVerifiesAlreadySelectedFile() throws Exception {
        UI ui = new UI();
        UI.setCurrent(ui);
        try {
            SingleFileUploadCard card = new SingleFileUploadCard();
            setField(card, "uploadedFileName", "rounds.pdf");
            setField(card, "uploadedBytes", new byte[] {1, 2, 3});

            AtomicReference<String> verifiedFile = new AtomicReference<>();
            card.setVerifyHandler((name, bytes) -> {
                verifiedFile.set(name + ":" + bytes.length);
                return Unit.INSTANCE;
            });

            assertEquals("rounds.pdf:3", verifiedFile.get());
        } finally {
            UI.setCurrent(null);
        }
    }

    private boolean isMissing(String value) {
        return value == null || value.isBlank();
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
