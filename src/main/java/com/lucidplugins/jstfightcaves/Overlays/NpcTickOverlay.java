package com.lucidplugins.jstfightcaves.Overlays;

import com.example.EthanApiPlugin.EthanApiPlugin;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class NpcTickOverlay
extends Overlay {
    private final Client client;
    private final PanelComponent panelComponent = new PanelComponent();
    private final EthanApiPlugin plugin;

    @Inject
    public NpcTickOverlay(Client client, EthanApiPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.HIGH);
    }

    public Dimension render(Graphics2D graphics) {
        this.panelComponent.getChildren().clear();
        return this.panelComponent.render(graphics);
    }
}
