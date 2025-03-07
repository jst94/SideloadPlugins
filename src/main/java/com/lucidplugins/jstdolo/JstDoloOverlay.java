package com.lucidplugins.jstdolo;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

public class JstDoloOverlay extends OverlayPanel {
    private final Client client;
    private final JstDoloConfig config;
    private final JstDoloPlugin plugin;

    @Inject
    public JstDoloOverlay(Client client, JstDoloConfig config, JstDoloPlugin plugin) {
        super();
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.drawOverlay()) {
            return null;
        }

        String username = client.getLocalPlayer() != null ? client.getLocalPlayer().getName() : "";
        boolean isMain = username.equals(config.mainUsername());
        boolean isAlt = username.equals(config.altUsername());

        if (!isMain && !isAlt) {
            return null;
        }

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("JST DOLO " + (isMain ? "(Main)" : "(Alt)"))
                .color(Color.WHITE)
                .build());

        if (plugin.isInGame()) {
            // Game time remaining
            long timeElapsed = Duration.between(plugin.getGameStartTime(), Instant.now()).getSeconds();
            long timeRemaining = Math.max(0, 750 - timeElapsed); // 12:30 = 750 seconds
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Time Remaining:")
                    .right(formatTime(timeRemaining))
                    .build());

            // Fragment count
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Fragments:")
                    .right(String.valueOf(plugin.getFragmentsCollected()))
                    .build());

            if (isMain) {
                boolean hasEnoughFragments = plugin.getFragmentsCollected() >= 8;
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Fragment Goal:")
                        .right(hasEnoughFragments ? "Met (8+)" : "Need more")
                        .rightColor(hasEnoughFragments ? Color.GREEN : Color.RED)
                        .build());
            } else if (isAlt) {
                boolean hasEnoughFragments = plugin.getFragmentsCollected() >= 16;
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Fragment Goal:")
                        .right(hasEnoughFragments ? "Met (16+)" : "Need more")
                        .rightColor(hasEnoughFragments ? Color.GREEN : Color.RED)
                        .build());
            }
        } else {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Status:")
                    .right("Not in game")
                    .build());
        }

        return super.render(graphics);
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }
}