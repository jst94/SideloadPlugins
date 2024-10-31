package com.lucidplugins.jstfletch;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Provides;
import com.lucidplugins.api.utils.BankUtils;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

@PluginDescriptor(
        name = "JSTFLETCHER",
        description = "Automated fletching plugin",
        tags = {"fletching", "crafting", "automation"}
)
public class JstFletchplugin extends Plugin {

    private static final String VERSION = "1.0";
    private static final String CONFIG_GROUP = "jstfletch";

    @Inject
    private JstFletchConfig config;

    @Inject
    private Client client;

    private FletchingState state;
    private boolean started = false;
    private long startTime = 0L;
    private int startExperience = 0;
    private int startLevel = 0;
    private int fletchingProduct = -1;

    @Override
    protected void startUp() throws Exception {
        state = new FletchingState();
    }

    @Override
    protected void shutDown() throws Exception {
        if (state != null) {
            // Perform any necessary cleanup
        }
        started = false;
        state = null; // Reset the state by setting it to null
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!started || client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        try {
            switch (state.getCurrentState()) {
                case BANKING:
                    handleBanking();
                    break;
                case FLETCHING:
                    handleFletching();
                    break;
                case BUYING_SUPPLIES:
                    handleBuyingSupplies();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleBanking() {
        if (!BankUtils.isOpen()) {
            return;
        }

        if (Inventory.getEmptySlots() == 0) {
            BankUtils.depositAll();
            BankUtils.withdraw1(ItemID.KNIFE);
            return;
        }

        if (Inventory.getEmptySlots() == 27) {
            BankUtils.withdrawAll(config.getLogType());
            if (!Inventory.search().withId(ItemID.KNIFE).first().isPresent()) {
                BankUtils.withdraw1(ItemID.KNIFE);
            }
        }

        calculateFletchingProduct();
        state.setCurrentState(FletchingState.State.FLETCHING);
    }

    private void handleFletching() {
        if (Inventory.getEmptySlots() == 28 || !Inventory.search().withId(ItemID.KNIFE).first().isPresent()) {
            state.setCurrentState(FletchingState.State.BANKING);
            return;
        }

        if (client.getWidget(270, 1) == null) { // Check if fletching interface is not open
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetOnWidget(Inventory.search().withId(ItemID.KNIFE).first().get(), Inventory.search().withId(config.getLogType()).first().get());
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePause(fletchingProduct, 1);
        }
    }

    private void handleBuyingSupplies() throws InterruptedException {
        if (!GrandExchange.isOpen()) {
            GrandExchange.collect();
            return;
        }

        if (GrandExchange.needsSupplies(config.getLogType())) {
            GrandExchange.buyItem(config.getLogType(), config.getBuyQuantity(), config.getBuyPrice());
        }

        if (GrandExchange.hasCompletedOrder()) {
            GrandExchange.collect();
            state.setCurrentState(FletchingState.State.BANKING);
        }
    }

    private void calculateFletchingProduct() {
        int fletchingLevel = client.getRealSkillLevel(Skill.FLETCHING);
        int logType = config.getLogType();

        if (fletchingLevel >= 85) {
            fletchingProduct = 2; // Magic longbow
        } else if (fletchingLevel >= 80) {
            fletchingProduct = 1; // Magic shortbow
        } else if (fletchingLevel >= 70) {
            fletchingProduct = 2; // Yew longbow
        } else if (fletchingLevel >= 65) {
            fletchingProduct = 1; // Yew shortbow
        } else if (fletchingLevel >= 55) {
            fletchingProduct = 2; // Maple longbow
        } else if (fletchingLevel >= 50) {
            fletchingProduct = 1; // Maple shortbow
        } else if (fletchingLevel >= 40) {
            fletchingProduct = 2; // Willow longbow
        } else if (fletchingLevel >= 35) {
            fletchingProduct = 1; // Willow shortbow
        } else if (fletchingLevel >= 25) {
            fletchingProduct = 2; // Oak longbow
        } else if (fletchingLevel >= 20) {
            fletchingProduct = 1; // Oak shortbow
        } else if (fletchingLevel >= 10) {
            fletchingProduct = 2; // Longbow
        } else {
            fletchingProduct = 1; // Shortbow
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals(CONFIG_GROUP)) {
            return;
        }

        if (event.getKey().equals("startFletching")) {
            if (config.startFletching()) {
                started = true;
                startTime = System.currentTimeMillis();
                startExperience = client.getSkillExperience(Skill.FLETCHING);
                startLevel = client.getRealSkillLevel(Skill.FLETCHING);
                state = new FletchingState(); // Create new state
                state.setCurrentState(FletchingState.State.BANKING);
            }
        } else if (event.getKey().equals("stopFletching")) {
            if (config.stopFletching()) {
                started = false;
                state = null;
            }
        }
    }

    @Provides
    JstFletchConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(JstFletchConfig.class);
    }
}
