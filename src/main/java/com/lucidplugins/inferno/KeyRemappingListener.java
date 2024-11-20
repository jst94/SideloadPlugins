package com.lucidplugins.inferno;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.input.KeyListener;

class KeyRemappingListener
implements KeyListener {
    private Boolean togglePrayer = false;
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    private final Map<Integer, Integer> modified = new HashMap<Integer, Integer>();
    private final Set<Character> blockedChars = new HashSet<Character>();

    KeyRemappingListener() {
    }

    public void keyTyped(KeyEvent e) {
        System.out.println(e);
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == 'i' && !this.togglePrayer.booleanValue()) {
            this.togglePrayer = true;
        } else if (e.getKeyChar() == 'i' && this.togglePrayer.booleanValue()) {
            this.togglePrayer = false;
        }
    }

    public void keyReleased(KeyEvent e) {
        System.out.println(e);
    }

    public Boolean getTogglePrayer() {
        return true;
    }
}
