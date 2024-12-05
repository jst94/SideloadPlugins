package com.lucidplugins.lucidpluginhelper;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import java.awt.Color;
import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class LucidPluginHelperOverlay extends OverlayPanel {
    private final LucidPluginHelper plugin;
    private final LucidPluginHelperConfig config;
    private static final Color HIGHLIGHT_COLOR = new Color(0, 255, 0); // Bright green

    @Inject
    private LucidPluginHelperOverlay(LucidPluginHelper plugin, LucidPluginHelperConfig config) {
        super(plugin);
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showDebugOverlay()) {
            return null;
        }

        String[] currentInfo = plugin.getCurrentInteractionInfo();
        if (currentInfo == null) {
            return null;
        }

        for (String line : currentInfo) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                panelComponent.getChildren().add(
                    LineComponent.builder()
                        .left(parts[0].trim())
                        .right(parts[1].trim())
                        .leftColor(HIGHLIGHT_COLOR)
                        .rightColor(Color.WHITE)
                        .build()
                );
            } else {
                panelComponent.getChildren().add(
                    LineComponent.builder()
                        .left(line)
                        .leftColor(Color.WHITE)
                        .build()
                );
            }
        }

        return super.render(graphics);
    }
}
