package com.kenjdavidson.golf.handicap;

import lombok.extern.slf4j.Slf4j;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

@Slf4j
public class DesktopAppLauncher {

    public static void launchApp(String[] args) {
        String url = "http://localhost:8080";
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pb;

        // 1. First, spawn the borderless app-mode browser window
        try {
            if (os.contains("win")) {
                String chromePath = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
                String edgePath = "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe";
                String profileDir = "--user-data-dir=" + System.getProperty("user.home") + "\\.golf-handicap-profile";

                if (new File(chromePath).exists()) {
                    pb = new ProcessBuilder(chromePath, "--app=" + url, profileDir);
                } else if (new File(edgePath).exists()) {
                    pb = new ProcessBuilder(edgePath, "--app=" + url, profileDir);
                } else {
                    pb = new ProcessBuilder("cmd", "/c", "start", url);
                }
            } else if (os.contains("mac")) {
                String macChrome = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
                String profileDir = "--user-data-dir=" + System.getProperty("user.home") + "/.golf-handicap-profile";

                if (new File(macChrome).exists()) {
                    pb = new ProcessBuilder(macChrome, "--app=" + url, profileDir);
                } else {
                    pb = new ProcessBuilder("open", url);
                }
            } else {
                pb = new ProcessBuilder("xdg-open", url);
            }

            log.info("Spawning dedicated native desktop window frame...");
            pb.start();

        } catch (IOException e) {
            log.error("Failed to cleanly spawn the native desktop web wrapper", e);
        }

        // 2. Next, register the Taskbar / System Tray Icon wrapper
        setupSystemTrayIcon(args);
    }

    private static void setupSystemTrayIcon(String[] args) {
        if (!SystemTray.isSupported()) {
            log.warn("System tray/taskbar icons are not supported on this operating system.");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();

            // Place your custom .png icon inside: src/main/resources/META-INF/resources/icons/icon.png
            // (Vaadin creates this file by default, or you can point to any PNG in your classpath)
            URL imageURL = DesktopAppLauncher.class.getResource("/static/icons/app-icon.png");
            Image iconImage = Toolkit.getDefaultToolkit().getImage(imageURL);

            // Construct the native OS context menu wrapper
            PopupMenu popupMenu = new PopupMenu();

            // Option A: "Open Dashboard" -> Re-spawns or navigates to the app window
            MenuItem openItem = new MenuItem("Open App Dashboard");
            openItem.addActionListener(e -> {
                // Simply re-trigger the launch script to open the browser window frame back up
                try {
                    Desktop.getDesktop().browse(new URI("http://localhost:8080"));
                } catch (Exception ex) {
                    log.error("Failed to re-open dashboard link", ex);
                }
            });

            // Option B: "Exit Application" -> Destroys everything instantly
            MenuItem exitItem = new MenuItem("Exit Application");
            exitItem.addActionListener(e -> {
                log.info("🛑 Tray icon shutdown requested. Terminating application context instantly...");

                // Let the main HandicapApplication class handle the Spring context cleanup
                HandicapApplication.shutdownAndExit();
            });

            popupMenu.add(openItem);
            popupMenu.addSeparator();
            popupMenu.add(exitItem);

            TrayIcon trayIcon = new TrayIcon(iconImage, "Golf Handicap Committee App", popupMenu);
            trayIcon.setImageAutoSize(true);

            // Optional Native Touch: Double-clicking the taskbar icon automatically opens the dashboard window
            trayIcon.addActionListener(e -> {
                try {
                    Desktop.getDesktop().browse(new URI("http://localhost:8080"));
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            });

            tray.add(trayIcon);
            log.info("System Tray / Taskbar Icon successfully registered!");

        } catch (Exception e) {
            log.error("Error setting up OS system tray application icon layout", e);
        }
    }
}