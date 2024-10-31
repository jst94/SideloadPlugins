package com.lucidplugins.jstfletch;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Collections.Widgets;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.WidgetInfoExtended;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import net.runelite.api.TileObject;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import java.util.Optional;

public class GrandExchange {

    private static final int GE_INTERFACE = WidgetInfo.GRAND_EXCHANGE_WINDOW_CONTAINER.getId() >> 16;
    private static final int COLLECTION_BOX = 26185;
    private static final int INVENTORY_INTERFACE = WidgetInfo.INVENTORY.getId() >> 16;
    private static final int GE_OFFER_CONTAINER = WidgetInfoExtended.GRAND_EXCHANGE_OFFER_CONTAINER.getId();

    public static boolean isOpen() {
        return Widgets.search().withParentId(GE_INTERFACE).first().isPresent();
    }

    public static boolean collect() {
        Optional<TileObject> boxOptional = TileObjects.search().withId(COLLECTION_BOX).first();
        if (boxOptional.isPresent()) {
            TileObject box = boxOptional.get();
            TileObjectInteraction.interact(box, "Collect");
            return true;
        }
        return false;
    }

    public static boolean close() {
        if (!isOpen()) {
            return true;
        }
        return Widgets.search()
            .withParentId(GE_INTERFACE)
            .withId(GE_INTERFACE << 16 | 2)
            .first()
            .map(widget -> {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetActionPacket(1, widget.getId(), -1, widget.getId());
                return true;
            })
            .orElse(false);
    }

    public static boolean buy(String itemName, int quantity, int price) throws InterruptedException {
        if (!isOpen()) {
            return false;
        }
        Optional<Widget> buyButton = Widgets.search().withParentId(GE_INTERFACE).withId(GE_INTERFACE << 16 | 7).first();
        if (buyButton.isPresent()) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(1, buyButton.get().getId(), -1, buyButton.get().getId());
            Thread.sleep(1000);
            
            // Type the item name
            com.lucidplugins.jstfletch.utils.VirtualKeyboard.type(itemName);
            Thread.sleep(500);
            
            Optional<Widget> item = Widgets.search()
                    .withParentId(GE_INTERFACE)
                    .withId((GE_INTERFACE << 16) | (25))
                    .first();
            if (item.isPresent()) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetActionPacket(1, item.get().getId(), -1, item.get().getId());
                Thread.sleep(500);
                
                // Type the quantity
                com.lucidplugins.jstfletch.utils.VirtualKeyboard.type(String.valueOf(quantity));
                Thread.sleep(500);
                
                Optional<Widget> priceWidget = Widgets.search().withParentId(GE_INTERFACE).withId(GE_INTERFACE << 16 | 26).first();
                if (priceWidget.isPresent()) {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetActionPacket(1, priceWidget.get().getId(), -1, priceWidget.get().getId());
                    Thread.sleep(500);
                    
                    // Type the price
                    com.lucidplugins.jstfletch.utils.VirtualKeyboard.type(String.valueOf(price));
                    Thread.sleep(500);
                    
                    Optional<Widget> confirmButton = Widgets.search().withParentId(GE_INTERFACE).withId(GE_INTERFACE << 16 | 27).first();
                    if (confirmButton.isPresent()) {
                        MousePackets.queueClickPacket();
                        WidgetPackets.queueWidgetActionPacket(1, confirmButton.get().getId(), -1, confirmButton.get().getId());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean sell(String itemName, int price) throws InterruptedException {
        if (!isOpen()) {
            return false;
        }
        Optional<Widget> sellButton = Widgets.search().withParentId(GE_INTERFACE).withId(GE_INTERFACE << 16 | 8).first();
        if (sellButton.isPresent()) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(1, sellButton.get().getId(), -1, sellButton.get().getId());
            Thread.sleep(1000);
            
            Optional<Widget> item = Widgets.search().withParentId(INVENTORY_INTERFACE).withTextContains(itemName).first();
            if (item.isPresent()) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetActionPacket(1, item.get().getId(), -1, item.get().getId());
                Thread.sleep(500);
                
                Optional<Widget> priceWidget = Widgets.search().withParentId(GE_INTERFACE).withId(GE_INTERFACE << 16 | 26).first();
                if (priceWidget.isPresent()) {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetActionPacket(1, priceWidget.get().getId(), -1, priceWidget.get().getId());
                    Thread.sleep(500);
                    
                    com.lucidplugins.jstfletch.utils.VirtualKeyboard.type(String.valueOf(price));
                    Thread.sleep(500);
                    
                    Optional<Widget> confirmButton = Widgets.search().withParentId(GE_INTERFACE).withId(GE_INTERFACE << 16 | 27).first();
                    if (confirmButton.isPresent()) {
                        MousePackets.queueClickPacket();
                        WidgetPackets.queueWidgetActionPacket(1, confirmButton.get().getId(), -1, confirmButton.get().getId());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean needsSupplies(int itemId) {
        return Inventory.search().withId(itemId).result().size() < 100; // Adjust this threshold as needed
    }

    public static void buyItem(int itemId, int quantity, int price) throws InterruptedException {
        if (!isOpen()) {
            collect();
            Thread.sleep(1000);
        }

        String itemName = EthanApiPlugin.getClient().getItemDefinition(itemId).getName();
        buy(itemName, quantity, price);
    }

    public static boolean hasCompletedOrder() {
        if (!isOpen()) {
            return false;
        }

        return Widgets.search()
                .withParentId(GE_INTERFACE)
                .withId(GE_INTERFACE << 16 | 6)
                .first()
                .isPresent();
    }
}
