package com.musictime.intellij.plugin;

import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

public class SoftwareCoStatusBarIconWidget implements StatusBarWidget {
    public static final Logger log = Logger.getLogger("SoftwareCoStatusBarKpmIconWidget");

    public static final String ICON_ID = "musictime.icon";

    private SoftwareCoSessionManager sessionMgr = SoftwareCoSessionManager.getInstance();

    private Icon icon = null;
    private String tooltip = "";
    private String id;

    private final IconPresentation presentation = new IconPresentation();
    private Consumer<MouseEvent> eventHandler;

    public SoftwareCoStatusBarIconWidget(String id) {
        this.id = id;
        eventHandler = new Consumer<MouseEvent>() {
            @Override
            public void consume(MouseEvent mouseEvent) {
                sessionMgr.statusBarClickHandler(mouseEvent, id);
            }
        };
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    class IconPresentation implements StatusBarWidget.IconPresentation {

        @NotNull
        @Override
        public Icon getIcon() {
            return SoftwareCoStatusBarIconWidget.this.icon;
        }

        @Nullable
        @Override
        public String getTooltipText() {
            return SoftwareCoStatusBarIconWidget.this.tooltip;
        }

        @Nullable
        @Override
        public Consumer<MouseEvent> getClickConsumer() {
            return eventHandler;
        }
    }

    @Override
    public WidgetPresentation getPresentation(@NotNull PlatformType type) {
        return presentation;
    }

    @NotNull
    @Override
    public String ID() {
        return id;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
    }

    @Override
    public void dispose() {
    }
}