package com.lucidplugins.jstfightcaves;

import com.lucidplugins.jstfightcaves.Variables.UiLayoutOption;
import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class CavesOverlay extends OverlayPanel {
    private final Client client;
    private final CavesPlugin plugin;
    private final CavesConfig config;
    private final Color backgroundColour = new Color(70, 61, 50, 156);

    @Inject
    public CavesOverlay(Client client, CavesPlugin plugin, CavesConfig config) {
        super();
        setPosition(OverlayPosition.TOP_LEFT);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        UiLayoutOption selectedLayout = this.config.uiLayoutOption();
        this.panelComponent.getChildren().clear();
        graphics.setStroke(new BasicStroke(1.0f));
        this.setResizable(true);
        this.setMovable(true);
        this.setSnappable(true);
        
        this.panelComponent.setBackgroundColor(this.backgroundColour);
        switch (selectedLayout) {
            case FULL: {
                this.addFullOverlay();
                break;
            }
            case SIMPLE: {
                this.addSimpleOverlay();
                break;
            }
        }
        
        return super.render(graphics);
    }

    private void addFullOverlay() {
        this.panelComponent.setPreferredSize(new Dimension(250, 200));
        this.panelComponent.getChildren().add(TitleComponent.builder()
            .text("Auto Fight Caves")
            .color(new Color(174, 156, 216))
            .build());

        // Add status indicator
        this.panelComponent.getChildren().add(LineComponent.builder()
            .left("Status:")
            .right(this.plugin.isStarted() ? "Running" : "Stopped")
            .rightColor(this.plugin.isStarted() ? Color.GREEN : Color.RED)
            .build());

        this.panelComponent.getChildren().add(LineComponent.builder()
            .left("Current State:")
            .leftColor(Color.WHITE)
            .right(String.valueOf(this.plugin.getCurrentState()))
            .rightColor(Color.WHITE)
            .build());

        // Only show runtime if plugin is started and startTime is not null
        if (this.plugin.isStarted() && this.plugin.getStartTime() != null) {
            Duration runtime = Duration.between(this.plugin.getStartTime(), Instant.now());
            String runtimeStr = this.formatDuration(runtime);
            this.panelComponent.getChildren().add(LineComponent.builder()
                .left("Runtime:")
                .leftColor(Color.WHITE)
                .right(runtimeStr)
                .rightColor(Color.WHITE)
                .build());
        }

        this.panelComponent.getChildren().add(LineComponent.builder()
            .left("Completions:")
            .right(String.valueOf(this.plugin.completionCount))
            .leftColor(Color.WHITE)
            .rightColor(Color.WHITE)
            .build());

        this.panelComponent.getChildren().add(LineComponent.builder()
            .left("Blowpipe Darts:")
            .right(String.valueOf(this.plugin.dartsInBlowpipe))
            .leftColor(Color.WHITE)
            .rightColor(Color.WHITE)
            .build());

        this.panelComponent.getChildren().add(LineComponent.builder()
            .left("Blowpipe Scales:")
            .right(String.valueOf(this.plugin.scalesInBlowpipe))
            .leftColor(Color.WHITE)
            .rightColor(Color.WHITE)
            .build());

        this.panelComponent.getChildren().add(LineComponent.builder()
            .left("Prayer Style:")
            .right(String.valueOf(this.config.prayerStyle()))
            .leftColor(Color.WHITE)
            .rightColor(Color.WHITE)
            .build());
    }

    private void addSimpleOverlay() {
        this.panelComponent.setPreferredSize(new Dimension(150, 30));
        
        // Add status indicator
        this.panelComponent.getChildren().add(LineComponent.builder()
            .left("Status:")
            .right(this.plugin.isStarted() ? "Running" : "Stopped")
            .rightColor(this.plugin.isStarted() ? Color.GREEN : Color.RED)
            .build());

        this.panelComponent.getChildren().add(LineComponent.builder()
            .left("Attack Timer:")
            .right(String.valueOf(this.plugin.attackDelayTimer))
            .leftColor(Color.WHITE)
            .rightColor(Color.WHITE)
            .build());
    }

    private String formatDuration(Duration duration) {
        if (duration.toHours() > 0L) {
            return String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
        }
        return String.format("%02d:%02d", duration.toMinutes(), duration.toSecondsPart());
    }
}
