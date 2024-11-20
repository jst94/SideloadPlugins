package com.lucidplugins.api.item;

import lombok.Getter;
import net.runelite.api.Item;

@Getter
public class SlottedItem {
    private final int id;
    private final int quantity;
    private final int slot;
    private final Item item;

    public SlottedItem(int id, int quantity, int slot) {
        this.id = id;
        this.quantity = quantity;
        this.slot = slot;
        this.item = new Item(id, quantity);
    }

    public static SlottedItem fromItem(Item item, int slot) {
        return new SlottedItem(item.getId(), item.getQuantity(), slot);
    }

    public Item getItem() {
        return item;
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

    public Item toItem() {
        return new Item(getId(), getQuantity());
    }
}