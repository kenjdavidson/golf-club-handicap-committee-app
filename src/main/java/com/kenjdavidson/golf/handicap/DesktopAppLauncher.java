package com.kenjdavidson.golf.handicap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

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
        stage.setTitle("⛳ Golf Handicap Committee App");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.show();
    }
}
