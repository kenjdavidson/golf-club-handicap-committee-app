package com.kenjdavidson.golf.handicap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class DesktopAppLauncher extends Application {

    public static void launchApp(String[] args) {
        Application.launch(DesktopAppLauncher.class, args);
    }

    @Override
    public void start(Stage stage) {
        Platform.setImplicitExit(true);

        WebView webView = new WebView();
        webView.getEngine().load("http://localhost:8080");

        Scene scene = new Scene(webView, 1280, 720);
        stage.setTitle("⛳ Golf Handicap Committee App");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.show();
    }
}
