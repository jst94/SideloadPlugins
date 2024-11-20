package com.lucidplugins.jstfightcaves.Variables;

import com.lucidplugins.jstfightcaves.Variables.CaveNPCs;
import java.util.Arrays;
import java.util.List;
import net.runelite.api.Prayer;

public class CaveNPCManager {
    private List<CaveNPCs> npcs;

    public CaveNPCManager() {
        this.loadNpcs();
    }

    private void loadNpcs() {
        this.npcs = Arrays.asList(new CaveNPCs("Tz-Kih", 4, Prayer.PROTECT_FROM_MELEE, 4, 2621, true, 1), new CaveNPCs("Tz-Kek", 4, Prayer.PROTECT_FROM_MELEE, 3, 2625, true, 1), new CaveNPCs("Tok-Xil", 4, Prayer.PROTECT_FROM_MISSILES, 2, 2633, false, 15), new CaveNPCs("Yt-MejKot", 4, Prayer.PROTECT_FROM_MELEE, 3, 2637, true, 1), new CaveNPCs("Ket-Zek", 4, Prayer.PROTECT_FROM_MAGIC, 1, 2647, false, 15), new CaveNPCs("Yt-HurtKot", 4, null, 0, 2637, true, 1), new CaveNPCs("TzTok-Jad", 5, null, 1, 2656, false, 15));
    }

    public List<CaveNPCs> getNpcs() {
        return this.npcs;
    }

    public CaveNPCs getNpcByNameContains(String name) {
        if (name == null) {
            return null;
        }
        return this.npcs.stream().filter(npc -> npc.getName() != null && npc.getName().toLowerCase().contains(name.toLowerCase())).findFirst().orElse(null);
    }
}
