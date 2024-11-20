package com.lucidplugins.api.item;

import lombok.Getter;
import net.runelite.api.Item;

@Getter
public class SlottedItem {
    private final int id;
    private final int quantity;
    private final int slot;

    public SlottedItem(int id, int quantity, int slot) {
        this.id = id;
        this.quantity = quantity;
        this.slot = slot;
    }

    public static SlottedItem fromItem(Item item, int slot) {
        return new SlottedItem(item.getId(), item.getQuantity(), slot);
    }

    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getSlot() {
        return slot;
    }
}