package com.lucidplugins.jstfletch;

import com.example.EthanApiPlugin.Collections.Bank;
import com.example.EthanApiPlugin.Collections.Inventory;

import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Optional;

@Extension
@PluginDescriptor(
        name = "JST Fletcher",
        description = "Automated fletching plugin using EthanApi",
        tags = {"fletching", "crafting", "automation"}
)
public class JstFletchplugin extends Plugin {
    private static final int KNIFE_ID = 946;
    private static final int FLETCHING_WIDGET_GROUP = 270;
    private static final int FLETCHING_WIDGET_CHILD = 14;
    private static final int BANK_WIDGET_ID = 786433;

    @Inject
    private Client client;

    @Inject
    private JstFletchConfig config;

    private State currentState = State.IDLE;
    private int timeout = 0;

    private enum State {
        IDLE,
        BANKING,
        FLETCHING,
        BUYING_SUPPLIES
    }

    @Provides
    JstFletchConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(JstFletchConfig.class);
    }

    @Override
    protected void startUp() {
        reset();
    }

    @Override
    protected void shutDown() {
        reset();
    }

    private void reset() {
        currentState = State.IDLE;
        timeout = 0;
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!config.started() || client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        if (timeout > 0) {
            timeout--;
            return;
        }

        handleState();
    }

    private void handleState() {
        switch (currentState) {
            case IDLE:
                if (shouldBank()) {
                    currentState = State.BANKING;
                    return;
                }
                if (canFletch()) {
                    currentState = State.FLETCHING;
                    return;
                }
                break;

            case BANKING:
                handleBanking();
                break;

            case FLETCHING:
                handleFletching();
                break;

            case BUYING_SUPPLIES:
                // TODO: Implement GE buying logic if needed
                break;
        }
    }

    private boolean shouldBank() {
        return !hasRequiredItems() && !Bank.isOpen();
    }

    private boolean canFletch() {
        return hasRequiredItems() && !Bank.isOpen();
    }

    private boolean hasRequiredItems() {
        return Inventory.getItemAmount(KNIFE_ID) >= 1 && 
               Inventory.getItemAmount(config.logType().getItemId()) >= 1;
    }

    private void handleBanking() {
        if (!Bank.isOpen()) {
            Widget bankWidget = client.getWidget(WidgetInfo.BANK_CONTAINER);
            if (bankWidget != null) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetActionPacket(1, BANK_WIDGET_ID, -1, 0);
                timeout = 1;
            }
            return;
        }

        if (Bank.isOpen()) {
            if (Inventory.getItemAmount(config.logType().getItemId()) > 0) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(Inventory.search().withId(config.logType().getItemId()).first().get(), "Deposit-All");
                timeout = 1;
                return;
            }

            if (Inventory.getItemAmount(KNIFE_ID) == 0) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(Bank.search().withId(KNIFE_ID).first().get(), "Withdraw-1");
                timeout = 1;
                return;
            }

            if (Bank.search().withId(config.logType().getItemId()).first().isPresent()) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(Bank.search().withId(config.logType().getItemId()).first().get(), "Withdraw-All");
                timeout = 1;
                currentState = State.IDLE;
            }
        }
    }

    private void handleFletching() {
        // If fletching interface is open
        if (client.getWidget(FLETCHING_WIDGET_GROUP, FLETCHING_WIDGET_CHILD) != null) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(config.fletchType().getInterfaceIndex(), 17694734, -1, 0);
            timeout = 2;
            return;
        }

        // If we have logs, start fletching
        if (Inventory.getItemAmount(config.logType().getItemId()) > 0) {
            Optional<Widget> knife = Inventory.search().withId(KNIFE_ID).first();
            Optional<Widget> logs = Inventory.search().withId(config.logType().getItemId()).first();
            
            if (knife.isPresent() && logs.isPresent()) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetOnWidget(knife.get(), logs.get());
                timeout = 2;
                return;
            }
        }

        currentState = State.IDLE;
    }
}
