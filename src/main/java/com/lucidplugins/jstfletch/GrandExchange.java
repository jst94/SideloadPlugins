package com.lucidplugins.jstfletch;

import com.lucidplugins.api.utils.GameObjectUtils;
import com.lucidplugins.api.utils.InventoryUtils;
import com.lucidplugins.jstfletch.utils.VirtualKeyboard;

import net.runelite.api.GameObject;
import net.runelite.api.Item;
import net.runelite.api.TileObject;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.client.RuneLite;
import net.runelite.api.Client;

import javax.inject.Inject;
import java.util.Optional;
import java.util.List;

public class GrandExchange {

    private static final int GE_INTERFACE = 465;
    private static final int COLLECTION_BOX = 26185;
    private static final int INVENTORY_INTERFACE = 0;
    private static final int GE_OFFER_CONTAINER = WidgetInfo.GRAND_EXCHANGE_OFFER_CONTAINER.getId();

    private static final Client client = RuneLite.getInjector().getInstance(Client.class);

    @Inject
    private static JstFletchConfig config;

    public static boolean isOpen() {
        Widget geWidget = client.getWidget(GE_OFFER_CONTAINER);
        return geWidget != null && !geWidget.isHidden();
    }

    public static boolean openCollectionBox() {
        TileObject box = GameObjectUtils.nearest(COLLECTION_BOX);
        if (box != null) {
            GameObjectUtils.interact(box, "Collect");
            return true;
        }
        return false;
    }

    public static boolean collect() {
        if (!isOpen()) {
            return false;
        }
        Widget collectButton = client.getWidget(GE_INTERFACE, 6);
        return collectButton != null && !collectButton.isHidden();
    }

    public static boolean close() {
        if (!isOpen()) {
            return true;
        }
        Widget closeButton = client.getWidget(GE_INTERFACE, 2);
        if (closeButton != null) {
            Widget closeChild = closeButton.getChild(11);
            return closeChild != null && !closeChild.isHidden();
        }
        return false;
    }

    public static boolean buy(String itemName, int quantity, int price) throws InterruptedException {
        if (!isOpen()) {
            return false;
        }
        
        Widget buyButton = client.getWidget(GE_INTERFACE, 7);
        if (buyButton != null && !buyButton.isHidden()) {
            Thread.sleep(1000);
            
            VirtualKeyboard.type(itemName);
            Thread.sleep(500);
            
            Widget item = client.getWidget(GE_INTERFACE, 25);
            if (item != null && !item.isHidden()) {
                Thread.sleep(500);
                
                VirtualKeyboard.type(String.valueOf(quantity));
                Thread.sleep(500);
                
                Widget priceWidget = client.getWidget(GE_INTERFACE, 26);
                if (priceWidget != null && !priceWidget.isHidden()) {
                    Thread.sleep(500);
                    
                    VirtualKeyboard.type(String.valueOf(price));
                    Thread.sleep(500);
                    
                    Widget confirmButton = client.getWidget(GE_INTERFACE, 27);
                    return confirmButton != null && !confirmButton.isHidden();
                }
            }
        }
        return false;
    }

    public static boolean needsSupplies(int itemId) {
        return InventoryUtils.count(itemId) < 100;
    }

    public static void buyItem(int itemId, int quantity, int price) throws InterruptedException {
        if (!isOpen()) {
            open();
            Thread.sleep(1000);
        }

        String itemName = client.getItemDefinition(itemId).getName();
        if (config.useWikiPrices()) {
            price = WikiPriceUtils.getItemPrice(itemId);
        }
        buy(itemName, quantity, price);
    }

    public static boolean hasCompletedOrder() {
        if (!isOpen()) {
            return false;
        }

        Widget offerSlot = client.getWidget(GE_INTERFACE, 7);
        if (offerSlot != null) {
            Widget progressBar = offerSlot.getChild(15);
            return progressBar != null && progressBar.getWidth() == progressBar.getOriginalWidth();
        }
        return false;
    }

    public static void collectItems() throws InterruptedException {
        if (!isOpen()) {
            open();
            Thread.sleep(1000);
        }

        if (hasCompletedOrder()) {
            collect();
            Thread.sleep(500);
        }
    }

    public static void open() {
        TileObject geBooth = GameObjectUtils.nearest(10061);
        if (geBooth != null) {
            GameObjectUtils.interact(geBooth, "Exchange");
        }
    }

    public static boolean abortAllOffers() {
        if (!isOpen()) {
            return false;
        }

        Widget offerSlot = client.getWidget(GE_INTERFACE, 7);
        if (offerSlot != null) {
            Widget abortButton = offerSlot.getChild(2);
            return abortButton != null && !abortButton.isHidden();
        }
        return false;
    }
}
