package com.lucidplugins.lucidpluginhelper;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.WidgetLoaded;

@Singleton
@PluginDescriptor(
        name = "<html><font color=\"#32CD32\">Lucid</font> <font color=\"#FF6B6B\">Plugin Helper</font></html>",
        description = "Helps gather information for plugin development",
        tags = {"development", "helper", "debug"}
)
@Slf4j
public class LucidPluginHelper extends Plugin {
    @Inject
    private Client client;

    @Inject
    private LucidPluginHelperConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private LucidPluginHelperOverlay overlay;

    private final List<String> gameObjectInfo = new ArrayList<>();
    private final List<String> npcInfo = new ArrayList<>();
    private final List<String> groundItemInfo = new ArrayList<>();
    private final List<String> widgetInfo = new ArrayList<>();
    private final List<String> packetInfo = new ArrayList<>();

    private static final int MAX_DISPLAY_ENTRIES = 5;

    private String[] lastInteraction = null;
    private String lastEventHash = "";

    @Provides
    LucidPluginHelperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(LucidPluginHelperConfig.class);
    }

    @Override
    protected void startUp() {
        gameObjectInfo.clear();
        npcInfo.clear();
        groundItemInfo.clear();
        widgetInfo.clear();
        packetInfo.clear();
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (!config.enableMenuEntryLogging()) return;

        // Skip logging cancel events unless specifically wanted
        if (event.getType() == MenuAction.CANCEL.getId() && !config.enablePacketLogging()) {
            return;
        }

        // Create a hash of the current event to prevent duplicates
        String eventHash = event.getOption() + event.getTarget() + event.getIdentifier() + event.getType();
        if (eventHash.equals(lastEventHash)) {
            return;
        }
        lastEventHash = eventHash;

        // Only log non-empty or significant interactions
        if (event.getIdentifier() != 0 || 
            event.getActionParam0() != 0 || 
            !event.getTarget().isEmpty() || 
            event.getType() != MenuAction.WALK.getId()) {
            
            // Get the corresponding MenuAction enum name if possible
            String actionName = "UNKNOWN";
            for (MenuAction action : MenuAction.values()) {
                if (action.getId() == event.getType()) {
                    actionName = action.name();
                    break;
                }
            }
            
            String[] info = new String[]{
                "ID: " + (event.getIdentifier() != 0 ? event.getIdentifier() : "-"),
                "Widget ID: " + (event.getActionParam0() != 0 ? event.getActionParam0() : "-"),
                "Param1: " + (event.getActionParam1() != 0 ? event.getActionParam1() : "-"),
                "Action: " + actionName + " (" + event.getType() + ")",
                "Option: " + event.getOption(),
                "Target: " + (event.getTarget().isEmpty() ? "-" : event.getTarget())
            };
            lastInteraction = info;
            
            // Log in a clean format
            StringBuilder logEntry = new StringBuilder("\n=== Menu Entry Added ===\n");
            for (String line : info) {
                logEntry.append(line).append("\n");
            }
            logEntry.append("=====================");
            log.info(logEntry.toString());
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        if (!config.enableGameObjectLogging()) return;
        
        GameObject obj = event.getGameObject();
        String info = String.format("\nGameObject:\nID: %d\nLocation: %s\nName: %s",
                obj.getId(), obj.getWorldLocation(), client.getObjectDefinition(obj.getId()).getName());
        if (!gameObjectInfo.contains(info)) {
            gameObjectInfo.add(info);
            log.info(info);
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (!config.enableNpcLogging()) return;
        
        NPC npc = event.getNpc();
        String info = String.format("\nNPC:\nID: %d\nName: %s\nLocation: %s",
                npc.getId(), npc.getName(), npc.getWorldLocation());
        if (!npcInfo.contains(info)) {
            npcInfo.add(info);
            log.info(info);
        }
    }

    @Subscribe
    public void onItemSpawned(ItemSpawned event) {
        if (!config.enableGroundItemLogging()) return;
        
        TileItem item = event.getItem();
        String info = String.format("\nGround Item:\nID: %d\nLocation: %s\nQuantity: %d",
                item.getId(), event.getTile().getWorldLocation(), item.getQuantity());
        if (!groundItemInfo.contains(info)) {
            groundItemInfo.add(info);
            log.info(info);
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (config.enableWidgetLogging()) {
            Widget[] widgets = client.getWidget(event.getGroupId(), 0).getChildren();
            if (widgets == null || widgets.length == 0) return;

            StringBuilder logEntry = new StringBuilder("\nWidget:\n");
            Arrays.stream(widgets).forEach(w -> {
                if (w == null) return;
                logEntry.append(String.format("ID: %d\nType: %d\nContentType: %d\nText: %s\n",
                        w.getId(), w.getType(), w.getContentType(), w.getText()));
            });
            String info = logEntry.toString();
            if (!widgetInfo.contains(info)) {
                widgetInfo.add(info);
                log.info(info);
            }
        }
        
        if (config.enablePacketLogging()) {
            String info = String.format("Widget Loaded - ID: %d, Group ID: %d",
                    event.getGroupId(), event.getGroupId());
            if (!packetInfo.contains(info)) {
                packetInfo.add(info);
                log.info(info);
            }
        }
    }

    public void copyToClipboard(List<String> data) {
        if (data.isEmpty()) return;
        
        StringBuilder sb = new StringBuilder();
        for (String line : data) {
            sb.append(line).append("\n");
        }
        
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(sb.toString());
        clipboard.setContents(selection, null);
    }

    public List<String[]> getLatestDebugInfo() {
        List<String[]> latestInfo = new ArrayList<>();
        
        if (!gameObjectInfo.isEmpty()) {
            String[] parts = gameObjectInfo.get(gameObjectInfo.size() - 1).split("Location:|Name:");
            if (parts.length >= 2) {
                String id = parts[0].replaceAll("GameObject - ID: ", "").trim();
                String name = parts[parts.length - 1].trim();
                latestInfo.add(new String[]{"GameObject", id + " - " + name});
            }
        }
        if (!npcInfo.isEmpty()) {
            String[] parts = npcInfo.get(npcInfo.size() - 1).split("Location:|Name:");
            if (parts.length >= 2) {
                String id = parts[0].replaceAll("NPC - ID: ", "").trim();
                String name = parts[parts.length - 1].trim();
                latestInfo.add(new String[]{"NPC", id + " - " + name});
            }
        }
        if (!groundItemInfo.isEmpty()) {
            String[] parts = groundItemInfo.get(groundItemInfo.size() - 1).split("Location:|Quantity:");
            if (parts.length >= 2) {
                String id = parts[0].replaceAll("GroundItem - ID: ", "").trim();
                String qty = parts[parts.length - 1].trim();
                latestInfo.add(new String[]{"Ground Item", id + " (x" + qty + ")"});
            }
        }
        if (!widgetInfo.isEmpty()) {
            String[] parts = widgetInfo.get(widgetInfo.size() - 1).split("Text:");
            if (parts.length >= 2) {
                String id = parts[0].replaceAll("Widget - ID: ", "").replaceAll(", Type:.*", "").trim();
                String text = parts[1].trim();
                latestInfo.add(new String[]{"Widget", id + " - " + text});
            }
        }
        if (!packetInfo.isEmpty()) {
            String info = packetInfo.get(packetInfo.size() - 1);
            String[] parts = info.split(" - ", 2);
            if (parts.length == 2) {
                latestInfo.add(new String[]{"Packet", parts[1]});
            }
        }

        return latestInfo.subList(0, Math.min(latestInfo.size(), MAX_DISPLAY_ENTRIES));
    }

    public String[] getCurrentInteractionInfo() {
        if (lastInteraction == null) return null;
        
        // Filter out empty entries
        return Arrays.stream(lastInteraction)
            .filter(line -> !line.endsWith(": ") && !line.endsWith(": -"))
            .toArray(String[]::new);
    }
}
