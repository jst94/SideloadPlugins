package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.Collections.ETileItem;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileItems;
import com.example.Packets.*;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;

import javax.swing.*;
import java.util.*;
import java.util.function.Predicate;

public class InteractionUtils
{
    static Client client = RuneLite.getInjector().getInstance(Client.class);
    private static final int INVENTORY_GROUP_ID = 149;

    // Coordinate area enum
    public enum CoordinateArea {
        INVENTORY,
        CHAT,
        MAIN_MODAL
    }

    // Packet interactions
    public static void queueClickPacketCoordinateArea() {
        MousePackets.queueClickPacket();
    }

    public static void queueClickPacketCoordinateArea(CoordinateArea area) {
        MousePackets.queueClickPacket();
    }

    // Movement
    public static void walk(WorldPoint point) {
        if (point != null) {
            MovementPackets.queueMovement(point);
        }
    }

    public static boolean isMoving() {
        return client.getLocalPlayer().getAnimation() != -1 || 
               client.getLocalPlayer().getPoseAnimation() != client.getLocalPlayer().getIdlePoseAnimation();
    }

    public static int getRunEnergy() {
        return client.getEnergy();
    }

    public static boolean isRunEnabled() {
        return client.getVarpValue(173) == 1;
    }

    // Distance calculations
    public static float distanceTo2DHypotenuse(WorldPoint point1, WorldPoint point2) {
        return (float) Math.hypot(point1.getX() - point2.getX(), point1.getY() - point2.getY());
    }

    public static float distanceTo2DHypotenuse(WorldPoint point1, WorldPoint point2, int size1, int size2) {
        WorldPoint mid1 = point1.dx(size1 / 2).dy(size1 / 2);
        WorldPoint mid2 = point2.dx(size2 / 2).dy(size2 / 2);
        return distanceTo2DHypotenuse(mid1, mid2);
    }

    public static float distanceTo2DHypotenuse(WorldPoint point1, WorldPoint point2, int size1, int size2, int plane) {
        if (point1.getPlane() != plane || point2.getPlane() != plane) return Float.MAX_VALUE;
        return distanceTo2DHypotenuse(point1, point2, size1, size2);
    }

    public static int approxDistanceTo(WorldPoint point1, WorldPoint point2) {
        return (int)Math.max(Math.abs(point1.getX() - point2.getX()), Math.abs(point1.getY() - point2.getY()));
    }

    // Widget interactions
    public static void widgetInteract(int parentId, int childId, String action) {
        Widget widget = client.getWidget(parentId, childId);
        if (widget != null) {
            WidgetPackets.queueWidgetAction(widget, action);
        }
    }

    public static void widgetInteract(int parentId, int childId, int grandchildId, String action) {
        Widget widget = client.getWidget(parentId, childId);
        if (widget != null && grandchildId != -1) {
            widget = widget.getChild(grandchildId);
        }
        if (widget != null) {
            WidgetPackets.queueWidgetAction(widget, action);
        }
    }

    public static void queueResumePause(int parentId, int childId, int grandchildId) {
        Widget widget = client.getWidget(parentId, childId);
        if (widget != null && grandchildId != -1) {
            widget = widget.getChild(grandchildId);
        }
        if (widget != null) {
            WidgetPackets.queueResumePause(widget.getId(), widget.getIndex());
        }
    }

    // Tile items
    public static List<ETileItem> getAllTileItems(Predicate<ETileItem> filter) {
        return TileItems.search().filter(filter).result();
    }

    public static boolean tileItemIdExistsWithinDistance(int id, int distance) {
        return TileItems.search().withId(id).withinDistance(distance).first().isPresent();
    }

    public static boolean tileItemNameExistsWithinDistance(String name, int distance) {
        return TileItems.search().withName(name).withinDistance(distance).first().isPresent();
    }

    // NPC utilities
    public static boolean isNpcInMeleeDistanceToPlayer(NPC npc) {
        if (npc == null) return false;
        WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();
        WorldPoint npcLoc = npc.getWorldLocation();
        return Math.abs(playerLoc.getX() - npcLoc.getX()) <= 1 &&
               Math.abs(playerLoc.getY() - npcLoc.getY()) <= 1;
    }

    public static WorldPoint getClosestSafeLocationNotInNPCMeleeDistance(List<LocalPoint> points, NPC npc, int distance) {
        if (points == null || npc == null) return null;
        return points.stream()
            .map(point -> WorldPoint.fromLocal(client, point))
            .filter(point -> !isNpcInMeleeDistanceToPlayer(npc) && 
                   point.distanceTo(client.getLocalPlayer().getWorldLocation()) <= distance)
            .min(Comparator.comparingInt(point -> 
                point.distanceTo(client.getLocalPlayer().getWorldLocation())
            ))
            .orElse(null);
    }

    public static WorldPoint getClosestSafeLocationInNPCMeleeDistance(List<LocalPoint> points, NPC npc) {
        if (points == null || npc == null) return null;
        return points.stream()
            .map(point -> WorldPoint.fromLocal(client, point))
            .filter(point -> isNpcInMeleeDistanceToPlayer(npc))
            .min(Comparator.comparingInt(point -> 
                point.distanceTo(client.getLocalPlayer().getWorldLocation())
            ))
            .orElse(null);
    }

    // World area utilities
    public static WorldPoint getCenterTileFromWorldArea(WorldArea area) {
        if (area == null) return null;
        return new WorldPoint(
            area.getX() + area.getWidth() / 2,
            area.getY() + area.getHeight() / 2,
            area.getPlane()
        );
    }

    // Tile utilities
    public static boolean isWalkable(WorldPoint point) {
        if (point == null) return false;
        LocalPoint local = LocalPoint.fromWorld(client.getTopLevelWorldView(), point);
        if (local == null) return false;
        
        CollisionData[] collisionData = client.getCollisionMaps();
        if (collisionData == null) return false;
        
        int[][] collisionDataFlags = collisionData[client.getPlane()].getFlags();
        return (collisionDataFlags[local.getSceneX()][local.getSceneY()] & CollisionDataFlag.BLOCK_MOVEMENT_FULL) == 0;
    }

    public static WorldPoint getRandomAdjacentTile(WorldPoint point) {
        if (point == null) return null;
        List<WorldPoint> adjacent = Arrays.asList(
            point.dx(1),
            point.dx(-1),
            point.dy(1),
            point.dy(-1)
        );
        return adjacent.get(new Random().nextInt(adjacent.size()));
    }

    public static <T> T getClosestFiltered(Collection<T> items, Predicate<T> filter) {
        if (items == null || items.isEmpty()) return null;
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        return items.stream()
            .filter(filter)
            .min((a, b) -> {
                if (a instanceof WorldPoint && b instanceof WorldPoint) {
                    return ((WorldPoint)a).distanceTo(playerLocation) - ((WorldPoint)b).distanceTo(playerLocation);
                }
                return 0;
            })
            .orElse(null);
    }

    // Item interactions
    public static void useItemOnNPC(int itemId, NPC npc) {
        if (npc != null) {
            Widget item = Inventory.search().withId(itemId).first().orElse(null);
            if (item != null) {
                NPCPackets.queueWidgetOnNPC(npc, item);
            }
        }
    }

    public static void useItemOnWallObject(Item item, TileObject object) {
        if (item != null && object != null) {
            Widget widget = client.getWidget(INVENTORY_GROUP_ID, 0);
            ObjectPackets.queueWidgetOnTileObject(widget, object);
        }
    }

    public static void useLastIdOnWallObject(int id, TileObject object) {
        if (object != null) {
            Widget widget = client.getWidget(id >> 16, id & 0xFFFF);
            ObjectPackets.queueWidgetOnTileObject(widget, object);
        }
    }

    // Widget interactions
    @SuppressWarnings("deprecation")
    public static Widget getItemWidget(Item item) {
        return item != null ? client.getWidget(INVENTORY_GROUP_ID, 0) : null;
    }

    public static void useWidgetOnNPC(Widget widget, NPC npc) {
        if (widget != null && npc != null) {
            NPCPackets.queueWidgetOnNPC(npc, widget);
        }
    }

    public static void useWidgetOnPlayer(Widget widget, Player player) {
        if (widget != null && player != null) {
            PlayerPackets.queueWidgetOnPlayer(player, widget);
        }
    }

    public static void useWidgetOnTileObject(Widget widget, TileObject object) {
        if (widget != null && object != null) {
            ObjectPackets.queueWidgetOnTileObject(widget, object);
        }
    }

    public static void useWidgetOnTileItem(Widget widget, ETileItem tileItem) {
        if (widget != null && tileItem != null) {
            TileItemPackets.queueWidgetOnTileItem(tileItem, widget, false);
        }
    }

    public static void useWidgetOnWidget(Widget widget1, Widget widget2) {
        if (widget1 != null && widget2 != null) {
            WidgetPackets.queueWidgetOnWidget(widget1, widget2);
        }
    }

    // Message dialogs
    public static void showNonModalMessageDialog(String message, String title) {
        JOptionPane pane = new JOptionPane(message);
        JDialog dialog = pane.createDialog(title);
        dialog.setModal(false);
        dialog.setVisible(true);
    }

    // Tile item interactions
    public static ETileItem nearestTileItem(Predicate<ETileItem> filter) {
        return TileItems.search().filter(filter).nearestToPlayer().orElse(null);
    }

    public static void interactWithTileItem(ETileItem tileItem, String action) {
        if (tileItem != null) {
            TileItemPackets.queueTileItemAction(tileItem, false);
        }
    }

    public static void interactWithTileItem(ETileItem tileItem, boolean ctrlDown) {
        if (tileItem != null) {
            TileItemPackets.queueTileItemAction(tileItem, ctrlDown);
        }
    }

    public static void interactWithTileItem(int id, String action) {
        ETileItem tileItem = TileItems.search().withId(id).first().orElse(null);
        if (tileItem != null) {
            TileItemPackets.queueTileItemAction(tileItem, false);
        }
    }

    public static void interactWithTileItem(String name, String action) {
        ETileItem tileItem = TileItems.search().withName(name).first().orElse(null);
        if (tileItem != null) {
            TileItemPackets.queueTileItemAction(tileItem, false);
        }
    }

    // Player state checks
    public static boolean isPlayerAnimating() {
        return client.getLocalPlayer().getAnimation() != -1;
    }

    public static boolean isPlayerIdle() {
        Player player = client.getLocalPlayer();
        return player.getAnimation() == -1 && 
               player.getPoseAnimation() == player.getIdlePoseAnimation() &&
               !isMoving();
    }

    // Location checks
    public static boolean isInArea(WorldPoint point, WorldArea area) {
        return area != null && area.contains(point);
    }

    public static boolean isPlayerInArea(WorldArea area) {
        return isInArea(client.getLocalPlayer().getWorldLocation(), area);
    }

    // Path finding
    public static List<WorldPoint> getWalkablePath(WorldPoint start, WorldPoint end) {
        if (start == null || end == null) return new ArrayList<>();
        
        List<WorldPoint> path = new ArrayList<>();
        WorldPoint current = start;
        
        while (!current.equals(end)) {
            WorldPoint next = getNextPathPoint(current, end);
            if (next == null) break;
            
            path.add(next);
            current = next;
        }
        
        return path;
    }

    private static WorldPoint getNextPathPoint(WorldPoint current, WorldPoint target) {
        List<WorldPoint> adjacent = Arrays.asList(
            current.dx(1), current.dx(-1),
            current.dy(1), current.dy(-1),
            current.dx(1).dy(1), current.dx(1).dy(-1),
            current.dx(-1).dy(1), current.dx(-1).dy(-1)
        );
        
        return adjacent.stream()
            .filter(InteractionUtils::isWalkable)
            .min(Comparator.comparingDouble(p -> 
                distanceTo2DHypotenuse(p, target)))
            .orElse(null);
    }

    // Rest of existing methods...

    // Add missing widget methods
    public static boolean isWidgetHidden(int parentId, int childId) {
        Widget widget = client.getWidget(parentId, childId);
        return widget == null || widget.isHidden();
    }

    public static int getWidgetSpriteId(int parentId, int childId) {
        Widget widget = client.getWidget(parentId, childId);
        return widget != null ? widget.getSpriteId() : -1;
    }

    public static boolean isWidgetHidden(int parentId, int childId, int grandchildId) {
        Widget widget = client.getWidget(parentId, childId);
        if (widget != null && grandchildId != -1) {
            widget = widget.getChild(grandchildId);
        }
        return widget == null || widget.isHidden();
    }

    public static List<Tile> getAll(Predicate<Tile> filter) {
        List<Tile> tiles = new ArrayList<>();
        @SuppressWarnings("deprecation")
        Scene scene = client.getScene();
        Tile[][][] sceneTiles = scene.getTiles();
        
        for (int z = 0; z < sceneTiles.length; ++z) {
            for (int x = 0; x < sceneTiles[z].length; ++x) {
                for (int y = 0; y < sceneTiles[z][x].length; ++y) {
                    Tile tile = sceneTiles[z][x][y];
                    if (tile != null && filter.test(tile)) {
                        tiles.add(tile);
                    }
                }
            }
        }
        return tiles;
    }

    // Add the missing invoke method
    public static void invoke(int var0, int var1, int var2, int var3, CoordinateArea area, String... optionalParams) {
        MousePackets.queueClickPacket();
    }

    public static String getWidgetText(int parentId, int childId) {
        Widget widget = client.getWidget(parentId, childId);
        return widget != null ? widget.getText() : "";
    }

    public static String getWidgetText(int parentId, int childId, int grandchildId) {
        Widget widget = client.getWidget(parentId, childId);
        if (widget != null && grandchildId != -1) {
            widget = widget.getChild(grandchildId);
        }
        return widget != null ? widget.getText() : "";
    }

    public static WorldPoint getClosestSafeLocationFiltered(List<LocalPoint> points, Predicate<Tile> filter) {
        if (points == null) return null;
        return points.stream()
            .map(point -> WorldPoint.fromLocal(client, point))
            .filter(point -> {
                if (point == null) return false;
                Scene scene = client.getScene();
                int plane = client.getPlane();
                LocalPoint local = LocalPoint.fromWorld(client.getTopLevelWorldView(), point);
                if (local == null) return false;
                int sceneX = local.getSceneX();
                int sceneY = local.getSceneY();
                if (sceneX < 0 || sceneY < 0 || sceneX >= 104 || sceneY >= 104) return false;
                Tile tile = scene.getTiles()[plane][sceneX][sceneY];
                return tile != null && filter.test(tile);
            })
            .min(Comparator.comparingInt(point -> 
                point.distanceTo(client.getLocalPlayer().getWorldLocation())
            ))
            .orElse(null);
    }

    public static int getWidgetSpriteId(int parentId, int childId, int grandchildId) {
        Widget widget = client.getWidget(parentId, childId);
        if (widget != null && grandchildId != -1) {
            widget = widget.getChild(grandchildId);
        }
        return widget != null ? widget.getSpriteId() : -1;
    }
}
