package com.kenjdavidson.golf.handicap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;
import java.net.URL;

@Slf4j
@Component
public class SystemTrayLauncher implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
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
            MenuItem openItem = new MenuItem("Open Handicap App");
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
