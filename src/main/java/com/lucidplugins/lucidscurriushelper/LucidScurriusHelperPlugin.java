package com.lucidplugins.lucidscurriushelper;

import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Projectile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.*;

@PluginDescriptor(
    name = "<html><font color=\"#32CD32\">Lucid </font>Scurrius Helper</html>",
    description = "Dodges Scurrius' falling ceiling attack and re-attacks",
    tags = {"scurrius", "helper", "lucid"}
)
public class LucidScurriusHelperPlugin extends Plugin {

    private static final class Constants {
        static final int FALLING_CEILING_GRAPHIC = 2644;
        static final WorldPoint DEFAULT_BANK = new WorldPoint(3327, 3233, 0);
    }

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private LucidScurriusHelperConfig config;

    @Getter
    private final Map<GraphicsObject, Integer> fallingCeilingToTicks = new HashMap<>();
    private final List<Projectile> attacks = new ArrayList<>();
    private final Set<Integer> foodIds = new HashSet<>();
    private final Set<Integer> prayerPotionIds = new HashSet<>();

    private WorldPoint bankLocation;
    private PluginState state;
    private CombatHandler combatHandler;

    @Override
    protected void startUp() {
        state = new PluginState();
        combatHandler = new CombatHandler(config, client, attacks, foodIds, state);
        updateConfigs();
    }
    
    public void checkForFallingCeiling() {
        if (client.getGraphicsObjects() == null) {
            return;
        }
        
        client.getGraphicsObjects().forEach(graphicsObject -> {
            if (graphicsObject.getId() == Constants.FALLING_CEILING_GRAPHIC) {
                fallingCeilingToTicks.putIfAbsent(graphicsObject, 0);
            }
        });
    }

    private void updateConfigs() {
        updateIdList(config.foodIds(), foodIds);
        updateIdList(config.prayerPotionIds(), prayerPotionIds);
        updateBankLocation();
    }

    private void updateIdList(String configIds, Set<Integer> idList) {
        idList.clear();
        Arrays.stream(configIds.split(","))
                .map(String::trim)
                .filter(s -> s.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .forEach(idList::add);
    }

    private void updateBankLocation() {
        try {
            String[] coords = config.bankLocation().split(",");
            int x = Integer.parseInt(coords[0].trim());
            int y = Integer.parseInt(coords[1].trim());
            int z = Integer.parseInt(coords[2].trim());
            bankLocation = new WorldPoint(x, y, z);
        } catch (Exception e) {
            bankLocation = Constants.DEFAULT_BANK;
        }
    }

    @Provides
    LucidScurriusHelperConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(LucidScurriusHelperConfig.class);
    }
}