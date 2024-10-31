package com.ethan.EthanApiPlugin.Collections;

import com.ethan.EthanApiPlugin.EthanApiPlugin;
import com.example.EthanApiPlugin.Collections.query.ItemQuery;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Bank {
    private static final Client client = RuneLite.getInjector().getInstance(Client.class);
    private static final List<Widget> bankItems = new ArrayList<>();
    private static int lastUpdateTick = 0;

    public static synchronized ItemQuery search() {
        if (lastUpdateTick < client.getTickCount()) {
            Bank.bankItems.clear();
            if (client.getItemContainer(InventoryID.BANK) == null) {
                // Log or handle the null case appropriately
                return new com.example.EthanApiPlugin.Collections.query.ItemQuery(new ArrayList<>());
            }
            for (Item item : client.getItemContainer(InventoryID.BANK).getItems()) {
                try {
                    if (item == null) {
                        continue;
                    }
                    ItemComposition itemDef = EthanApiPlugin.itemDefs.get(item.getId());
                    if (itemDef.getPlaceholderTemplateId() == 14401) {
                        continue;
                    }
                    Bank.bankItems.add(new com.ethan.EthanApiPlugin.Collections.BankItemWidget(itemDef.getName(), item.getId(), item.getQuantity(), bankItems.size()));
                } catch (NullPointerException | ExecutionException ex) {
                    ex.printStackTrace(); // Replace with proper logging
                }
            }
            lastUpdateTick = client.getTickCount();
        }
        return new ItemQuery(bankItems.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public static boolean isOpen() {
        Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        return bankContainer != null && !bankContainer.isHidden();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.HOPPING ||
            gameStateChanged.getGameState() == GameState.LOGIN_SCREEN ||
            gameStateChanged.getGameState() == GameState.CONNECTION_LOST) {
            Bank.bankItems.clear();
        }
    }
}
