package jstscsur;

import com.lucidplugins.api.utils.CombatUtils;
import com.lucidplugins.api.utils.InteractionUtils;
import com.lucidplugins.api.utils.InventoryUtils;
import com.lucidplugins.api.utils.NpcUtils;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.WidgetInfo;

import java.util.ArrayList;
import java.util.List;

public class JstScurScript {
    private final Client client;
    private final JstScurConfig config;
    private final JstScurPlugin plugin;

    @Getter
    private boolean initialized = false;

    private List<Projectile> activeProjectiles;
    private int lastPrayerPotionTick;
    private int lastEatTick;
    private int lastAttackTick;

    public JstScurScript(Client client, JstScurConfig config, JstScurPlugin plugin) {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        this.activeProjectiles = new ArrayList<>();
        this.lastPrayerPotionTick = 0;
        this.lastEatTick = 0;
        this.lastAttackTick = 0;
    }

    public void onTick() {
        if (client.getGameState() != GameState.LOGGED_IN) return;

        Player local = client.getLocalPlayer();
        if (local == null) return;

        NPC scurrius = NpcUtils.getNearestNpc("Scurrius");
        if (scurrius == null) return;

        handleFallingCeilings();
        handlePrayers(scurrius);
        handleEating();
        handlePrayerPotions();
        handleCombat(scurrius);
    }

    public void onPhaseChange(int newPhase) {
        // Reset combat timers on phase change
        lastAttackTick = client.getTickCount();
        
        // Phase-specific handling
        switch (newPhase) {
            case 1: // East side, melee phase
                CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MELEE);
                break;
            case 2: // Food pile phase
                moveToSafeSpot();
                break;
            case 3: // Center phase
                activeProjectiles.clear(); // Reset projectile tracking
                break;
        }
    }

    private void handleFallingCeilings() {
        Player local = client.getLocalPlayer();
        if (local == null) return;
        
        for (GraphicsObject ceiling : plugin.getFallingCeilings()) {
            LocalPoint ceilingLoc = ceiling.getLocation();
            if (ceilingLoc != null && local.getLocalLocation().distanceTo(ceilingLoc) < 100) {
                WorldPoint safe = findSafeSpot(local.getWorldLocation());
                if (safe != null) {
                    client.createMenuEntry(0)                                    
                            .setOption("Walk here")
                            .setTarget("")
                            .setType(MenuAction.WALK)
                            .setParam0(safe.getX())
                            .setParam1(safe.getY())
                            .setIdentifier(0);
                    break;
                }
            }
        }
    }

    private void handlePrayers(NPC scurrius) {
        if (!config.useQuickPrayers()) {
            // Phase-specific prayer switching
            switch (plugin.getPhase()) {
                case 1:
                    CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MELEE);
                    break;
                case 2:
                case 3:
                    // Check projectiles for prayer switching
                    if (isRangedProjectileIncoming()) {
                        CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MISSILES);
                    } else if (isMagicProjectileIncoming()) {
                        CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MAGIC);
                    }
                    break;
            }
        } else if (!CombatUtils.isQuickPrayersEnabled()) {
            CombatUtils.activateQuickPrayers();
        }
    }

    private void handleEating() {
        if (client.getTickCount() - lastEatTick < 3) return;
        
        if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.minEatHP()) {
            for (int foodId : plugin.getFoodIds()) {
                if (InventoryUtils.contains(foodId)) {
                    InventoryUtils.itemInteract(foodId, "Eat");
                    lastEatTick = client.getTickCount();
                    break;
                }
            }
        }
    }

    private void handlePrayerPotions() {
        if (client.getTickCount() - lastPrayerPotionTick < 3) return;

        if (client.getBoostedSkillLevel(Skill.PRAYER) <= config.minPrayerPoints()) {
            for (int potionId : plugin.getPrayerPotionIds()) {
                if (InventoryUtils.contains(potionId)) {
                    InventoryUtils.itemInteract(potionId, "Drink");
                    lastPrayerPotionTick = client.getTickCount();
                    break;
                }
            }
        }
    }

    private void handleCombat(NPC scurrius) {
        if (client.getTickCount() - lastAttackTick < 4) {
            return;
        }
        client.createMenuEntry(0)
                .setOption("Attack")
                .setTarget("<col=ffff00>Scurrius</col>")
                .setType(MenuAction.NPC_FIRST_OPTION)
                .setIdentifier(scurrius.getIndex())
                .setParam0(scurrius.getLocalLocation().getSceneX())
                .setParam1(scurrius.getLocalLocation().getSceneY());
    }

    private boolean isRangedProjectileIncoming() {
        return activeProjectiles.stream()
                .anyMatch(p -> p.getId() == 1995); // Ranged projectile ID
    }

    private boolean isMagicProjectileIncoming() {
        return activeProjectiles.stream()
                .anyMatch(p -> p.getId() == 1996); // Magic projectile ID
    }

    private WorldPoint findSafeSpot(WorldPoint current) {
        
        WorldPoint safeSpot = plugin.getSafeSpot();        
        if (safeSpot == null) return null;        
        
        // Try spots in different directions from current position
        WorldPoint[] potentialSpots = {
            new WorldPoint(current.getX() + 2, current.getY(), current.getPlane()),
            new WorldPoint(current.getX() - 2, current.getY(), current.getPlane()),
            new WorldPoint(current.getX(), current.getY() + 2, current.getPlane()),
            new WorldPoint(current.getX(), current.getY() - 2, current.getPlane())
        };

        // Find first spot that doesn't have falling ceiling
        for (WorldPoint spot : potentialSpots) {
            if (isSafeFromCeilings(spot)) {
                return spot;                
            }
        }

        return null;
    }

    private boolean isSafeFromCeilings(WorldPoint point) {
        LocalPoint local = LocalPoint.fromWorld(client, point);
        if (local == null) return false;

        for (GraphicsObject ceiling : plugin.getFallingCeilings()) {
            LocalPoint ceilingLoc = ceiling.getLocation();
            if (ceilingLoc != null && ceilingLoc.distanceTo(local) < 100) {
                return false;
            }
        }

        return true;
    }

    private void moveToSafeSpot() {
        WorldPoint safeSpot = plugin.getSafeSpot();
        if (safeSpot == null) return;

        WorldPoint safe = findSafeSpot(client.getLocalPlayer().getWorldLocation());        
        if(safe != null) {
            client.createMenuEntry(0)
                    .setOption("Walk here")
                    .setType(MenuAction.WALK)
                    .setParam0(safe.getX())
                    .setParam1(safe.getY())
                    .setIdentifier(0);
        }
    }
}
