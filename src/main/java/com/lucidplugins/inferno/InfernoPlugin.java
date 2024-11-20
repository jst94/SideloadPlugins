package com.lucidplugins.inferno;

import com.example.InteractionApi.PrayerInteraction;
import com.google.inject.Provides;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.inject.Inject;
import com.lucidplugins.inferno.InfernoConfig;
import com.lucidplugins.inferno.InfernoInfoBoxOverlay;
import com.lucidplugins.inferno.InfernoNPC;
import com.lucidplugins.inferno.InfernoSpawnTimerInfobox;
import com.lucidplugins.inferno.KeyRemappingListener;
import com.lucidplugins.inferno.displaymodes.InfernoPrayerDisplayMode;
import com.lucidplugins.inferno.displaymodes.InfernoSafespotDisplayMode;
import com.lucidplugins.inferno.displaymodes.InfernoZukShieldDisplayMode;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.NPCManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.apache.commons.lang3.ArrayUtils;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
@PluginDescriptor(name="<html><font color=#AE9CD8>[JST]</font> Auto Inferno</html>", enabledByDefault=false, description="Inferno helper", tags={"combat", "overlay", "pve", "pvm"})
public class InfernoPlugin
extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(InfernoPlugin.class);
    private static final int INFERNO_REGION = 9043;
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private InfoBoxManager infoBoxManager;
    @Inject
    private ItemManager itemManager;
    @Inject
    private NPCManager npcManager;
    @Inject
    private InfernoInfoBoxOverlay jadOverlay;
    @Inject
    private InfernoConfig config;
    @Inject
    private KeyManager keyManager;
    @Inject
    private KeyRemappingListener keyListener;
    private InfernoConfig.FontStyle fontStyle = InfernoConfig.FontStyle.BOLD;
    private int textSize = 32;
    private WorldPoint lastLocation = new WorldPoint(0, 0, 0);
    private int currentWaveNumber;
    private final List<InfernoNPC> infernoNpcs = new ArrayList<InfernoNPC>();
    private final Map<Integer, Map<InfernoNPC.Attack, Integer>> upcomingAttacks = new HashMap<Integer, Map<InfernoNPC.Attack, Integer>>();
    private InfernoNPC.Attack closestAttack = null;
    private final List<WorldPoint> obstacles = new ArrayList<WorldPoint>();
    private boolean finalPhase = false;
    private NPC zukShield = null;
    private NPC zuk = null;
    private WorldPoint zukShieldLastPosition = null;
    private WorldPoint zukShieldBase = null;
    private int zukShieldCornerTicks = -2;
    private int zukShieldNegativeXCoord = -1;
    private int zukShieldPositiveXCoord = -1;
    private int zukShieldLastNonZeroDelta = 0;
    private int zukShieldLastDelta = 0;
    private int zukShieldTicksLeftInCorner = -1;
    private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1);
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private InfernoNPC centralNibbler = null;
    private final Map<WorldPoint, Integer> safeSpotMap = new HashMap<WorldPoint, Integer>();
    private final Map<Integer, List<WorldPoint>> safeSpotAreas = new HashMap<Integer, List<WorldPoint>>();
    private long lastTick;
    private WorldPoint lastPlayerLocation;
    private InfernoSpawnTimerInfobox spawnTimerInfoBox;
    private final List<BlobDeathLocation> blobDeathSpots = new ArrayList<>();

    @Provides
    InfernoConfig provideConfig(ConfigManager configManager) {
        return (InfernoConfig)configManager.getConfig(InfernoConfig.class);
    }

    protected void startUp() throws Exception {
        this.keyManager.registerKeyListener((KeyListener)this.keyListener);
        if (this.isInInferno()) {
            this.overlayManager.add((Overlay)this.jadOverlay);
            this.hideNpcDeaths();
        }
    }

    protected void shutDown() {
        this.overlayManager.remove((Overlay)this.jadOverlay);
        this.infoBoxManager.removeInfoBox((InfoBox)this.spawnTimerInfoBox);
        this.currentWaveNumber = -1;
        this.showNpcDeaths();
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if ("inferno".equals(event.getGroup())) {
            this.hideNpcDeaths();
            this.showNpcDeaths();
            if (!event.getKey().equals("mirrorMode") || this.isInInferno()) {
                // empty if block
            }
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!this.isInInferno()) {
            return;
        }

        this.lastTick = System.currentTimeMillis();
        this.upcomingAttacks.clear();

        for (InfernoNPC infernoNPC : this.infernoNpcs) {
            if (infernoNPC.getNpc().getAnimation() != -1) {
                infernoNPC.resetIdleTicks();
            } else {
                infernoNPC.incrementIdleTicks();
            }

            infernoNPC.gameTick(this.client, this.lastPlayerLocation, this.finalPhase);
            if (infernoNPC.getTicksTillNextAttack() <= 0 || !this.isPrayerHelper(infernoNPC) || infernoNPC.getNextAttack() == InfernoNPC.Attack.UNKNOWN && (!this.config.indicateBlobDetectionTick() || infernoNPC.getType() != InfernoNPC.Type.BLOB || infernoNPC.getTicksTillNextAttack() < 4)) continue;
            this.upcomingAttacks.computeIfAbsent(infernoNPC.getTicksTillNextAttack(), k -> new HashMap());
            if (this.config.indicateBlobDetectionTick() && infernoNPC.getType() == InfernoNPC.Type.BLOB && infernoNPC.getTicksTillNextAttack() >= 4) {
                this.upcomingAttacks.computeIfAbsent(infernoNPC.getTicksTillNextAttack() - 3, k -> new HashMap());
                this.upcomingAttacks.computeIfAbsent(infernoNPC.getTicksTillNextAttack() - 4, k -> new HashMap());
                if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey((Object)InfernoNPC.Attack.MAGIC)) {
                    if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get((Object)InfernoNPC.Attack.MAGIC) <= InfernoNPC.Type.BLOB.getPriority()) continue;
                    this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.MAGIC, InfernoNPC.Type.BLOB.getPriority());
                    continue;
                }
                if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey((Object)InfernoNPC.Attack.RANGED)) {
                    if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get((Object)InfernoNPC.Attack.RANGED) <= InfernoNPC.Type.BLOB.getPriority()) continue;
                    this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.RANGED, InfernoNPC.Type.BLOB.getPriority());
                    continue;
                }
                if (!this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).containsKey((Object)InfernoNPC.Attack.MAGIC) && !this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 4).containsKey((Object)InfernoNPC.Attack.MAGIC)) {
                    if (!this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).containsKey((Object)InfernoNPC.Attack.RANGED) && !this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 4).containsKey((Object)InfernoNPC.Attack.RANGED)) {
                        this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.MAGIC, InfernoNPC.Type.BLOB.getPriority());
                        continue;
                    }
                    if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey((Object)InfernoNPC.Attack.MAGIC) && this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get((Object)InfernoNPC.Attack.MAGIC) <= InfernoNPC.Type.BLOB.getPriority()) continue;
                    this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.MAGIC, InfernoNPC.Type.BLOB.getPriority());
                    continue;
                }
                if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey((Object)InfernoNPC.Attack.RANGED) && this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get((Object)InfernoNPC.Attack.RANGED) <= InfernoNPC.Type.BLOB.getPriority()) continue;
                this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.RANGED, InfernoNPC.Type.BLOB.getPriority());
                continue;
            }
            InfernoNPC.Attack attack = infernoNPC.getNextAttack();
            int priority = infernoNPC.getType().getPriority();
            if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).containsKey((Object)attack) && this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).get((Object)attack) <= priority) continue;
            this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).put(attack, priority);
        }
        this.closestAttack = null;
        this.calculateClosestAttack();
        this.safeSpotMap.clear();
        this.calculateSafespots();
        this.safeSpotAreas.clear();
        this.calculateSafespotAreas();
        this.obstacles.clear();
        this.calculateObstacles();
        this.centralNibbler = null;
        this.calculateCentralNibbler();
        this.calculateSpawnTimerInfobox();
        if (this.keyListener.getTogglePrayer().booleanValue()) {
            this.checkPrayer2();
        }

        // Update blob death spots
        Iterator<BlobDeathLocation> iterator = blobDeathSpots.iterator();
        while (iterator.hasNext()) {
            BlobDeathLocation spot = iterator.next();
            spot.decrementTicks();
            if (spot.getTicksUntilDone() <= 0) {
                iterator.remove();
            } else if (config.blobDeathLocationFade()) {
                spot.updateFillAlpha(config.getBlobDeathLocationDuration());
            }
        }
    }

    @Subscribe
    private void onNpcSpawned(NpcSpawned event) {
        if (this.isInInferno()) {
            int npcId = event.getNpc().getId();
            if (npcId == 7707) {
                this.zukShield = event.getNpc();
            } else {
                InfernoNPC.Type infernoNPCType = InfernoNPC.Type.typeFromId(npcId);
                if (infernoNPCType != null) {
                    switch (infernoNPCType) {
                        case BLOB: {
                            this.infernoNpcs.add(new InfernoNPC(event.getNpc()));
                            return;
                        }
                        case MAGE: {
                            if (this.zuk == null || this.spawnTimerInfoBox == null) break;
                            this.spawnTimerInfoBox.reset();
                            this.spawnTimerInfoBox.run();
                            break;
                        }
                        case ZUK: {
                            this.finalPhase = false;
                            this.zukShieldCornerTicks = -2;
                            this.zukShieldLastPosition = null;
                            this.zukShieldBase = null;
                            log.debug("[INFERNO] Zuk spawn detected, not in final phase");
                            if (!this.config.spawnTimerInfobox()) break;
                            this.zuk = event.getNpc();
                            if (this.spawnTimerInfoBox != null) {
                                this.infoBoxManager.removeInfoBox((InfoBox)this.spawnTimerInfoBox);
                            }
                            this.spawnTimerInfoBox = new InfernoSpawnTimerInfobox((BufferedImage)this.itemManager.getImage(22319), this);
                            this.infoBoxManager.addInfoBox((InfoBox)this.spawnTimerInfoBox);
                            break;
                        }
                        case HEALER_ZUK: {
                            this.finalPhase = true;
                            for (InfernoNPC infernoNPC : this.infernoNpcs) {
                                if (infernoNPC.getType() != InfernoNPC.Type.ZUK) continue;
                                infernoNPC.setTicksTillNextAttack(infernoNPC.getTicksTillNextAttack() - 3);
                            }
                            log.debug("[INFERNO] Final phase detected!");
                        }
                    }
                    this.infernoNpcs.add(0, new InfernoNPC(event.getNpc()));
                }
            }
        }
    }

    @Subscribe
    private void onNpcDespawned(NpcDespawned event) {
        if (this.isInInferno()) {
            int npcId = event.getNpc().getId();
            if (npcId == 7706) {
                this.zuk = null;
                if (this.spawnTimerInfoBox != null) {
                    this.infoBoxManager.removeInfoBox((InfoBox)this.spawnTimerInfoBox);
                }
                this.spawnTimerInfoBox = null;
            } else if (npcId == 7707) {
                this.zukShield = null;
            } else {
                InfernoNPC.Type type = InfernoNPC.Type.typeFromId(npcId);
                if (type == InfernoNPC.Type.BLOB) {
                    WorldPoint deathLocation = event.getNpc().getWorldLocation();
                    if (deathLocation != null) {
                        blobDeathSpots.add(new BlobDeathLocation(deathLocation, config.getBlobDeathLocationDuration()));
                    }
                }
                this.infernoNpcs.removeIf(infernoNPC -> infernoNPC.getNpc() == event.getNpc());
            }
        }
    }

    @Subscribe
    private void onAnimationChanged(AnimationChanged event) {
        if (this.isInInferno() && event.getActor() instanceof NPC) {
            NPC npc = (NPC)event.getActor();
            if (ArrayUtils.contains(InfernoNPC.Type.NIBBLER.getNpcIds(), npc.getId()) && npc.getAnimation() == 7576) {
                this.infernoNpcs.removeIf(infernoNPC -> infernoNPC.getNpc() == npc);
            }
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN) {
            if (!this.isInInferno()) {
                this.infernoNpcs.clear();
                this.blobDeathSpots.clear();
                this.currentWaveNumber = -1;
                this.overlayManager.remove((Overlay)this.jadOverlay);
                this.zukShield = null;
                this.zuk = null;
                if (this.spawnTimerInfoBox != null) {
                    this.infoBoxManager.removeInfoBox((InfoBox)this.spawnTimerInfoBox);
                }
                this.spawnTimerInfoBox = null;
            } else if (this.currentWaveNumber == -1) {
                this.infernoNpcs.clear();
                this.blobDeathSpots.clear();
                this.currentWaveNumber = 1;
                this.overlayManager.add((Overlay)this.jadOverlay);
            }
        }
    }

    @Subscribe
    private void onChatMessage(ChatMessage event) {
        if (this.isInInferno() && event.getType() == ChatMessageType.GAMEMESSAGE) {
            String message = event.getMessage();
            if (event.getMessage().contains("Wave:")) {
                message = message.substring(message.indexOf(": ") + 2);
                this.currentWaveNumber = Integer.parseInt(message.substring(0, message.indexOf(60)));
            }
        }
    }

    private boolean isInInferno() {
        return ArrayUtils.contains(this.client.getMapRegions(), 9043);
    }

    int getNextWaveNumber() {
        return this.currentWaveNumber != -1 && this.currentWaveNumber != 69 ? this.currentWaveNumber + 1 : -1;
    }

    private void calculateUpcomingAttacks() {
        Iterator<InfernoNPC> var1 = this.infernoNpcs.iterator();
        while (var1.hasNext()) {
            InfernoNPC infernoNPC = var1.next();
            infernoNPC.gameTick(this.client, this.lastLocation, this.finalPhase);
            if (infernoNPC.getType() == InfernoNPC.Type.ZUK && this.zukShieldCornerTicks == -1) {
                infernoNPC.updateNextAttack(InfernoNPC.Attack.UNKNOWN, 12);
                this.zukShieldCornerTicks = 0;
            }
            if (infernoNPC.getTicksTillNextAttack() <= 0 || !this.isPrayerHelper(infernoNPC) || infernoNPC.getNextAttack() == InfernoNPC.Attack.UNKNOWN && (!this.config.indicateBlobDetectionTick() || infernoNPC.getType() != InfernoNPC.Type.BLOB || infernoNPC.getTicksTillNextAttack() < 4)) continue;
            this.upcomingAttacks.computeIfAbsent(infernoNPC.getTicksTillNextAttack(), k -> new HashMap());
            if (this.config.indicateBlobDetectionTick() && infernoNPC.getType() == InfernoNPC.Type.BLOB && infernoNPC.getTicksTillNextAttack() >= 4) {
                this.upcomingAttacks.computeIfAbsent(infernoNPC.getTicksTillNextAttack() - 3, k -> new HashMap());
                this.upcomingAttacks.computeIfAbsent(infernoNPC.getTicksTillNextAttack() - 4, k -> new HashMap());
                if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey((Object)InfernoNPC.Attack.MAGIC)) {
                    if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get((Object)InfernoNPC.Attack.MAGIC) <= InfernoNPC.Type.BLOB.getPriority()) continue;
                    this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.MAGIC, InfernoNPC.Type.BLOB.getPriority());
                    continue;
                }
                if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey((Object)InfernoNPC.Attack.RANGED)) {
                    if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get((Object)InfernoNPC.Attack.RANGED) <= InfernoNPC.Type.BLOB.getPriority()) continue;
                    this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.RANGED, InfernoNPC.Type.BLOB.getPriority());
                    continue;
                }
                if (!this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).containsKey((Object)InfernoNPC.Attack.MAGIC) && !this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 4).containsKey((Object)InfernoNPC.Attack.MAGIC)) {
                    if (!this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).containsKey((Object)InfernoNPC.Attack.RANGED) && !this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 4).containsKey((Object)InfernoNPC.Attack.RANGED)) {
                        this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.MAGIC, InfernoNPC.Type.BLOB.getPriority());
                        continue;
                    }
                    if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey((Object)InfernoNPC.Attack.MAGIC) && this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get((Object)InfernoNPC.Attack.MAGIC) <= InfernoNPC.Type.BLOB.getPriority()) continue;
                    this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.MAGIC, InfernoNPC.Type.BLOB.getPriority());
                    continue;
                }
                if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey((Object)InfernoNPC.Attack.RANGED) && this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get((Object)InfernoNPC.Attack.RANGED) <= InfernoNPC.Type.BLOB.getPriority()) continue;
                this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.RANGED, InfernoNPC.Type.BLOB.getPriority());
                continue;
            }
            InfernoNPC.Attack attack = infernoNPC.getNextAttack();
            int priority = infernoNPC.getType().getPriority();
            if (this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).containsKey((Object)attack) && this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).get((Object)attack) <= priority) continue;
            this.upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).put(attack, priority);
        }
        return;
    }

    private void calculateClosestAttack() {
        if (this.config.prayerDisplayMode() == InfernoPrayerDisplayMode.PRAYER_TAB || this.config.prayerDisplayMode() == InfernoPrayerDisplayMode.BOTH) {
            int closestTick = 999;
            int closestPriority = 999;
            for (Integer tick : this.upcomingAttacks.keySet()) {
                Map<InfernoNPC.Attack, Integer> attackPriority = this.upcomingAttacks.get(tick);
                for (InfernoNPC.Attack currentAttack : attackPriority.keySet()) {
                    int currentPriority = attackPriority.get((Object)currentAttack);
                    if (tick >= closestTick && (tick != closestTick || currentPriority >= closestPriority)) continue;
                    this.closestAttack = currentAttack;
                    closestPriority = currentPriority;
                    closestTick = tick;
                }
            }
        }
    }

    private void calculateSafespots() {
        if (this.currentWaveNumber < 69) {
            if (this.config.safespotDisplayMode() != InfernoSafespotDisplayMode.OFF) {
                int checkSize = (int)Math.floor((double)this.config.safespotsCheckSize() / 2.0);
                for (int zukShieldDelta = -checkSize; zukShieldDelta <= checkSize; ++zukShieldDelta) {
                    for (int y = -checkSize; y <= checkSize; ++y) {
                        WorldPoint checkLoc = this.client.getLocalPlayer().getWorldLocation().dx(zukShieldDelta).dy(y);
                        if (this.obstacles.contains(checkLoc)) continue;
                        for (InfernoNPC infernoNPC : this.infernoNpcs) {
                            if (!this.isNormalSafespots(infernoNPC)) continue;
                            if (!this.safeSpotMap.containsKey(checkLoc)) {
                                this.safeSpotMap.put(checkLoc, 0);
                            }
                            if (!infernoNPC.canAttack(this.client, checkLoc) && !infernoNPC.canMoveToAttack(this.client, checkLoc, this.obstacles)) continue;
                            if (infernoNPC.getType().getDefaultAttack() == InfernoNPC.Attack.MELEE) {
                                if (this.safeSpotMap.get(checkLoc) == 0) {
                                    this.safeSpotMap.put(checkLoc, 1);
                                } else if (this.safeSpotMap.get(checkLoc) == 2) {
                                    this.safeSpotMap.put(checkLoc, 4);
                                } else if (this.safeSpotMap.get(checkLoc) == 3) {
                                    this.safeSpotMap.put(checkLoc, 5);
                                } else if (this.safeSpotMap.get(checkLoc) == 6) {
                                    this.safeSpotMap.put(checkLoc, 7);
                                }
                            }
                            if (infernoNPC.getType().getDefaultAttack() == InfernoNPC.Attack.MAGIC || infernoNPC.getType() == InfernoNPC.Type.BLOB && this.safeSpotMap.get(checkLoc) != 2 && this.safeSpotMap.get(checkLoc) != 4) {
                                if (this.safeSpotMap.get(checkLoc) == 0) {
                                    this.safeSpotMap.put(checkLoc, 3);
                                } else if (this.safeSpotMap.get(checkLoc) == 1) {
                                    this.safeSpotMap.put(checkLoc, 5);
                                } else if (this.safeSpotMap.get(checkLoc) == 2) {
                                    this.safeSpotMap.put(checkLoc, 6);
                                } else if (this.safeSpotMap.get(checkLoc) == 5) {
                                    this.safeSpotMap.put(checkLoc, 7);
                                }
                            }
                            if (infernoNPC.getType().getDefaultAttack() == InfernoNPC.Attack.RANGED || infernoNPC.getType() == InfernoNPC.Type.BLOB && this.safeSpotMap.get(checkLoc) != 3 && this.safeSpotMap.get(checkLoc) != 5) {
                                if (this.safeSpotMap.get(checkLoc) == 0) {
                                    this.safeSpotMap.put(checkLoc, 2);
                                } else if (this.safeSpotMap.get(checkLoc) == 1) {
                                    this.safeSpotMap.put(checkLoc, 4);
                                } else if (this.safeSpotMap.get(checkLoc) == 3) {
                                    this.safeSpotMap.put(checkLoc, 6);
                                } else if (this.safeSpotMap.get(checkLoc) == 4) {
                                    this.safeSpotMap.put(checkLoc, 7);
                                }
                            }
                            if (infernoNPC.getType() != InfernoNPC.Type.JAD || !infernoNPC.getNpc().getWorldArea().isInMeleeDistance(checkLoc)) continue;
                            if (this.safeSpotMap.get(checkLoc) == 0) {
                                this.safeSpotMap.put(checkLoc, 1);
                                continue;
                            }
                            if (this.safeSpotMap.get(checkLoc) == 2) {
                                this.safeSpotMap.put(checkLoc, 4);
                                continue;
                            }
                            if (this.safeSpotMap.get(checkLoc) == 3) {
                                this.safeSpotMap.put(checkLoc, 5);
                                continue;
                            }
                            if (this.safeSpotMap.get(checkLoc) != 6) continue;
                            this.safeSpotMap.put(checkLoc, 7);
                        }
                    }
                }
            }
        } else if (this.currentWaveNumber == 69 && this.zukShield != null) {
            WorldPoint zukShieldCurrentPosition = this.zukShield.getWorldLocation();

            this.zukShieldLastPosition = zukShieldCurrentPosition;

            if (this.config.safespotDisplayMode() == InfernoSafespotDisplayMode.OFF) {
                return;
            }

            boolean isLiveMode = finalPhase ? 
                config.safespotsZukShieldAfterHealers() == InfernoZukShieldDisplayMode.LIVE :
                config.safespotsZukShieldBeforeHealers() == InfernoZukShieldDisplayMode.LIVE;

            boolean isPredictMode = finalPhase ?
                config.safespotsZukShieldAfterHealers() == InfernoZukShieldDisplayMode.PREDICT :
                config.safespotsZukShieldBeforeHealers() == InfernoZukShieldDisplayMode.PREDICT;

            boolean isLivePlusPredictMode = finalPhase ?
                config.safespotsZukShieldAfterHealers() == InfernoZukShieldDisplayMode.LIVEPLUSPREDICT :
                config.safespotsZukShieldBeforeHealers() == InfernoZukShieldDisplayMode.LIVEPLUSPREDICT;

            WorldPoint shieldLocation = zukShield.getWorldLocation();

            if (isLiveMode || isLivePlusPredictMode) {
                drawZukSafespot(shieldLocation.getX(), shieldLocation.getY(), 0);
            }

            if (isPredictMode || isLivePlusPredictMode) {
                drawZukPredictedSafespot();
            }
        }
    }

    private void drawZukPredictedSafespot() {
        WorldPoint zukShieldCurrentPosition = this.zukShield.getWorldLocation();
        if (this.zukShieldPositiveXCoord != -1 && this.zukShieldNegativeXCoord != -1) {
            int nextShieldXCoord = zukShieldCurrentPosition.getX();
            for (InfernoNPC infernoNPC : this.infernoNpcs) {
                if (infernoNPC.getType() != InfernoNPC.Type.ZUK) continue;
                int ticksTilZukAttack = infernoNPC.getTicksTillNextAttack();
                if (ticksTilZukAttack < 1) {
                    return;
                }
                if (this.zukShieldLastNonZeroDelta > 0) {
                    if ((nextShieldXCoord += ticksTilZukAttack) <= this.zukShieldPositiveXCoord) continue;
                    if ((nextShieldXCoord -= this.zukShieldTicksLeftInCorner) <= this.zukShieldPositiveXCoord) {
                        nextShieldXCoord = this.zukShieldPositiveXCoord;
                        continue;
                    }
                    nextShieldXCoord = this.zukShieldPositiveXCoord - nextShieldXCoord + this.zukShieldPositiveXCoord;
                    continue;
                }
                if ((nextShieldXCoord -= ticksTilZukAttack) >= this.zukShieldNegativeXCoord) continue;
                if ((nextShieldXCoord += this.zukShieldTicksLeftInCorner) >= this.zukShieldNegativeXCoord) {
                    nextShieldXCoord = this.zukShieldNegativeXCoord;
                    continue;
                }
                nextShieldXCoord = this.zukShieldNegativeXCoord - nextShieldXCoord + this.zukShieldNegativeXCoord;
            }
            this.drawZukSafespot(nextShieldXCoord, this.zukShield.getWorldLocation().getY(), 2);
        }
    }

    private void checkPrayer2() {
        for (Integer tick : this.upcomingAttacks.keySet()) {
            Map<InfernoNPC.Attack, Integer> attackPriority = this.upcomingAttacks.get(tick);
            int bestPriority = 999;
            InfernoNPC.Attack bestAttack = null;
            for (Map.Entry<InfernoNPC.Attack, Integer> attackEntry : attackPriority.entrySet()) {
                if (attackEntry.getValue() >= bestPriority) continue;
                bestAttack = attackEntry.getKey();
                bestPriority = attackEntry.getValue();
            }
            for (InfernoNPC.Attack currentAttack : attackPriority.keySet()) {
                if (tick == 1 && currentAttack == bestAttack && this.client.getLocalPlayer() != null) {
                    if (this.client.getLocalPlayer().getOverheadIcon() == null) {
                        switch (currentAttack) {
                            case MAGIC: {
                                this.clickPrayMage();
                                break;
                            }
                            case RANGED: {
                                this.clickPrayRange();
                                break;
                            }
                            case MELEE: {
                                this.clickPrayMelee();
                            }
                        }
                    } else if (this.client.getLocalPlayer().getOverheadIcon().toString() != currentAttack.toString()) {
                        switch (currentAttack) {
                            case MAGIC: {
                                this.clickPrayMage();
                                break;
                            }
                            case RANGED: {
                                this.clickPrayRange();
                                break;
                            }
                            case MELEE: {
                                this.clickPrayMelee();
                            }
                        }
                    }
                }
                if (this.client.getLocalPlayer() == null || this.upcomingAttacks.containsKey(1) || this.client.getLocalPlayer().getOverheadIcon() == null) continue;
                switch (this.client.getLocalPlayer().getOverheadIcon().toString()) {
                    case "MAGIC": {
                        this.clickPrayMageOff();
                        break;
                    }
                    case "RANGED": {
                        this.clickPrayRangeOff();
                        break;
                    }
                    case "MELEE": {
                        this.clickPrayMeleeOff();
                    }
                }
            }
        }
    }

    private void clickPrayMage() {
        PrayerInteraction.togglePrayer(Prayer.PROTECT_FROM_MAGIC);
    }

    private void clickPrayRange() {
        PrayerInteraction.togglePrayer(Prayer.PROTECT_FROM_MISSILES);
    }

    private void clickPrayMelee() {
        PrayerInteraction.togglePrayer(Prayer.PROTECT_FROM_MELEE);
    }

    private void clickPrayMageOff() {
        if (client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC)) {
            PrayerInteraction.togglePrayer(Prayer.PROTECT_FROM_MAGIC);
        }
    }

    private void clickPrayRangeOff() {
        if (client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES)) {
            PrayerInteraction.togglePrayer(Prayer.PROTECT_FROM_MISSILES);
        }
    }

    private void clickPrayMeleeOff() {
        if (client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
            PrayerInteraction.togglePrayer(Prayer.PROTECT_FROM_MELEE);
        }
    }

    private void pressTab(int keyCode) {
        KeyEvent keyPressed = new KeyEvent(this.client.getCanvas(), 401, 0L, 0, keyCode, '\uffff');
        this.client.getCanvas().dispatchEvent(keyPressed);
        KeyEvent keyReleased = new KeyEvent(this.client.getCanvas(), 402, 0L, 0, keyCode, '\uffff');
        this.client.getCanvas().dispatchEvent(keyReleased);
    }

    private void drawZukSafespot(int xCoord, int yCoord, int colorSafeSpotId) {
        for (int x = xCoord - 1; x <= xCoord + 3; ++x) {
            for (int y = yCoord - 4; y <= yCoord - 2; ++y) {
                this.safeSpotMap.put(new WorldPoint(x, y, this.client.getPlane()), colorSafeSpotId);
            }
        }
    }

    private void calculateSafespotAreas() {
        if (this.config.safespotDisplayMode() == InfernoSafespotDisplayMode.AREA) {
            for (WorldPoint worldPoint : this.safeSpotMap.keySet()) {
                if (!this.safeSpotAreas.containsKey(this.safeSpotMap.get(worldPoint))) {
                    this.safeSpotAreas.put(this.safeSpotMap.get(worldPoint), new ArrayList());
                }
                this.safeSpotAreas.get(this.safeSpotMap.get(worldPoint)).add(worldPoint);
            }
        }
        this.lastLocation = this.client.getLocalPlayer().getWorldLocation();
        this.lastPlayerLocation = this.client.getLocalPlayer().getWorldLocation();
    }

    private void calculateObstacles() {
        for (NPC npc : this.client.getNpcs()) {
            this.obstacles.addAll(npc.getWorldArea().toWorldPointList());
        }
    }

    private void calculateCentralNibbler() {
        InfernoNPC bestNibbler = null;
        int bestAmountInArea = 0;
        int bestDistanceToPlayer = 999;
        Iterator<InfernoNPC> var4 = this.infernoNpcs.iterator();
        while (true) {
            if (!var4.hasNext()) {
                if (bestNibbler != null) {
                    this.centralNibbler = bestNibbler;
                }
                return;
            }
            InfernoNPC infernoNPC = var4.next();
            if (infernoNPC.getType() != InfernoNPC.Type.NIBBLER) continue;
            int amountInArea = 0;
            int distanceToPlayer = infernoNPC.getNpc().getWorldLocation().distanceTo(this.client.getLocalPlayer().getWorldLocation());
            for (InfernoNPC checkNpc : this.infernoNpcs) {
                if (checkNpc.getType() != InfernoNPC.Type.NIBBLER || checkNpc.getNpc().getWorldArea().distanceTo(infernoNPC.getNpc().getWorldArea()) > 1) continue;
                ++amountInArea;
            }
            if (amountInArea <= bestAmountInArea && (amountInArea != bestAmountInArea || distanceToPlayer >= bestDistanceToPlayer)) continue;
            bestNibbler = infernoNPC;
        }
    }

    private void calculateSpawnTimerInfobox() {
        if (this.zuk != null && !this.finalPhase && this.spawnTimerInfoBox != null) {
            boolean pauseHp = true;
            boolean resumeHp = true;
            int hp = InfernoPlugin.calculateNpcHp(this.zuk.getHealthRatio(), this.zuk.getHealthScale(), this.npcManager.getHealth(this.zuk.getId()));
            if (hp > 0) {
                if (this.spawnTimerInfoBox.isRunning()) {
                    if (hp >= 480 && hp < 600) {
                        this.spawnTimerInfoBox.pause();
                    }
                } else if (hp < 480) {
                    this.spawnTimerInfoBox.run();
                }
            }
        }
    }

    private static int calculateNpcHp(int ratio, int health, int maxHp) {
        if (ratio >= 0 && health > 0 && maxHp != -1) {
            int exactHealth = 0;
            if (ratio > 0) {
                int maxHealth;
                int minHealth = 1;
                if (health > 1) {
                    if (ratio > 1) {
                        minHealth = (maxHp * (ratio - 1) + health - 2) / (health - 1);
                    }
                    if ((maxHealth = (maxHp * ratio - 1) / (health - 1)) > maxHp) {
                        maxHealth = maxHp;
                    }
                } else {
                    maxHealth = maxHp;
                }
                exactHealth = (minHealth + maxHealth + 1) / 2;
            }
            return exactHealth;
        }
        return -1;
    }

    private boolean isPrayerHelper(InfernoNPC infernoNPC) {
        switch (infernoNPC.getType()) {
            case BLOB: {
                return this.config.prayerBlob();
            }
            case MAGE: {
                return this.config.prayerMage();
            }
            default: {
                return false;
            }
            case BAT: {
                return this.config.prayerBat();
            }
            case MELEE: {
                return this.config.prayerMeleer();
            }
            case RANGER: {
                return this.config.prayerRanger();
            }
            case HEALER_JAD: {
                return this.config.prayerHealerJad();
            }
            case JAD: 
        }
        return this.config.prayerJad();
    }

    boolean isTicksOnNpc(InfernoNPC infernoNPC) {
        switch (infernoNPC.getType()) {
            case BLOB: {
                return this.config.ticksOnNpcBlob();
            }
            case MAGE: {
                return this.config.ticksOnNpcMage();
            }
            case ZUK: {
                return this.config.ticksOnNpcZuk();
            }
            default: {
                return false;
            }
            case BAT: {
                return this.config.ticksOnNpcBat();
            }
            case MELEE: {
                return this.config.ticksOnNpcMeleer();
            }
            case RANGER: {
                return this.config.ticksOnNpcRanger();
            }
            case HEALER_JAD: {
                return this.config.ticksOnNpcHealerJad();
            }
            case JAD: 
        }
        return this.config.ticksOnNpcJad();
    }

    boolean isNormalSafespots(InfernoNPC infernoNPC) {
        switch (infernoNPC.getType()) {
            case BLOB: {
                return this.config.safespotsBlob();
            }
            case MAGE: {
                return this.config.safespotsMage();
            }
            default: {
                return false;
            }
            case BAT: {
                return this.config.safespotsBat();
            }
            case MELEE: {
                return this.config.safespotsMeleer();
            }
            case RANGER: {
                return this.config.safespotsRanger();
            }
            case HEALER_JAD: {
                return this.config.safespotsHealerJad();
            }
            case JAD: 
        }
        return this.config.safespotsJad();
    }

    boolean isIndicateNpcPosition(InfernoNPC infernoNPC) {
        switch (infernoNPC.getType()) {
            case BLOB: {
                return this.config.indicateNpcPositionBlob();
            }
            case MAGE: {
                return this.config.indicateNpcPositionMage();
            }
            default: {
                return false;
            }
            case BAT: {
                return this.config.indicateNpcPositionBat();
            }
            case MELEE: {
                return this.config.indicateNpcPositionMeleer();
            }
            case RANGER: 
        }
        return this.config.indicateNpcPositionRanger();
    }

    private void hideNpcDeaths() {
    }

    private void showNpcDeaths() {
        if (!this.config.hideNibblerDeath()) {
            // empty if block
        }
        if (!this.config.hideBatDeath()) {
            // empty if block
        }
        if (!this.config.hideBlobDeath()) {
            // empty if block
        }
        if (!this.config.hideBlobSmallMeleeDeath()) {
            // empty if block
        }
        if (!this.config.hideBlobSmallMagicDeath()) {
            // empty if block
        }
        if (!this.config.hideBlobSmallRangedDeath()) {
            // empty if block
        }
        if (!this.config.hideMeleerDeath()) {
            // empty if block
        }
        if (!this.config.hideRangerDeath()) {
            // empty if block
        }
        if (!this.config.hideMagerDeath()) {
            // empty if block
        }
        if (!this.config.hideHealerJadDeath()) {
            // empty if block
        }
        if (!this.config.hideJadDeath()) {
            // empty if block
        }
        if (!this.config.hideHealerZukDeath()) {
            // empty if block
        }
    }

    private void delay(int minDelay, int maxDelay) {
        InfernoPlugin.delay(this.getRandomIntBetweenRange(minDelay, maxDelay));
    }

    private int getRandomIntBetweenRange(int min, int max) {
        return (int)(Math.random() * (double)(max - min + 1) + (double)min);
    }

    private static void delay(int ms) {
        try {
            Thread.sleep(ms);
        }
        catch (Exception var2) {
            var2.printStackTrace();
        }
    }

    private void clickRectangle(Rectangle rectangle) {
        Point cp = this.getClickPoint(rectangle);
        if (cp.getX() >= -1) {
            this.leftClick(cp.getX(), cp.getY());
        }
    }

    private void leftClick(int x, int y) {
        MouseEvent mousePressed = new MouseEvent(this.client.getCanvas(), 501, System.currentTimeMillis(), 0, x, y, 1, false, 1);
        this.client.getCanvas().dispatchEvent(mousePressed);
        System.out.println("click" + x + y);
        MouseEvent mouseReleased = new MouseEvent(this.client.getCanvas(), 502, System.currentTimeMillis(), 0, this.client.getMouseCanvasPosition().getX(), this.client.getMouseCanvasPosition().getY(), 1, false, 1);
        this.client.getCanvas().dispatchEvent(mouseReleased);
    }

    private static double clamp(double val) {
        return Math.max(1.0, Math.min(13000.0, val));
    }

    private void moveMouse(int x, int y) {
        MouseEvent mouseMoved = new MouseEvent(this.client.getCanvas(), 503, 0L, 0, x, y, 0, false);
        this.client.getCanvas().dispatchEvent(mouseMoved);
    }

    private Point getClickPoint(Rectangle rect) {
        int rand = Math.random() <= 0.5 ? 1 : 2;
        int x = (int)(rect.getX() + (double)(rand * 3) + rect.getWidth() / 2.0);
        int y = (int)(rect.getY() + (double)(rand * 3) + rect.getHeight() / 2.0);
        return new Point(x, y);
    }

    InfernoConfig.FontStyle getFontStyle() {
        return this.fontStyle;
    }

    int getTextSize() {
        return this.textSize;
    }

    int getCurrentWaveNumber() {
        return this.currentWaveNumber;
    }

    List<InfernoNPC> getInfernoNpcs() {
        return this.infernoNpcs;
    }

    Map<Integer, Map<InfernoNPC.Attack, Integer>> getUpcomingAttacks() {
        return this.upcomingAttacks;
    }

    InfernoNPC.Attack getClosestAttack() {
        return this.closestAttack;
    }

    List<WorldPoint> getObstacles() {
        return this.obstacles;
    }

    boolean isFinalPhase() {
        return this.finalPhase;
    }

    NPC getZukShield() {
        return this.zukShield;
    }

    InfernoNPC getCentralNibbler() {
        return this.centralNibbler;
    }

    Map<WorldPoint, Integer> getSafeSpotMap() {
        return this.safeSpotMap;
    }

    Map<Integer, List<WorldPoint>> getSafeSpotAreas() {
        return this.safeSpotAreas;
    }

    long getLastTick() {
        return this.lastTick;
    }

    public List<BlobDeathLocation> getBlobDeathSpots() {
        return blobDeathSpots;
    }
}
