package com.lucidplugins.jstfightcaves;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.EthanApiPlugin.Collections.Bank;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Collections.Widgets;
import com.example.EthanApiPlugin.Collections.query.ItemQuery;
import com.example.InteractionApi.*;
import com.example.Packets.MovementPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.lucidplugins.jstfightcaves.QLearning.Action;
import com.lucidplugins.jstfightcaves.QLearning.State;
import com.lucidplugins.api.utils.Equipment;
import com.lucidplugins.jstfightcaves.Overlays.NpcTickOverlay;
import com.lucidplugins.jstfightcaves.Overlays.TextOverlays;
import com.lucidplugins.jstfightcaves.Overlays.TickOverlay;
import com.lucidplugins.jstfightcaves.Variables.*;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@PluginDescriptor(
    name = "JST Fight Caves",
    description = "Fight Caves Helper",
    tags = {"jad", "fight", "caves", "fire", "cape", "inferno"}
)
@Slf4j
@Singleton
public class CavesPlugin extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(CavesPlugin.class);
    private static final Keybind DEFAULT_KEYBIND = new Keybind(0, 0);

    @Inject
    private Client client;

    @Inject
    private KeyManager keyManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ConfigManager configManager;

    @Inject
    private CavesConfig config;

    private CaveStates state = CaveStates.STARTING;
    private State currentState;
    private Action lastAction;
    private final Map<String, Map<Action, Double>> qTable = new HashMap<>();
    private final Random random = new Random();

    @Inject
    TextOverlays textOverlays;
    @Inject
    NpcTickOverlay npcTickOverlay;
    @Inject
    TickOverlay tickOverlay;
    @Inject
    CavesOverlay overlay;
    CaveNPCs caveNPCs;
    @Inject
    CaveNPCManager caveNPCManager;
    private Instant startTime;
    public int attackDelayTimer;
    public boolean isSteppingUnderNpc = false;
    private final int runOrbWidgetId = 10485787;
    public int brewSipsTaken = 0;
    public int eatDelay = 0;
    public int completionCount = 0;
    public boolean shouldChargeBlowpipe;
    public int currentWave = 0;
    private Prayer currentJadPrayer = null;
    private static final int DEFENSE_THRESHOLD = 80;
    public static final int TZTOK_JAD_RANGE_ATTACK = 2652;
    public static final int TZTOK_JAD_MAGIC_ATTACK = 2656;
    private NPC currentTarget;
    public int interactionCooldown = 0;
    public int dartsInBlowpipe;
    public int scalesInBlowpipe;
    private static int COOLDOWN_TICKS = 5;
    public boolean focusingOnHealers = false;
    public boolean firstBankVisit = true;
    private Map<Integer, Integer> npcAttackTimers = new HashMap<Integer, Integer>();
    private static final Pattern WAVE_PATTERN = Pattern.compile("Wave: (\\d+)");
    public Set<WorldPoint> dangerousTiles = new HashSet<>();
    private boolean started;
    public int saradominBrewDoses = 0;
    private List<Integer> saradominBrews = Arrays.asList(6691, 6689, 6687, 6685);
    private List<Integer> restorePotions = Arrays.asList(143, 141, 139, 2434, 3030, 3028, 3026, 3024);
    private List<Integer> offensivePotions = Arrays.asList(173, 171, 169, 2444);
    private List<CaveNPCs> npcs;
    private static final Map<Integer, Integer> ANIMATION_TO_ATTACK_SPEED = new HashMap<>();

    private final HotkeyListener toggle = new HotkeyListener(() -> DEFAULT_KEYBIND) {
        @Override
        public void hotkeyPressed() {
            toggle();
        }
    };

    @Override
    protected void startUp() {
        keyManager.registerKeyListener(toggle);
        overlayManager.add(overlay);
        overlayManager.add(textOverlays);
        this.startTime = Instant.now();
        this.currentWave = 0;
    }

    protected void shutDown() throws Exception {
        this.keyManager.unregisterKeyListener((KeyListener)this.toggle);
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlayManager.remove((Overlay)this.textOverlays);
        this.startTime = null;
        this.completionCount = 0;
        this.dartsInBlowpipe = -1;
        this.scalesInBlowpipe = -1;
        this.setStarted(false);
        this.resetVariables();
    }

    private void resetVariables() {
        this.isSteppingUnderNpc = false;
        this.currentWave = 0;
        this.brewSipsTaken = 0;
        this.focusingOnHealers = false;
        this.npcAttackTimers.clear();
        this.npcs = null;
    }

    public void toggle() {
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        this.setStarted(!this.started);
    }

    public void setStarted(boolean started) {
        this.started = started;
        if (started) {
            this.startTime = Instant.now();
        }
    }

    @Provides
    CavesConfig provideConfig(ConfigManager configManager) {
        return (CavesConfig)configManager.getConfig(CavesConfig.class);
    }

    @Subscribe
    private void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();
        CaveNPCs caveNPC = this.caveNPCManager.getNpcByNameContains(npc.getName());
        if (caveNPC != null) {
            this.npcAttackTimers.put(npc.getIndex(), 0);
            log.info("NPC Spawned: {} (Index: {}, Attack Timer: {})", npc.getName(), npc.getIndex(), 0);
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGIN_SCREEN) {
            this.dartsInBlowpipe = -1;
            this.scalesInBlowpipe = -1;
            this.resetVariables();
        }
        if ((event.getGameState() == GameState.LOGGED_IN || event.getGameState() == GameState.LOADING) && this.dartsInBlowpipe == -1 && this.scalesInBlowpipe == -1) {
            this.checkBlowpipeCharges();
        }
    }

    @Subscribe
    private void onNpcDespawned(NpcDespawned event) {
        this.npcAttackTimers.remove(event.getNpc().getIndex());
    }

    private boolean isRanging() {
        int currentEquippedWeaponTypeVarbit = this.client.getVarbitValue(357);
        WeaponType weaponType = WeaponType.getWeaponType(currentEquippedWeaponTypeVarbit);
        return weaponType == WeaponType.TYPE_3 || weaponType == WeaponType.TYPE_5 || weaponType == WeaponType.TYPE_7 || weaponType == WeaponType.TYPE_19;
    }

    @Subscribe
    private void onAnimationChanged(AnimationChanged event) {
        NPC npc;
        CaveNPCs caveNPC;
        Player player;
        if (event.getActor() instanceof Player && (player = (Player)event.getActor()).equals(this.client.getLocalPlayer())) {
            int animationId = player.getAnimation();
            Integer weaponSpeed = ANIMATION_TO_ATTACK_SPEED.get(animationId);
            if (animationId == 5061) {
                this.attackDelayTimer = 4;
            }
            if (weaponSpeed != null) {
                this.attackDelayTimer = weaponSpeed;
                log.info("Set attackDelayTimer to {} based on animation ID {}", (Object)weaponSpeed, (Object)animationId);
            }
        }
        if (event.getActor() instanceof NPC && (caveNPC = this.findCaveNpc((npc = (NPC)event.getActor()).getName())) != null && npc.getAnimation() == caveNPC.getAttackAnimationId()) {
            this.npcAttackTimers.put(npc.getIndex(), caveNPC.getAttackSpeed());
        }
        if (event.getActor().getAnimation() == 2656) {
            this.currentJadPrayer = Prayer.PROTECT_FROM_MAGIC;
        } else if (event.getActor().getAnimation() == 2652) {
            this.currentJadPrayer = Prayer.PROTECT_FROM_MISSILES;
        }
    }

    @Subscribe
    private void onChatMessage(ChatMessage event) {
        if (event.getType() == ChatMessageType.GAMEMESSAGE) {
            String message = event.getMessage();
            Matcher matcher = WAVE_PATTERN.matcher(message);
            if (matcher.find()) {
                try {
                    this.currentWave = Integer.parseInt(matcher.group(1));
                    log.info("Current wave: {}", (Object)this.currentWave);
                }
                catch (NumberFormatException e) {
                    log.warn("Failed to parse wave number from message: {}", (Object)message);
                }
            }
            if (event.getMessage().contains("Your TzTok-Jad kill count is")) {
                ++this.completionCount;
                if (this.config.useBlowpipe()) {
                    this.checkBlowpipeCharges();
                }
            }
            this.handleBlowpipeChatMessage(event.getMessage());
        }
    }

    private void handleBlowpipeChatMessage(String message) {
        if (message.contains("Your blowpipe has ")) {
            String[] parts = message.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("darts")) {
                    try {
                        this.dartsInBlowpipe = Integer.parseInt(parts[i-1].replaceAll(",", ""));
                        log.debug("Updated darts in blowpipe: {}", this.dartsInBlowpipe);
                    } catch (NumberFormatException e) {
                        log.error("Failed to parse darts count: " + parts[i-1]);
                    }
                } else if (parts[i].equals("scales")) {
                    try {
                        this.scalesInBlowpipe = Integer.parseInt(parts[i-1].replaceAll(",", ""));
                        log.debug("Updated scales in blowpipe: {}", this.scalesInBlowpipe);
                    } catch (NumberFormatException e) {
                        log.error("Failed to parse scales count: " + parts[i-1]);
                    }
                }
            }
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!this.started || this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }

        int animationId = player.getAnimation();
        log.debug("Attack Delay Timer: {}", this.attackDelayTimer);

        if (this.interactionCooldown > 0) {
            --this.interactionCooldown;
        }
        if (this.attackDelayTimer > 0) {
            --this.attackDelayTimer;
        }
        this.assignInteractionCooldown();

        // Handle blowpipe checks
        if (this.config.useBlowpipe() && !this.fightStarted()) {
            boolean hasEnough = this.hasEnoughBlowPipeCharges();
            log.debug("hasEnoughBlowPipeCharges: {}, darts: {}, scales: {}", 
                hasEnough, this.dartsInBlowpipe, this.scalesInBlowpipe);
            if (!hasEnough) {
                this.checkBlowpipeCharges();
            }
        }

        this.updateDangerousTiles();
        
        // Handle combat using Q-learning if in combat
        if (this.fightStarted() && !this.isSteppingUnderNpc) {
            handleCombatLogic();
        } else {
            // Reset Q-learning state when not in combat
            currentState = null;
            lastAction = null;
        }

        // Handle other game logic
        this.handleCombatState();
        if (!this.fightStarted()) {
            this.resetVariables();
            this.disablePrayer();
        } else if (this.fightStarted()) {
            this.state = CaveStates.CLEARING_CAVE;
            this.firstBankVisit = true;
        }

        // Handle UI interactions
        if (!Widgets.search().withTextContains("Enter amount:").empty()) {
            this.client.runScript(new Object[]{299, 1, 1, 0});
        }
        if (Widgets.search().withTextContains("You're on your own now").hiddenState(false).first().isPresent()) {
            this.resumePause();
        }

        // Handle timers and other mechanics
        if (this.eatDelay > 0) {
            --this.eatDelay;
        }
        this.handleRunEnergy();

        // Update NPC attack timers
        Iterator<Map.Entry<Integer, Integer>> iterator = this.npcAttackTimers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int npcIndex = entry.getKey();
            NPC npc = this.client.getCachedNPCs()[npcIndex];
            if (npc == null || npc.getHealthRatio() == 0) {
                iterator.remove();
                continue;
            }
            int ticksUntilAttack = entry.getValue();
            if (ticksUntilAttack > 0) {
                this.npcAttackTimers.put(npcIndex, ticksUntilAttack - 1);
                continue;
            }
            CaveNPCs caveNpc = this.findCaveNpc(npc.getName());
            if (caveNpc == null || npc.getAnimation() != caveNpc.getAttackAnimationId()) continue;
            this.npcAttackTimers.put(npcIndex, caveNpc.getAttackSpeed() - 1);
        }
    }

    private void handleCombatLogic() {
        Player player = client.getLocalPlayer();
        if (player == null) return;

        // Get nearest enemy and count nearby enemies
        NPC nearestEnemy = NPCs.search()
            .filter(npc -> npc.getHealthRatio() > 0 && 
                    !npc.getName().equals("TzTok-Jad"))
            .nearestToPlayer().orElse(null);
            
        int nearbyEnemyCount = (int) NPCs.search()
            .filter(npc -> npc.getHealthRatio() > 0 && 
                    npc.getWorldLocation().distanceTo(player.getWorldLocation()) <= 5)
            .result().size();

        // Create new state
        State newState = new State(
            player,
            nearestEnemy,
            isUnderAttack(),
            nearbyEnemyCount,
            isInSafeSpot(player.getWorldLocation()),
            isMoving()
        );

        // If we have a previous state, calculate reward and update Q-table
        if (currentState != null && lastAction != null) {
            double reward = calculateReward(currentState, newState, lastAction);
            updateQValues(currentState, lastAction, newState, reward);
        }

        // Choose and execute next action
        Action action = chooseAction(newState);
        executeAction(action, nearestEnemy);

        // Update state
        currentState = newState;
        lastAction = action;
    }

    private void executeAction(Action action, NPC nearestEnemy) {
        if (isMoving()) return; // Don't execute new actions while moving

        switch (action) {
            case ATTACK_NEAREST:
                if (nearestEnemy != null) {
                    handleInteraction(nearestEnemy);
                }
                break;
            case MOVE_TO_SAFESPOT:
                if (nearestEnemy != null) {
                    WorldPoint safeSpot = findSafeSpot(client.getLocalPlayer().getWorldLocation(), 
                        nearestEnemy.getWorldLocation());
                    if (safeSpot != null) {
                        handleMovement(safeSpot);
                    }
                }
                break;
            case HEAL:
                handleAutoEat();
                break;
            case PRAYER_FLICK:
                handlePrayer();
                break;
            case KITE:
                if (nearestEnemy != null) {
                    kitePrayerDrainer();
                }
                break;
            case STEP_UNDER:
                if (nearestEnemy != null) {
                    stepUnderNpc(nearestEnemy);
                }
                break;
            case WAIT:
                // Do nothing
                break;
        }
    }

    private double getQValue(String stateKey, Action action) {
        return qTable
            .computeIfAbsent(stateKey, k -> new HashMap<>())
            .getOrDefault(action, 0.0);
    }

    private void updateQValue(String stateKey, Action action, double reward, State nextState) {
        double learningRate = 0.1;
        double discountFactor = 0.9;

        // Get current Q value
        double currentQ = getQValue(stateKey, action);

        // Find max Q value for next state
        double maxNextQ = Arrays.stream(Action.getAllActions())
            .mapToDouble(a -> getQValue(nextState.getKey(), a))
            .max()
            .orElse(0.0);

        // Q-learning update formula
        double newQ = currentQ + learningRate * (reward + discountFactor * maxNextQ - currentQ);

        // Update Q-table
        qTable.computeIfAbsent(stateKey, k -> new HashMap<>())
            .put(action, newQ);
    }

    private Action chooseAction(State state) {
        double epsilon = 0.1; // Exploration rate

        if (random.nextDouble() < epsilon) {
            // Explore: choose random action
            Action[] actions = Action.getAllActions();
            return actions[random.nextInt(actions.length)];
        } else {
            // Exploit: choose best action
            return Arrays.stream(Action.getAllActions())
                .max(Comparator.comparingDouble(a -> getQValue(state.getKey(), a)))
                .orElse(Action.WAIT);
        }
    }

    private double calculateReward(State state, Action action) {
        double reward = 0;

        // Base rewards
        if (state.isUnderAttack() && action == Action.MOVE_TO_SAFESPOT) {
            reward += 1;
        }
        if (state.getPlayerHealth() < 50 && action == Action.HEAL) {
            reward += 2;
        }
        if (state.isInSafeSpot() && action == Action.ATTACK_NEAREST) {
            reward += 1.5;
        }

        // Penalties
        if (state.getPlayerHealth() < 30 && action != Action.HEAL) {
            reward -= 1;
        }
        if (state.isUnderAttack() && !state.isInSafeSpot() && action != Action.MOVE_TO_SAFESPOT) {
            reward -= 0.5;
        }

        return reward;
    }

    private boolean isUnderAttack() {
        return !npcAttackTimers.isEmpty() && 
               npcAttackTimers.values().stream().anyMatch(timer -> timer <= 1);
    }

    private boolean isInSafeSpot(WorldPoint location) {
        // Implement safe spot detection logic
        return false;
    }

    private WorldPoint findSafeSpot(WorldPoint playerLocation, WorldPoint tzKihLocation) {
        List<WorldPoint> reachableTiles = EthanApiPlugin.reachableTiles();
        List<WorldPoint> potentialSpots = reachableTiles.stream()
            .filter(tile -> tile.distanceTo(playerLocation) == 5)
            .filter(tile -> tile.distanceTo(tzKihLocation) >= 4)
            .collect(Collectors.toList());

        return potentialSpots.stream()
            .min(Comparator.comparingInt(tile -> tile.distanceTo(playerLocation)))
            .orElse(null);
    }

    private void handleState() {
        if (this.config.useBlowpipe()) {
            if (this.hasAllItems() && !this.fightStarted() && this.hasEnoughBlowPipeCharges()) {
                this.state = CaveStates.ENTERING_CAVE;
                return;
            }
            if (!Bank.isOpen() && !this.fightStarted() && !this.hasAllItems() && this.hasEnoughBlowPipeCharges()) {
                this.state = CaveStates.OPENING_BANK;
                return;
            }
            if (Bank.isOpen() && !this.fightStarted() && !this.hasAllItems() && this.hasEnoughBlowPipeCharges()) {
                this.state = CaveStates.RESUPPLYING;
                return;
            }
            if (Bank.isOpen() && !this.fightStarted() && this.hasAllItems() && this.hasEnoughBlowPipeCharges()) {
                this.state = CaveStates.CLOSING_BANK;
                return;
            }
            if (!this.fightStarted() && !this.hasEnoughBlowPipeCharges()) {
                this.state = CaveStates.RECHARGING_BLOWPIPE;
                return;
            }
        } else {
            if (this.hasAllItems() && !this.fightStarted()) {
                this.state = CaveStates.ENTERING_CAVE;
                return;
            }
            if (!(Bank.isOpen() || this.fightStarted() || this.hasAllItems())) {
                this.state = CaveStates.OPENING_BANK;
                return;
            }
            if (Bank.isOpen() && !this.fightStarted() && !this.hasAllItems()) {
                this.state = CaveStates.RESUPPLYING;
                return;
            }
            if (Bank.isOpen() && !this.fightStarted() && this.hasAllItems()) {
                this.state = CaveStates.CLOSING_BANK;
                return;
            }
        }
    }

    private void handleCombatState() {
        if (this.config.useBlowpipe()) {
            if (this.hasAllItems() && !this.fightStarted() && this.hasEnoughBlowPipeCharges()) {
                // Combat state handling logic
            }
        }
    }

    private void handleBanking() {
        if (Bank.isOpen() || isMoving()) {
            return;
        }

        if (!hasAllItems()) {
            if (!Bank.isOpen()) {
                TileObjects.search().withName("Bank booth").first().ifPresent(bank -> 
                    TileObjectInteraction.interact(bank, "Bank"));
                return;
            }

            Widget depositButton = client.getWidget(12, 42);
            if (depositButton != null) {
                handleWidgetInteraction(depositButton, "Deposit inventory");
            }
        }
    }

    private void openBank() {
        if (Bank.isOpen() || isMoving()) {
            return;
        }
        TileObjects.search().withId(30267).atLocation(2445, 5181, 0).first().ifPresent(x -> {
            TileObjectInteraction.interact(x, "Use");
        });
    }

    private void closeBank() {
        if (!Bank.isOpen()) {
            return;
        }
        Optional<Widget> closeButton = Widgets.search().withId(786434).first();
        if (closeButton.isPresent()) {
            handleWidgetInteraction(closeButton.get(), "Close");
        }
    }

    private void attackHighestPriorityNpc() {
        Player localPlayer = this.client.getLocalPlayer();
        if (localPlayer == null) {
            return;
        }
        List<String> npcPriorityOrder = Arrays.asList("Tz-Kih", "Yt-HurKot", "TzTok-Jad", "Yt-MejKot", "Tok-Xil", "Ket-Zek");
        List<NPC> attackableNpcs = NPCs.search().alive().result().stream().filter(npc -> this.npcAttackTimers.containsKey(npc.getIndex()) || npcPriorityOrder.contains(npc.getName())).sorted((a, b) -> {
            int priorityA = npcPriorityOrder.indexOf(a.getName());
            int priorityB = npcPriorityOrder.indexOf(b.getName());
            if (priorityA == -1) {
                return 1;
            }
            if (priorityB == -1) {
                return -1;
            }
            return Integer.compare(priorityA, priorityB);
        }).sorted(Comparator.comparingInt(npc -> localPlayer.getWorldLocation().distanceTo(npc.getWorldLocation()))).collect(Collectors.toList());
        if (!attackableNpcs.isEmpty()) {
            NPC highestPriorityNpc = (NPC)attackableNpcs.get(0);
            if (!(this.isSteppingUnderNpc || this.currentTarget != null && this.currentTarget.equals(highestPriorityNpc) && this.interactionCooldown != 0)) {
                this.currentTarget = highestPriorityNpc;
                log.info("Attacking: " + highestPriorityNpc.getName());
                if (!this.client.getLocalPlayer().isInteracting() || this.client.getLocalPlayer().getInteracting() == null || !this.client.getLocalPlayer().getInteracting().equals(highestPriorityNpc)) {
                    this.handleInteraction(highestPriorityNpc);
                }
            }
        }
    }

    private void handleInteraction(NPC npc) {
        if (npc == null || client.getLocalPlayer() == null) {
            return;
        }
        
        if (isMoving()) {
            return;
        }

        NPCInteraction.interact(npc, "Attack");
        interactionCooldown = COOLDOWN_TICKS;
    }

    private void handleMovement(WorldPoint target) {
        if (target == null || client.getLocalPlayer() == null) {
            return;
        }

        if (isMoving()) {
            return;
        }

        MovementPackets.queueMovement(target);
    }

    private void handleWidgetInteraction(Widget widget, String action) {
        if (widget == null || client.getLocalPlayer() == null) {
            return;
        }

        if (isMoving()) {
            return;
        }

        WidgetPackets.queueWidgetAction(widget, action);
        interactionCooldown = COOLDOWN_TICKS;
    }

    private void resumePause() {
        Widget widget = client.getWidget(15138821);
        if (widget != null) {
            handleWidgetInteraction(widget, "Resume");
        }
    }

    private void useWidgetOnWidget(Widget source, Widget target) {
        if (source != null && target != null) {
            WidgetPackets.queueWidgetOnWidget(source, target);
        }
    }

    private boolean isMoving() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return false;
        }
        return player.getPoseAnimation() != player.getIdlePoseAnimation();
    }

    private boolean isLocalPlayerMoving() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return false;
        }
        return player.getPoseAnimation() != player.getIdlePoseAnimation();
    }

    private boolean nameContainsInsensitive(ItemQuery query, String name) {
        return query.filter(item -> item.getName().toLowerCase().contains(name.toLowerCase())).first().isPresent();
    }

    private boolean jadFightActive() {
        return NPCs.search().withName("TzTok-Jad").alive().first().isPresent();
    }

    private void handlePrayer() {
        CavesPrayerStyle prayerMode = this.config.prayerStyle();
        switch (prayerMode) {
            case NORMAL: {
                this.handleRegularPrayer();
                break;
            }
            case ONE_TICK_FLICK: {
                this.handleOneTickFlickPrayer();
                break;
            }
            case LAZY_FLICK: {
                this.handleLazyFlickPrayer();
                break;
            }
            case REALISTIC_FLICK: {
                this.handleRealisticFlickPrayer();
            }
        }
    }

    private void handleRegularPrayer() {
        if (this.jadFightActive()) {
            if (this.currentJadPrayer != null) {
                PrayerInteraction.setPrayerState(this.currentJadPrayer, true);
            }
            if (this.config.offensivePrayer() != null) {
                PrayerInteraction.setPrayerState(this.config.offensivePrayer().prayer, true);
            }
            return;
        }
        if (!this.fightStarted()) {
            this.disablePrayer();
            return;
        }
        Player localPlayer = this.client.getLocalPlayer();
        if (localPlayer == null) {
            return;
        }
        WorldPoint playerLocation = localPlayer.getWorldLocation();
        ArrayList<CaveNPCs> attackingNpcs = new ArrayList<CaveNPCs>();
        boolean anyNpcsAlive = false;
        for (Map.Entry<Integer, Integer> entry : this.npcAttackTimers.entrySet()) {
            WorldArea npcArea;
            NPC attackingNpc = this.client.getCachedNPCs()[entry.getKey()];
            if (attackingNpc == null || attackingNpc.isDead()) continue;
            anyNpcsAlive = true;
            CaveNPCs caveNpc = this.caveNPCManager.getNpcByNameContains(attackingNpc.getName());
            if (caveNpc == null || entry.getValue() != 0 || !this.isWithinAttackRange(npcArea = attackingNpc.getWorldArea(), playerLocation, caveNpc)) continue;
            attackingNpcs.add(caveNpc);
        }
        if (anyNpcsAlive) {
            if (!attackingNpcs.isEmpty()) {
                attackingNpcs.sort(Comparator.comparingInt(CaveNPCs::getAttackPriority));
                CaveNPCs highestPriorityNpc = (CaveNPCs)attackingNpcs.get(0);
                if (highestPriorityNpc.getPrayer() != null) {
                    PrayerInteraction.setPrayerState(highestPriorityNpc.getPrayer(), true);
                }
            }
            if (this.config.offensivePrayer() != null) {
                PrayerInteraction.setPrayerState(this.config.offensivePrayer().prayer, true);
            }
        } else {
            this.disablePrayer();
        }
    }

    private void handleOneTickFlickPrayer() {
        if (this.jadFightActive()) {
            if (this.currentJadPrayer != null) {
                PrayerInteraction.flickPrayers(this.currentJadPrayer, this.config.offensivePrayer().prayer);
            }
            return;
        }
        if (!this.fightStarted()) {
            this.disablePrayer();
            return;
        }
        Player localPlayer = this.client.getLocalPlayer();
        if (localPlayer == null) {
            return;
        }
        WorldPoint playerLocation = localPlayer.getWorldLocation();
        ArrayList<CaveNPCs> attackingNpcs = new ArrayList<CaveNPCs>();
        for (Map.Entry<Integer, Integer> entry : this.npcAttackTimers.entrySet()) {
            WorldArea npcArea;
            CaveNPCs caveNpc;
            NPC attackingNpc;
            if (entry.getValue() != 0 || (attackingNpc = this.client.getCachedNPCs()[entry.getKey()]) == null || attackingNpc.isDead() || (caveNpc = this.caveNPCManager.getNpcByNameContains(attackingNpc.getName())) == null || !this.isWithinAttackRange(npcArea = attackingNpc.getWorldArea(), playerLocation, caveNpc)) continue;
            attackingNpcs.add(caveNpc);
        }
        if (!attackingNpcs.isEmpty()) {
            attackingNpcs.sort((a, b) -> {
                if (a.getName().equals("Ket-Zek")) {
                    return -1;
                }
                if (b.getName().equals("Ket-Zek")) {
                    return 1;
                }
                if (a.getName().equals("Yt-MejKot")) {
                    return -1;
                }
                if (b.getName().equals("Yt-MejKot")) {
                    return 1;
                }
                if (a.getName().equals("Tok-Xil")) {
                    return -1;
                }
                if (b.getName().equals("Tok-Xil")) {
                    return 1;
                }
                if (a.getName().equals("Tz-Kek")) {
                    return -1;
                }
                if (b.getName().equals("Tz-Kek")) {
                    return 1;
                }
                if (a.getName().equals("Tz-Kih")) {
                    return -1;
                }
                if (b.getName().equals("Tz-Kih")) {
                    return 1;
                }
                return 0;
            });
            CaveNPCs highestPriorityNpc = (CaveNPCs)attackingNpcs.get(0);
            if (highestPriorityNpc.getPrayer() != null) {
                PrayerInteraction.flickPrayers(highestPriorityNpc.getPrayer(), this.config.offensivePrayer().prayer);
            }
        } else {
            this.disablePrayer();
        }
        if (this.client.getLocalPlayer().isInteracting()) {
            PrayerInteraction.setPrayerState(this.config.offensivePrayer().prayer, true);
        } else {
            PrayerInteraction.setPrayerState(this.config.offensivePrayer().prayer, false);
        }
    }

    private void handleLazyFlickPrayer() {
        if (this.jadFightActive()) {
            if (this.currentJadPrayer != null) {
                if (this.attackDelayTimer <= 1) {
                    PrayerInteraction.setPrayerState(this.currentJadPrayer, true);
                } else {
                    PrayerInteraction.setPrayerState(this.currentJadPrayer, false);
                }
            }
            if (this.config.offensivePrayer() != null) {
                PrayerInteraction.setPrayerState(this.config.offensivePrayer().prayer, true);
            }
            return;
        }
        if (!this.fightStarted()) {
            this.disablePrayer();
            return;
        }
        Player localPlayer = this.client.getLocalPlayer();
        if (localPlayer == null) {
            return;
        }
        WorldPoint playerLocation = localPlayer.getWorldLocation();
        ArrayList<CaveNPCs> attackingNpcs = new ArrayList<CaveNPCs>();
        boolean anyNpcsAlive = false;
        boolean shouldPray = false;
        for (Map.Entry<Integer, Integer> entry : this.npcAttackTimers.entrySet()) {
            WorldArea npcArea;
            NPC attackingNpc = this.client.getCachedNPCs()[entry.getKey()];
            if (attackingNpc == null || attackingNpc.isDead()) continue;
            anyNpcsAlive = true;
            CaveNPCs caveNpc = this.caveNPCManager.getNpcByNameContains(attackingNpc.getName());
            if (caveNpc == null || entry.getValue() != 0 && entry.getValue() != 1 || !this.isWithinAttackRange(npcArea = attackingNpc.getWorldArea(), playerLocation, caveNpc)) continue;
            attackingNpcs.add(caveNpc);
            shouldPray = true;
        }
        if (!attackingNpcs.isEmpty()) {
            attackingNpcs.sort(Comparator.comparingInt(CaveNPCs::getAttackPriority));
            CaveNPCs highestPriorityNpc = (CaveNPCs)attackingNpcs.get(0);
            if (highestPriorityNpc.getPrayer() != null) {
                if (this.attackDelayTimer <= 1) {
                    PrayerInteraction.setPrayerState(highestPriorityNpc.getPrayer(), true);
                } else {
                    PrayerInteraction.setPrayerState(highestPriorityNpc.getPrayer(), false);
                }
            }
        } else {
            this.disablePrayer();
        }
        if (anyNpcsAlive) {
            if (this.config.offensivePrayer() != null) {
                PrayerInteraction.setPrayerState(this.config.offensivePrayer().prayer, true);
            }
        } else {
            this.disablePrayer();
        }
    }

    private void handleRealisticFlickPrayer() {
        Player localPlayer = this.client.getLocalPlayer();
        if (localPlayer == null) {
            return;
        }
        if (isMoving()) {
            this.handleRegularPrayer();
            return;
        }
        this.handleOneTickFlickPrayer();
    }

    private void checkForSameTickAttackers() {
        List<NPC> tokXils = NPCs.search().withName("Tok-Xil").alive().result();
        List<NPC> ytMejkots = NPCs.search().withName("Yt-MejKot").alive().result();
        List<NPC> ketZeks = NPCs.search().withName("Ket-Zek").alive().result();
        for (NPC tokXil : tokXils) {
            for (NPC ytMejkot : ytMejkots) {
                if (this.npcAttackTimers.get(tokXil.getIndex()) == null || this.npcAttackTimers.get(ytMejkot.getIndex()) == null || !this.npcAttackTimers.get(tokXil.getIndex()).equals(this.npcAttackTimers.get(ytMejkot.getIndex()))) continue;
                log.info("Tok-Xil and Yt-MejKot are attacking on the same tick!");
                this.stepUnderNpc(tokXil);
                return;
            }
            for (NPC ketZek : ketZeks) {
                if (this.npcAttackTimers.get(tokXil.getIndex()) == null || this.npcAttackTimers.get(ketZek.getIndex()) == null || !this.npcAttackTimers.get(tokXil.getIndex()).equals(this.npcAttackTimers.get(ketZek.getIndex()))) continue;
                log.info("Tok-Xil and Ket-Zek are attacking on the same tick!");
                this.stepUnderNpc(tokXil);
                return;
            }
        }
        for (NPC ytMejkot : ytMejkots) {
            for (NPC ketZek : ketZeks) {
                if (this.npcAttackTimers.get(ytMejkot.getIndex()) == null || this.npcAttackTimers.get(ketZek.getIndex()) == null || !this.npcAttackTimers.get(ytMejkot.getIndex()).equals(this.npcAttackTimers.get(ketZek.getIndex()))) continue;
                log.info("Yt-MejKot and Ket-Zek are attacking on the same tick!");
                this.stepUnderNpc(ytMejkot);
                return;
            }
        }
    }

    private void kitePrayerDrainer() {
        List<NPC> tzKihs = NPCs.search().withName("Tz-Kih").alive().result();
        Player localPlayer = this.client.getLocalPlayer();
        WorldPoint playerLocation = localPlayer.getWorldLocation();
        if (this.hasSameTickAttackers()) {
            return;
        }
        Optional<NPC> closestTzKih = tzKihs.stream().min(Comparator.comparingInt(npc -> npc.getWorldLocation().distanceTo(playerLocation)));
        if (closestTzKih.isPresent()) {
            WorldPoint newPosition;
            NPC tzKih = closestTzKih.get();
            WorldPoint tzKihLocation = tzKih.getWorldLocation();
            int distance = playerLocation.distanceTo(tzKihLocation);
            if (this.attackDelayTimer == 0) {
                this.handleInteraction(tzKih);
                this.interactionCooldown = COOLDOWN_TICKS;
                return;
            }
            if (this.attackDelayTimer > 1 && distance <= 5 && (newPosition = this.findSafeSpot(playerLocation, tzKihLocation)) != null) {
                this.handleMovement(newPosition);
            }
        }
    }

    private void moveAwayFromMagerAndRanger() {
        WorldPoint safeSpot;
        List<NPC> dangerousNpcs = new ArrayList<>();
        Player localPlayer = this.client.getLocalPlayer();
        WorldPoint playerLocation = localPlayer.getWorldLocation();
        if (!this.isInSafeZone(playerLocation, dangerousNpcs) && (safeSpot = this.findSafeSpotFromNpcs(playerLocation, dangerousNpcs)) != null && !this.dangerousTiles.contains(safeSpot)) {
            this.handleMovement(safeSpot);
        }
    }

    private WorldPoint findSafeSpotFromNpcs(WorldPoint playerLocation, List<NPC> dangerousNpcs) {
        ArrayList<WorldPoint> potentialSpots = new ArrayList<WorldPoint>();
        for (int dx = -5; dx <= 5; ++dx) {
            for (int dy = -5; dy <= 5; ++dy) {
                WorldPoint potentialSpot = new WorldPoint(playerLocation.getX() + dx, playerLocation.getY() + dy, playerLocation.getPlane());
                if (!this.isInSafeZone(potentialSpot, dangerousNpcs)) continue;
                potentialSpots.add(potentialSpot);
            }
        }
        return potentialSpots.stream()
            .min(Comparator.comparingInt(tile -> tile.distanceTo(playerLocation)))
            .orElse(null);
    }

    private boolean isInSafeZone(WorldPoint playerLocation, List<NPC> dangerousNpcs) {
        for (NPC npc : dangerousNpcs) {
            WorldArea npcArea = npc.getWorldArea();
            if (npcArea == null || npcArea.distanceTo(playerLocation) > 1) continue;
            return false;
        }
        return true;
    }

    private void disablePrayer() {
        if (this.jadFightActive()) {
            return;
        }
        for (Prayer prayer : Prayer.values()) {
            if (!this.client.isPrayerActive(prayer)) continue;
            PrayerInteraction.setPrayerState(prayer, false);
        }
    }

    private boolean hasAllItems() {
        return hasFood() && hasRestores() && hasRangingPots() && (!config.useStaminas() || hasStaminas());
    }

    private boolean hasFood() {
        return Inventory.search().withId(config.foodType().getFoodTypeId()).result().size() >= config.foodAmounts();
    }

    private boolean hasRestores() {
        return Inventory.search().withId(config.restorePotionType().getRestorePotionId()).result().size() >= config.restoreAmounts();
    }

    private boolean hasRangingPots() {
        return Inventory.search().withId(2444).result().size() >= config.rangingPotions();
    }

    private boolean hasStaminas() {
        return config.staminaAmounts() == 0 || Inventory.search().withId(12625).result().size() >= config.staminaAmounts();
    }

    public CaveNPCs findCaveNpc(String name) {
        return this.caveNPCManager.getNpcByNameContains(name);
    }

    public boolean isWithinAttackRange(WorldArea npcArea, WorldPoint playerLocation, CaveNPCs caveNpc) {
        int attackRange = caveNpc.getName().equals("Ket-Zek") || caveNpc.getName().equals("Tok-Xil") ? 16 : 2;
        for (WorldPoint npcTile : npcArea.toWorldPointList()) {
            if (npcTile.distanceTo(playerLocation) > attackRange) continue;
            return true;
        }
        return false;
    }

    public boolean canSpec() {
        return this.client.getVarpValue(300) >= this.config.specWeapon().getSpecRequired() * 10;
    }

    private boolean isSpecActive() {
        return this.client.getVarpValue(301) == 1;
    }

    private boolean specItemEquipped() {
        return Equipment.search().withId(this.config.specWeapon().getItemId()).first().isPresent();
    }

    public boolean fightStarted() {
        return this.client.getTopLevelWorldView().getScene().isInstance();
    }

    public boolean canUseSpecialAttack() {
        boolean isBlowpipe = Equipment.search().nameContains("Blowpipe").first().isPresent();
        int specialAttackEnergy = this.client.getVarpValue(300);
        return isBlowpipe && specialAttackEnergy >= 1000;
    }

    public Instant getStartTime() {
        return this.startTime;
    }

    public CaveStates getState() {
        return this.state;
    }

    public int getCurrentWave() {
        return this.currentWave;
    }

    public Map<Integer, Integer> getNpcAttackTimers() {
        return this.npcAttackTimers;
    }

    public boolean isStarted() {
        return this.started;
    }

    private void moveAwayFromJad(NPC jad) {
        if (jad == null || client.getLocalPlayer() == null) {
            return;
        }

        WorldPoint safeSpot = findSafeSpotFromNpcs(client.getLocalPlayer().getWorldLocation(), Collections.singletonList(jad));
        if (safeSpot != null) {
            handleMovement(safeSpot);
        }
    }

    private void tagJadHealers() {
        List<NPC> healers = NPCs.search()
            .withName("Yt-HurKot")
            .alive()
            .result();
            
        if (!healers.isEmpty()) {
            NPC healer = healers.get(0);
            handleInteraction(healer);
        }
    }

    private void handleSpec() {
        if (!canSpec() || !specItemEquipped()) {
            return;
        }

        Widget specOrb = client.getWidget(593, 37);
        if (specOrb != null && !isMoving()) {
            handleWidgetInteraction(specOrb, "Use <col=ff981f>Special Attack</col>");
        }
    }

    private void handleAutoEat() {
        if (eatDelay > 0) {
            return;
        }

        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }

        int currentHp = player.getHealthRatio();
        int maxHp = player.getHealthScale();
        double hpPercent = (currentHp * 100.0) / maxHp;

        // Check if we need to drink brew (when HP is low)
        if (hpPercent < 60) {
            ItemQuery brewQuery = Inventory.search().filter(item -> item.getName().contains("Saradomin brew"));
            if (brewQuery.first().isPresent()) {
                handleWidgetInteraction(brewQuery.first().get(), "Drink");
                eatDelay = 3;
                brewSipsTaken++;
                return;
            }
        }

        // Check if we need restore (after drinking brews or when prayer is low)
        if (brewSipsTaken > 0 || client.getBoostedSkillLevel(Skill.PRAYER) <= client.getRealSkillLevel(Skill.PRAYER) * 0.5) {
            ItemQuery restoreQuery = Inventory.search().filter(item -> item.getName().contains("Super restore"));
            if (restoreQuery.first().isPresent()) {
                handleWidgetInteraction(restoreQuery.first().get(), "Drink");
                eatDelay = 3;
                brewSipsTaken = Math.max(0, brewSipsTaken - 1);
                return;
            }
        }

        // Check if we need ranging potion (only when our ranging level is not boosted)
        if (client.getBoostedSkillLevel(Skill.RANGED) <= client.getRealSkillLevel(Skill.RANGED)) {
            ItemQuery rangingQuery = Inventory.search().filter(item -> item.getName().contains("Ranging potion"));
            if (rangingQuery.first().isPresent()) {
                handleWidgetInteraction(rangingQuery.first().get(), "Drink");
                eatDelay = 3;
            }
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("cavesplugin")) {
            return;
        }

        if (event.getKey().equals("pluginEnabled")) {
            boolean enabled = config.pluginEnabled();
            if (enabled && !started) {
                start();
            } else if (!enabled && started) {
                stop();
            }
        }
    }

    private void start() {
        started = true;
        startTime = Instant.now();
        overlayManager.add(overlay);
    }

    private void stop() {
        started = false;
        startTime = null;
        overlayManager.remove(overlay);
    }

    private void assignInteractionCooldown() {
        interactionCooldown = COOLDOWN_TICKS;
    }

    private void checkBlowpipeCharges() {
        Optional<Widget> blowpipe = Inventory.search().withName("Toxic blowpipe").first();
        if (blowpipe.isPresent()) {
            InventoryInteraction.useItem(blowpipe.get(), "Check");
        }
    }

    private boolean hasEnoughBlowPipeCharges() {
        Optional<Widget> blowpipe = Inventory.search().withName("Toxic blowpipe").first();
        return blowpipe.isPresent(); // TODO: Implement proper charge checking
    }

    private void handleRunEnergy() {
        if (client.getEnergy() < 50) {
            Optional<Widget> runOrb = Widgets.search().withId(runOrbWidgetId).first();
            Optional<Widget> stamina = Inventory.search().withName("Stamina potion").first();
            
            if (stamina.isPresent()) {
                InventoryInteraction.useItem(stamina.get(), "Drink");
            }
            
            if (runOrb.isPresent() && client.getVarpValue(173) == 0) {
                WidgetInteraction.interact(runOrb.get(), "Toggle Run");
            }
        }
    }

    private boolean hasSameTickAttackers() {
        Map<Integer, List<NPC>> attackersByTick = new HashMap<>();
        
        for (Map.Entry<Integer, Integer> entry : npcAttackTimers.entrySet()) {
            NPC npc = client.getCachedNPCs()[entry.getKey()];
            int ticksUntilAttack = entry.getValue();
            
            if (npc != null && ticksUntilAttack <= 8) {
                attackersByTick.computeIfAbsent(ticksUntilAttack, k -> new ArrayList<>()).add(npc);
            }
        }
        
        return attackersByTick.values().stream().anyMatch(list -> list.size() > 1);
    }

    private void stepUnderNpc(NPC npc) {
        if (npc == null) return;
        
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) return;
        
        WorldArea npcArea = npc.getWorldArea();
        if (npcArea == null) return;
        
        WorldPoint closestTile = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (WorldPoint tile : npcArea.toWorldPointList()) {
            int currentDistance = localPlayer.getWorldLocation().distanceTo(tile);
            if (dangerousTiles.contains(tile) || currentDistance >= minDistance) continue;
            minDistance = currentDistance;
            closestTile = tile;
        }
        
        if (closestTile != null && !isMoving()) {
            handleMovement(closestTile);
        }
    }

    private void updateDangerousTiles() {
        dangerousTiles.clear();
        List<NPC> magers = NPCs.search().withName("Ket-Zek").alive().result();
        List<NPC> jad = NPCs.search().withName("TzTok-Jad").alive().result();
        
        updateDangerousTilesForNpcs(magers);
        updateDangerousTilesForNpcs(jad);
    }

    private void updateDangerousTilesForNpcs(List<NPC> npcs) {
        for (NPC npc : npcs) {
            WorldPoint location = npc.getWorldLocation();
            int size = npc.getComposition().getSize();
            WorldArea npcArea = new WorldArea(location, size, size);
            
            for (WorldPoint tile : npcArea.toWorldPointList()) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        dangerousTiles.add(new WorldPoint(tile.getX() + dx, tile.getY() + dy, tile.getPlane()));
                    }
                }
            }
        }
    }

    public CaveStates getCurrentState() {
        return state;
    }

    static {
        ANIMATION_TO_ATTACK_SPEED.put(422, 5);
        ANIMATION_TO_ATTACK_SPEED.put(5061, 3);
    }
}
