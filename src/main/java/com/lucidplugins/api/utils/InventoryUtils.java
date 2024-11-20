package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.InteractionApi.InventoryInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.lucidplugins.api.item.SlottedItem;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class InventoryUtils
{
    static Client client = RuneLite.getInjector().getInstance(Client.class);

    public static List<SlottedItem> getAll()
    {
        List<SlottedItem> items = new ArrayList<>();
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
        if (inventory != null && !inventory.isHidden())
        {
            Item[] inventoryItems = inventory.getWidgetItems();
            for (int i = 0; i < inventoryItems.length; i++)
            {
                if (inventoryItems[i] != null)
                {
                    items.add(SlottedItem.fromItem(inventoryItems[i], i));
                }
            }
        }
        return items;
    }

    public static List<SlottedItem> getAll(Predicate<SlottedItem> filter)
    {
        return getAll().stream().filter(filter).toList();
    }

    public static List<SlottedItem> getAll(int... ids)
    {
        return getAll(item -> {
            for (int id : ids)
            {
                if (item.getId() == id) return true;
            }
            return false;
        });
    }

    public static Optional<SlottedItem> getFirst(Predicate<SlottedItem> filter)
    {
        return getAll(filter).stream().findFirst();
    }

    public static Optional<SlottedItem> getFirst(int... ids)
    {
        return getAll(ids).stream().findFirst();
    }

    public static boolean contains(int itemId)
    {
        return getFirst(itemId).isPresent();
    }

    public static boolean containsAll(int... itemIds)
    {
        for (int id : itemIds)
        {
            if (!contains(id)) return false;
        }
        return true;
    }

    public static int getCount(boolean stacks, int... ids)
    {
        int count = 0;
        for (SlottedItem item : getAll(ids))
        {
            count += stacks ? item.getQuantity() : 1;
        }
        return count;
    }

    public static int getFreeSlots()
    {
        return 28 - getAll().size();
    }

    public static void useItem(String action, int id)
    {
        Optional<SlottedItem> item = getFirst(id);
        if (item.isPresent())
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(item.get().getSlot(), id, action);
        }
    }

    public static void useItemOnItem(int id1, int id2)
    {
        Optional<SlottedItem> item1 = getFirst(id1);
        Optional<SlottedItem> item2 = getFirst(id2);
        if (item1.isPresent() && item2.isPresent())
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetOnWidget(item1.get().getSlot(), id1, item2.get().getSlot(), id2);
        }
    }

    public static void useItemOnTileObject(int itemId, TileObject object)
    {
        Optional<SlottedItem> item = getFirst(itemId);
        if (item.isPresent() && object != null)
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetOnTileObject(item.get().getSlot(), itemId, object);
        }
    }
}
