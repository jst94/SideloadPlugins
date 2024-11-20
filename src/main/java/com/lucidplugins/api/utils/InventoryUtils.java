package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.Packets.MousePackets;
import com.example.Packets.ObjectPackets;
import com.example.Packets.WidgetPackets;
import com.lucidplugins.api.item.SlottedItem;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InventoryUtils {
    static Client client = RuneLite.getInjector().getInstance(Client.class);
    private static final int INVENTORY_GROUP_ID = 149;
    private static final int INVENTORY_CHILD_ID = 0;

    public static List<SlottedItem> getAll() {
        List<SlottedItem> items = new ArrayList<>();
        Widget inventoryWidget = client.getWidget(INVENTORY_GROUP_ID, INVENTORY_CHILD_ID);
        if (inventoryWidget != null && !inventoryWidget.isHidden()) {
            Widget[] children = inventoryWidget.getDynamicChildren();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    Widget child = children[i];
                    if (child != null && child.getItemId() != -1) {
                        items.add(SlottedItem.fromItem(new Item(child.getItemId(), child.getItemQuantity()), i));
                    }
                }
            }
        }
        return items;
    }

    public static List<SlottedItem> getAll(Predicate<SlottedItem> filter) {
        return getAll().stream().filter(filter).collect(Collectors.toList());
    }

    public static List<SlottedItem> getAll(int... ids) {
        return getAll(item -> {
            for (int id : ids) {
                if (item.getId() == id) return true;
            }
            return false;
        });
    }

    public static List<SlottedItem> getAllSlotted(Predicate<SlottedItem> filter) {
        return getAll(filter);
    }

    public static Optional<SlottedItem> getFirst(Predicate<SlottedItem> filter) {
        return getAll(filter).stream().findFirst();
    }

    public static Optional<SlottedItem> getFirst(int... ids) {
        return getAll(ids).stream().findFirst();
    }

    public static Item getFirstItem(String itemName) {
        SlottedItem slottedItem = getAll().stream()
                .filter(item -> client.getItemDefinition(item.getId()).getName().equalsIgnoreCase(itemName))
                .findFirst()
                .orElse(null);
        return slottedItem != null ? slottedItem.toItem() : null;
    }

    public static Item getFirstItem(int itemId) {
        Optional<SlottedItem> slottedItem = getFirst(itemId);
        return slottedItem.map(SlottedItem::toItem).orElse(null);
    }

    public static SlottedItem getFirstItemSlotted(String itemName) {
        return getAll().stream()
                .filter(item -> client.getItemDefinition(item.getId()).getName().equalsIgnoreCase(itemName))
                .findFirst()
                .orElse(null);
    }

    public static SlottedItem getFirstItemSlotted(int itemId) {
        return getFirst(itemId)
                .orElse(null);
    }

    public static SlottedItem getFirstItemSlotted(int... itemIds) {
        return getFirst(itemIds).orElse(null);
    }

    public static Item getFirstItem(Predicate<Item> predicate) {
        return getAll().stream()
                .filter(slottedItem -> predicate.test(slottedItem.toItem()))
                .findFirst()
                .map(SlottedItem::toItem)
                .orElse(null);
    }

    public static SlottedItem getFirstItemSlotted(Predicate<Item> predicate) {
        return getAll().stream()
                .filter(slottedItem -> predicate.test(slottedItem.toItem()))
                .findFirst()
                .orElse(null);
    }

    public static boolean contains(int itemId) {
        return getFirst(itemId).isPresent();
    }

    public static boolean contains(String itemName) {
        return getAll().stream()
                .anyMatch(item -> client.getItemDefinition(item.getId()).getName().equalsIgnoreCase(itemName));
    }

    public static boolean contains(int[] itemIds) {
        for (int id : itemIds) {
            if (contains(id)) return true;
        }
        return false;
    }

    public static boolean containsAll(int... itemIds) {
        for (int id : itemIds) {
            if (!contains(id)) return false;
        }
        return true;
    }

    public static int getCount(boolean stacks, int... ids) {
        int count = 0;
        for (SlottedItem item : getAll(ids)) {
            count += stacks ? item.getQuantity() : 1;
        }
        return count;
    }

    public static int count(int itemId) {
        return getCount(true, itemId);
    }

    public static int count(String itemName) {
        return getAll().stream()
                .filter(item -> client.getItemDefinition(item.getId()).getName().equalsIgnoreCase(itemName))
                .mapToInt(SlottedItem::getQuantity)
                .sum();
    }

    public static int getItemCount(int... itemIds) {
        return getCount(true, itemIds);
    }

    public static int getFreeSlots() {
        return 28 - getAll().size();
    }

    public static void useItem(String action, int id) {
        itemInteract(id, action);
    }

    public static void itemInteract(int id, String action) {
        Optional<SlottedItem> item = getFirst(id);
        item.ifPresent(slottedItem -> {
            MousePackets.queueClickPacket();
            Widget widget = client.getWidget(INVENTORY_GROUP_ID, INVENTORY_CHILD_ID);
            if (widget != null) {
                Widget itemWidget = widget.getChild(slottedItem.getSlot());
                if (itemWidget != null) {
                    WidgetPackets.queueWidgetAction(itemWidget, action);
                }
            }
        });
    }

    public static void wieldItem(int itemId) {
        if (itemHasAction(itemId, "Wield")) {
            itemInteract(itemId, "Wield");
        } else if (itemHasAction(itemId, "Wear")) {
            itemInteract(itemId, "Wear");
        }
    }

    public static void interactSlot(int slot, String action) {
        MousePackets.queueClickPacket();
        Widget widget = client.getWidget(INVENTORY_GROUP_ID, INVENTORY_CHILD_ID);
        if (widget != null) {
            Widget itemWidget = widget.getChild(slot);
            if (itemWidget != null) {
                WidgetPackets.queueWidgetAction(itemWidget, action);
            }
        }
    }

    public static void itemOnItem(Item item1, Item item2) {
        useItemOnItem(item1.getId(), item2.getId());
    }

    public static void useItemOnItem(int id1, int id2) {
        Optional<SlottedItem> item1 = getFirst(id1);
        Optional<SlottedItem> item2 = getFirst(id2);
        if (item1.isPresent() && item2.isPresent()) {
            MousePackets.queueClickPacket();
            Widget inventory = client.getWidget(INVENTORY_GROUP_ID, INVENTORY_CHILD_ID);
            if (inventory != null) {
                Widget sourceWidget = inventory.getChild(item1.get().getSlot());
                Widget targetWidget = inventory.getChild(item2.get().getSlot());
                if (sourceWidget != null && targetWidget != null) {
                    WidgetPackets.queueWidgetOnWidget(sourceWidget, targetWidget);
                }
            }
        }
    }

    public static void useItemOnTileObject(int itemId, TileObject object) {
        Optional<SlottedItem> item = getFirst(itemId);
        if (item.isPresent() && object != null) {
            MousePackets.queueClickPacket();
            Widget inventory = client.getWidget(INVENTORY_GROUP_ID, INVENTORY_CHILD_ID);
            if (inventory != null) {
                Widget itemWidget = inventory.getChild(item.get().getSlot());
                if (itemWidget != null) {
                    ObjectPackets.queueWidgetOnTileObject(itemWidget, object);
                }
            }
        }
    }

    public static void castAlchemyOnItem(int itemId, boolean highAlch) {
        Optional<SlottedItem> item = getFirst(itemId);
        item.ifPresent(slottedItem -> {
            MousePackets.queueClickPacket();
            Widget widget = client.getWidget(INVENTORY_GROUP_ID, INVENTORY_CHILD_ID);
            if (widget != null) {
                Widget itemWidget = widget.getChild(slottedItem.getSlot());
                if (itemWidget != null) {
                    WidgetPackets.queueWidgetAction(itemWidget, highAlch ? "Cast High Alchemy" : "Cast Low Alchemy");
                }
            }
        });
    }

    public static SlottedItem getItemInSlot(int slot) {
        Widget inventoryWidget = client.getWidget(INVENTORY_GROUP_ID, INVENTORY_CHILD_ID);
        if (inventoryWidget != null && !inventoryWidget.isHidden()) {
            Widget[] children = inventoryWidget.getDynamicChildren();
            if (children != null && slot >= 0 && slot < children.length) {
                Widget child = children[slot];
                if (child != null && child.getItemId() != -1) {
                    return SlottedItem.fromItem(new Item(child.getItemId(), child.getItemQuantity()), slot);
                }
            }
        }
        return null;
    }

    public static boolean itemHasAction(int itemId, String action) {
        ItemComposition def = client.getItemDefinition(itemId);
        String[] actions = def.getInventoryActions();
        for (String a : actions) {
            if (a != null && a.equalsIgnoreCase(action)) {
                return true;
            }
        }
        return false;
    }
}
