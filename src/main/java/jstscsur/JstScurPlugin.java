package jstscsur;

import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.*;

@PluginDescriptor(
    name = "JST Scurrius",
    description = "Automated Scurrius helper with phase detection and prayer switching",
    tags = {"combat", "overlay", "pve", "pvm"}
)
public class JstScurPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private JstScurConfig config;

    @Getter
    private JstScurScript script;

    // Constants for Scurrius mechanics
    private static final int SCURRIUS_REGION = 14484;
    private static final int FALLING_CEILING_GRAPHIC = 2644;
    private static final int SCURRIUS_NPC_ID = 12191;

    // State tracking
    private final Map<GraphicsObject, Integer> fallingCeilings = new HashMap<>();
    private final Set<Integer> foodIds = new HashSet<>();
    private final Set<Integer> prayerPotionIds = new HashSet<>();
    private WorldPoint safeSpot;
    private int phase = 1;
    private int lastPhaseChange = 0;

    @Override
    protected void startUp() {
        script = new JstScurScript(client, config, this);
        updateConfigs();
    }

    @Override
    protected void shutDown() {
        script = null;
        fallingCeilings.clear();
        foodIds.clear();
        prayerPotionIds.clear();
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (script != null && isInScurriusRegion()) {
            script.onTick();
            checkPhaseTransition();
            updateFallingCeilings();
        }
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (event.getGraphicsObject().getId() == FALLING_CEILING_GRAPHIC) {
            fallingCeilings.put(event.getGraphicsObject(), client.getTickCount());
        }
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event) {
        if (event.getNpc().getId() == SCURRIUS_NPC_ID) {
            updatePhase(event.getNpc());
        }
    }

    private boolean isInScurriusRegion() {
        if (client.getLocalPlayer() == null) {
            return false;
        }
        return client.getLocalPlayer().getWorldLocation().getRegionID() == SCURRIUS_REGION;
    }

    private void updateConfigs() {
        updateIdList(config.foodIds(), foodIds);
        updateIdList(config.prayerPotionIds(), prayerPotionIds);
        updateSafeSpot();
    }

    private void updateIdList(String configIds, Set<Integer> idList) {
        idList.clear();
        Arrays.stream(configIds.split(","))
                .map(String::trim)
                .filter(s -> s.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .forEach(idList::add);
    }

    private void updateSafeSpot() {
        try {
            String[] coords = config.safeSpotTile().split(",");
            int x = Integer.parseInt(coords[0].trim());
            int y = Integer.parseInt(coords[1].trim());
            int z = Integer.parseInt(coords[2].trim());
            safeSpot = new WorldPoint(x, y, z);
        } catch (Exception e) {
            safeSpot = null;
        }
    }

    private void checkPhaseTransition() {
        NPC scurrius = findScurrius();
        if (scurrius != null) {
            int currentPhase = calculatePhase(scurrius);
            if (currentPhase != phase) {
                phase = currentPhase;
                lastPhaseChange = client.getTickCount();
                script.onPhaseChange(phase);
            }
        }
    }

    private void updatePhase(NPC npc) {
        int newPhase = calculatePhase(npc);
        if (newPhase != phase) {
            phase = newPhase;
            lastPhaseChange = client.getTickCount();
            script.onPhaseChange(phase);
        }
    }

    private NPC findScurrius() {
        return client.getNpcs().stream()
                .filter(n -> n.getId() == SCURRIUS_NPC_ID)
                .findFirst()
                .orElse(null);
    }

    private int calculatePhase(NPC npc) {
        int healthRatio = npc.getHealthRatio();
        if (healthRatio == -1) return phase;
        
        if (healthRatio > 80) return 1;
        if (healthRatio > 30) return 2;
        return 3;
    }

    private void updateFallingCeilings() {
        // Remove ceilings after 10 ticks to be safe
        int currentTick = client.getTickCount();
        fallingCeilings.entrySet().removeIf(entry -> currentTick - entry.getValue() > 10);
    }

    @Provides
    JstScurConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(JstScurConfig.class);
    }

    public Collection<GraphicsObject> getFallingCeilings() {
        return fallingCeilings.keySet();
    }

    public Set<Integer> getFoodIds() {
        return Collections.unmodifiableSet(foodIds);
    }

    public Set<Integer> getPrayerPotionIds() {
        return Collections.unmodifiableSet(prayerPotionIds);
    }

    public WorldPoint getSafeSpot() {
        return safeSpot;
    }

    public int getPhase() {
        return phase;
    }
}
