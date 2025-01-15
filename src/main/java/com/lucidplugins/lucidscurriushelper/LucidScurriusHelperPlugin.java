package com.lucidplugins.lucidscurriushelper;

import com.google.inject.Provides;
import com.lucidplugins.api.utils.BankUtils;
import com.lucidplugins.api.utils.CombatUtils;
import com.lucidplugins.api.utils.GameObjectUtils;
import com.lucidplugins.api.utils.InteractionUtils;
import com.lucidplugins.api.utils.InventoryUtils;
import com.lucidplugins.api.utils.NpcUtils;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PluginDescriptor(name = "<html><font color=\"#32CD32\">Lucid </font>Scurrius Helper</html>", description = "Dodges Scurrius' falling ceiling attack and re-attacks")
public class LucidScurriusHelperPlugin extends Plugin
{

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private LucidScurriusHelperConfig config;

    @Getter
    private final Map<GraphicsObject, Integer> fallingCeilingToTicks = new HashMap<>();

    private static final int FALLING_CEILING_GRAPHIC = 2644;

    private static final int SCURRIUS = 7222;
    private static final int SCURRIUS_PUBLIC = 7221;

    private static final int DURATION = 9;
    private static final int INSTANCE_REGION = 13210;
    private static final WorldPoint INSTANCE_ENTRANCE = new WorldPoint(3192, 3427, 0);
    private static final int RAW_SHARK_ID = 383;

    private boolean justDodged = false;
    private boolean needsRestock = false;
    private boolean isRestocking = false;
    private boolean returningToInstance = false;
    private boolean needToEatShark = false;
    private boolean needToEnterManhole = false;

    private int lastDodgeTick = 0;
    private int lastRatTick = 0;
    private int lastActivateTick = 0;
    private int lastEatTick = 0;
    private int lastPrayerRestoreTick = 0;

    private List<Projectile> attacks = new ArrayList<>();
    private List<Integer> foodIds = new ArrayList<>();
    private List<Integer> prayerPotionIds = new ArrayList<>();
    private WorldPoint bankLocation;

    @Override
    protected void startUp()
    {
        updateFoodIds();
        updatePrayerPotionIds();
        updateBankLocation();
    }

    private void updateFoodIds()
    {
        foodIds.clear();
        for (String id : config.foodIds().split(","))
        {
            try
            {
                foodIds.add(Integer.parseInt(id.trim()));
            }
            catch (NumberFormatException e)
            {
                // Skip invalid numbers
            }
        }
    }

    private void updatePrayerPotionIds()
    {
        prayerPotionIds.clear();
        for (String id : config.prayerPotionIds().split(","))
        {
            try
            {
                prayerPotionIds.add(Integer.parseInt(id.trim()));
            }
            catch (NumberFormatException e)
            {
                // Skip invalid numbers
            }
        }
    }

    private void updateBankLocation()
    {
        String[] coords = config.bankLocation().split(",");
        try
        {
            int x = Integer.parseInt(coords[0].trim());
            int y = Integer.parseInt(coords[1].trim());
            int z = Integer.parseInt(coords[2].trim());
            bankLocation = new WorldPoint(x, y, z);
        }
        catch (Exception e)
        {
            // Use default if parsing fails
            bankLocation = new WorldPoint(3327, 3233, 0);
        }
    }

    private void handleAutoEat()
    {
        if (!config.autoEat() || client.getTickCount() - lastEatTick < 3)
        {
            return;
        }

        int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        if (currentHp <= config.eatAtHp())
        {
            for (String foodIdStr : config.foodIds().split(","))
            {
                try 
                {
                    int foodId = Integer.parseInt(foodIdStr.trim());
                    if (InventoryUtils.contains(foodId))
                    {
                        InventoryUtils.itemInteract(foodId, "Eat");
                        lastEatTick = client.getTickCount();
                        return;
                    }
                }
                catch (NumberFormatException e)
                {
                    // Skip invalid numbers
                }
            }
            needsRestock = true;
        }
    }

    private void handlePrayerRestore()
    {
        if (!config.autoPrayerRestore() || client.getTickCount() - lastPrayerRestoreTick < 3)
        {
            return;
        }

        int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        if (currentPrayer <= config.restorePrayerAt())
        {
            for (String potionIdStr : config.prayerPotionIds().split(","))
            {
                try
                {
                    int potionId = Integer.parseInt(potionIdStr.trim());
                    if (InventoryUtils.contains(potionId))
                    {
                        InventoryUtils.itemInteract(potionId, "Drink");
                        lastPrayerRestoreTick = client.getTickCount();
                        return;
                    }
                }
                catch (NumberFormatException e)
                {
                    // Skip invalid numbers
                }
            }
            needsRestock = true;
        }
    }

    private void handleRestock()
    {
        if (!config.autoRestock() || !needsRestock)
        {
            return;
        }

        if (!isRestocking)
        {
            isRestocking = true;
            if (InventoryUtils.contains(config.varrockTabId()))
            {
                InventoryUtils.itemInteract(config.varrockTabId(), "Break");
            }
            else
            {
                // No teleport tabs left, disable auto restock
                needsRestock = false;
                isRestocking = false;
            }
            return;
        }

        // If we're returning to the instance
        if (returningToInstance)
        {
            if (client.getLocalPlayer().getWorldLocation().distanceTo(INSTANCE_ENTRANCE) > 5)
            {
                InteractionUtils.walk(INSTANCE_ENTRANCE);
                return;
            }

            // First need to eat a raw shark
            if (!needToEatShark && !needToEnterManhole)
            {
                if (InventoryUtils.contains(RAW_SHARK_ID))
                {
                    InventoryUtils.itemInteract(RAW_SHARK_ID, "Eat");
                    needToEatShark = true;
                }
                return;
            }

            // Then enter the manhole
            if (needToEatShark && !needToEnterManhole)
            {
                TileObject manhole = GameObjectUtils.nearest("Manhole");
                if (manhole != null)
                {
                    GameObjectUtils.interact(manhole, "Climb-down");
                    needToEnterManhole = true;
                }
                return;
            }

            // Finally enter the instance portal
            if (needToEatShark && needToEnterManhole)
            {
                TileObject portal = GameObjectUtils.nearest("Instance portal");
                if (portal != null)
                {
                    GameObjectUtils.interact(portal, "Enter");
                    isRestocking = false;
                    needsRestock = false;
                    returningToInstance = false;
                    needToEatShark = false;
                    needToEnterManhole = false;
                }
            }
            return;
        }

        if (client.getLocalPlayer().getWorldLocation().distanceTo(bankLocation) > 20)
        {
            InteractionUtils.walk(bankLocation);
            return;
        }

        if (!BankUtils.isOpen())
        {
            // Find nearest bank booth or banker
            NPC banker = NpcUtils.getNearestNpc("Banker");
            TileObject bankBooth = GameObjectUtils.nearest("Bank booth");
            
            if (banker != null)
            {
                NpcUtils.interact(banker, "Bank");
            }
            else if (bankBooth != null)
            {
                GameObjectUtils.interact(bankBooth, "Bank");
            }
            return;
        }

        // Deposit everything
        BankUtils.depositAll();

        // Withdraw supplies
        for (String foodIdStr : config.foodIds().split(","))
        {
            try
            {
                int foodId = Integer.parseInt(foodIdStr.trim());
                BankUtils.withdraw1(foodId);
            }
            catch (NumberFormatException e)
            {
                // Skip invalid numbers
            }
        }

        for (String potionIdStr : config.prayerPotionIds().split(","))
        {
            try
            {
                int potionId = Integer.parseInt(potionIdStr.trim());
                BankUtils.withdraw1(potionId);
            }
            catch (NumberFormatException e)
            {
                // Skip invalid numbers
            }
        }

        // Withdraw teleport tabs and raw shark
        BankUtils.withdraw1(config.varrockTabId());
        BankUtils.withdraw1(RAW_SHARK_ID);

        BankUtils.close();
        returningToInstance = true;
    }

    @Provides
    LucidScurriusHelperConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(LucidScurriusHelperConfig.class);
    }

    @Subscribe
    private void onGraphicsObjectCreated(GraphicsObjectCreated event)
    {
        final GraphicsObject graphicsObject = event.getGraphicsObject();
        final int id = graphicsObject.getId();

        if (id == FALLING_CEILING_GRAPHIC)
        {
            fallingCeilingToTicks.put(graphicsObject, DURATION);
        }
    }

    @Subscribe
    private void onAnimationChanged(AnimationChanged event)
    {
        if (!(event.getActor() instanceof NPC))
        {
            return;
        }

        NPC npc = (NPC) event.getActor();
        if (npc.getName() != null && npc.getName().equals("Scurrius") && npc.getAnimation() == 10705 && NpcUtils.getNearestNpc("Giant rat") == null)
        {
            CombatUtils.deactivatePrayers(false);
        }
    }

    @Subscribe
    private void onProjectileMoved(ProjectileMoved event)
    {
        final Projectile projectile = event.getProjectile();
        if (projectile.getRemainingCycles() != (projectile.getEndCycle() - projectile.getStartCycle()))
        {
            return;
        }

        if (projectile.getId() != 2642 && projectile.getId() != 2640)
        {
            return;
        }

        NPC scurrius = NpcUtils.getNearestNpc("Scurrius");
        if (scurrius == null || event.getProjectile().getInteracting() != client.getLocalPlayer())
        {
            return;
        }

        if (!attacks.contains(projectile))
        {
            attacks.add(projectile);
            CombatUtils.deactivatePrayer(Prayer.PROTECT_FROM_MELEE);
        }
    }

    @Subscribe
    private void onGameTick(GameTick event)
    {
        WorldPoint instancePoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());

        if (instancePoint.getRegionID() != INSTANCE_REGION || instancePoint.getRegionX() < 23)
        {
            if (isRestocking)
            {
                handleRestock();
            }
            return;
        }

        if (isRestocking)
        {
            handleRestock();
            return;
        }

        if (config.autoPray())
        {
            handlePrayers();
        }

        handleAutoEat();
        handlePrayerRestore();

        // Clean up expired projectiles
        attacks.removeIf(proj -> proj.getRemainingCycles() < 30);

        justDodged = false;

        // Handle falling ceiling mechanics
        if (!fallingCeilingToTicks.isEmpty() && config.autoDodge())
        {
            dodgeFallingCeiling();
            fallingCeilingToTicks.replaceAll((k, v) -> v - 1);
            fallingCeilingToTicks.values().removeIf(v -> v <= 0);
        }

        if (!config.autoAttack())
        {
            return;
        }

        NPC scurrius = NpcUtils.getNearestNpc("Scurrius");
        NPC giantRat = getEligibleRat();

        // Determine attack priority
        if (!justDodged)
        {
            if (config.prioritizeRats() && giantRat != null)
            {
                if (client.getLocalPlayer().getInteracting() != giantRat)
                {
                    NpcUtils.attackNpc(giantRat);
                    lastRatTick = client.getTickCount();
                }
            }
            else if (scurrius != null && (giantRat == null || !config.prioritizeRats()))
            {
                if (client.getLocalPlayer().getInteracting() != scurrius)
                {
                    NpcUtils.attackNpc(scurrius);
                }
            }
        }

        // Handle Scurrius health state
        if (scurrius != null)
        {
            int ratio = scurrius.getHealthRatio();
            int scale = scurrius.getHealthScale();
            double targetHpPercent = (double) ratio  / (double) scale * 100;

            // If Scurrius is dead, reset attack state
            if (targetHpPercent <= 0)
            {
                attacks.clear();
                fallingCeilingToTicks.clear();
            }
        }
    }

    private void handlePrayers()
    {
        // Track active attack types
        boolean hasMagicAttack = false;
        boolean hasRangedAttack = false;
        boolean hasMeleeAttack = false;

        // Analyze all active projectiles
        for (Projectile projectile : attacks)
        {
            int cyclesToTicks = ((int)Math.floor(projectile.getRemainingCycles() / 30.0F));
            if (cyclesToTicks <= 2) // Slightly earlier activation for safety
            {
                if (projectile.getId() == 2642)
                {
                    hasRangedAttack = true;
                }
                else if (projectile.getId() == 2640)
                {
                    hasMagicAttack = true;
                }
            }
        }

        // Check for melee attacks
        NPC scurrius = NpcUtils.getNearestNpc("Scurrius");
        if (scurrius != null && 
            scurrius.getInteracting() == client.getLocalPlayer() &&
            scurrius.getAnimation() == -1 && // Not mid-attack animation
            scurrius.getPoseAnimation() == 10687) // Melee attack pose
        {
            hasMeleeAttack = true;
        }

        // Determine optimal prayer
        if (hasMagicAttack)
        {
            CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MAGIC);
        }
        else if (hasRangedAttack)
        {
            CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MISSILES);
        }
        else if (hasMeleeAttack)
        {
            CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MELEE);
        }
        else
        {
            // No active attacks - deactivate prayers if not recently activated
            if (client.getTickCount() - lastActivateTick > 2)
            {
                CombatUtils.deactivatePrayers(true);
            }
        }
    }

    @Subscribe
    private void onNpcSpawned(NpcSpawned event)
    {
        if (event.getNpc().getId() == SCURRIUS || event.getNpc().getId() == SCURRIUS_PUBLIC)
        {
            lastDodgeTick = client.getTickCount();
        }
    }

    private void dodgeFallingCeiling()
    {
        // Don't dodge if we just moved
        if (client.getTickCount() - lastDodgeTick < 2)
        {
            return;
        }

        LocalPoint playerTile = client.getLocalPlayer().getLocalLocation();
        List<LocalPoint> unsafeTiles = new ArrayList<>();

        // Collect all unsafe tiles
        for (Map.Entry<GraphicsObject, Integer> fallingCeiling : fallingCeilingToTicks.entrySet())
        {
            LocalPoint unsafeTile = fallingCeiling.getKey().getLocation();
            unsafeTiles.add(unsafeTile);
            
            // Check if player is on unsafe tile
            if (unsafeTile.getX() == playerTile.getX() && unsafeTile.getY() == playerTile.getY())
            {
                NPC scurrius = NpcUtils.getNearestNpc("Scurrius");
                if (scurrius != null)
                {
                    // Find safe tile with pathfinding
                    WorldPoint safeTile = InteractionUtils.getClosestSafeLocationInNPCMeleeDistance(unsafeTiles, scurrius);
                    
                    // If no safe tile found, move to a random adjacent tile as failsafe
                    if (safeTile == null)
                    {
                        safeTile = InteractionUtils.getRandomAdjacentTile(client.getLocalPlayer().getWorldLocation());
                    }

                    if (safeTile != null)
                    {
                        // Only move if we're not already moving to that tile
                        if (!safeTile.equals(client.getLocalPlayer().getWorldLocation()))
                        {
                            InteractionUtils.walk(safeTile);
                            justDodged = true;
                            lastDodgeTick = client.getTickCount();
                        }
                    }
                }
            }
        }
    }

    private NPC getEligibleRat()
    {
        return NpcUtils.getNearestNpc(npc -> {
            if (npc == null)
            {
                return false;
            }
            int ratio = npc.getHealthRatio();
            int scale = npc.getHealthScale();

            double targetHpPercent = (double) ratio  / (double) scale * 100;
            return npc.getName() != null && npc.getName().equals("Giant rat") && targetHpPercent > 0;
        });
    }
}
