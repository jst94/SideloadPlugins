package com.lucidplugins.jstdolo;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.callback.ClientThread;

import javax.inject.Inject;
import java.time.Instant;

@Slf4j
@PluginDescriptor(
        name = "JST DOLO",
        description = "Soul Wars DOLO helper for pet hunting",
        tags = {"soul wars", "dolo", "pet"}
)
public class JstDoloPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private JstDoloConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private JstDoloOverlay overlay;

    @Inject
    private ClientThread clientThread;

    private JstDoloScript script;

    @Getter
    private boolean inGame = false;

    @Getter
    private int fragmentsCollected = 0;

    @Getter
    private Instant gameStartTime = null;

    @Provides
    JstDoloConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(JstDoloConfig.class);
    }

    @Override
    protected void startUp() {
        log.info("JST DOLO started!");
        script = new JstDoloScript(client, clientThread, config);
        overlayManager.add(overlay);
        reset();
    }

    @Override
    protected void shutDown() {
        log.info("JST DOLO stopped!");
        overlayManager.remove(overlay);
        script = null;
        reset();
    }

    private void reset() {
        inGame = false;
        fragmentsCollected = 0;
        gameStartTime = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOADING) {
            reset();
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        String message = event.getMessage();

        // Track game state and fragments
        if (message.contains("The battle begins!")) {
            inGame = true;
            gameStartTime = Instant.now();
        } else if (message.contains("The battle is over!")) {
            inGame = false;
        } else if (message.contains("You found some soul fragments")) {
            fragmentsCollected++;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        if (script != null) {
            script.handleGame();
        }
    }
}