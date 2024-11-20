package com.lucidplugins.jstfightcaves.Overlays;

import com.lucidplugins.jstfightcaves.CavesPlugin;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class TickOverlay extends Overlay {
    private final Client client;
    private final CavesPlugin plugin;

    @Inject
    public TickOverlay(Client client, CavesPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.isStarted()) {
            return null;
        }

        Map<Integer, List<String>> tickData = getTickData();
        // Add rendering logic here
        return null;
    }

    public Map<Integer, List<String>> getTickData() {
        HashMap<Integer, List<String>> npcsAttackingOnTick = new HashMap<Integer, List<String>>();
        for (int i = 0; i <= 8; ++i) {
            npcsAttackingOnTick.put(i, new ArrayList());
        }
        NPC[] npcs = this.client.getCachedNPCs();
        for (Map.Entry<Integer, Integer> entry : this.plugin.getNpcAttackTimers().entrySet()) {
            NPC npc;
            int npcIndex = entry.getKey();
            int ticksUntilAttack = entry.getValue();
            if (ticksUntilAttack > 8 || (npc = npcs[npcIndex]) == null || !this.isWithinAttackRange(npc)) continue;
            ((List)npcsAttackingOnTick.get(ticksUntilAttack)).add("--> " + npc.getName());
        }
        return npcsAttackingOnTick;
    }

    private boolean isWithinAttackRange(NPC npc) {
        Player localPlayer = this.client.getLocalPlayer();
        if (localPlayer == null) {
            return false;
        }
        WorldArea npcArea = npc.getWorldArea();
        WorldPoint playerLocation = localPlayer.getWorldLocation();
        return this.plugin.isWithinAttackRange(npcArea, playerLocation, this.plugin.findCaveNpc(npc.getName()));
    }
}
