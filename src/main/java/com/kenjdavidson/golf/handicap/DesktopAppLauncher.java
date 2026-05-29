package com.kenjdavidson.golf.handicap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Taskbar;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

public class DesktopAppLauncher extends Application {
    private static final String DEFAULT_URL = "http://localhost:8080";
    private static final AtomicReference<String> launchUrl = new AtomicReference<>(DEFAULT_URL);

    public static void launchApp(String[] args) {
        launchUrl.set(DEFAULT_URL);
        Application.launch(DesktopAppLauncher.class, args);
    }

    public static void launchApp(String[] args, String url) {
        launchUrl.set(url);
        Application.launch(DesktopAppLauncher.class, args);
    }

    @Override
    public void start(Stage stage) {
        Platform.setImplicitExit(true);

        WebView webView = new WebView();
        webView.getEngine().load(launchUrl.get());

        Scene scene = new Scene(webView, 1280, 720);
        stage.setTitle("Golf Handicap Committee App");
        configureWindowAndTaskbarIcons(stage);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.show();
    }

    private void configureWindowAndTaskbarIcons(Stage stage) {
        try (InputStream iconStream = DesktopAppLauncher.class.getResourceAsStream("/static/icons/app-icon.png")) {
            if (iconStream == null) {
                return;
            }
            byte[] iconBytes = iconStream.readAllBytes();
            stage.getIcons().add(new Image(new ByteArrayInputStream(iconBytes)));
            setTaskbarIcon(iconBytes);
        } catch (IOException ignored) {
        }
    }

    private void setTaskbarIcon(byte[] iconBytes) {
        if (!Taskbar.isTaskbarSupported()) {
            return;
        }
        Taskbar taskbar = Taskbar.getTaskbar();
        if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
            return;
        }
        try {
            BufferedImage iconImage = ImageIO.read(new ByteArrayInputStream(iconBytes));
            if (iconImage != null) {
                taskbar.setIconImage(iconImage);
            }
        } catch (IOException | UnsupportedOperationException | SecurityException ignored) {
        }
    }
}
