/*
 * Copyright (c) 2020, Dutta64 <https://github.com/dutta64>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.lucidplugins.inferno;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.Instant;
import net.runelite.client.plugins.Plugin;
import com.lucidplugins.inferno.InfernoPlugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;

class InfernoSpawnTimerInfobox
extends InfoBox {
    private static final long SPAWN_DURATION = 210L;
    private static final long SPAWN_DURATION_INCREMENT = 105L;
    private static final long SPAWN_DURATION_WARNING = 120L;
    private static final long SPAWN_DURATION_DANGER = 30L;
    private long timeRemaining;
    private long startTime;
    private boolean running;

    InfernoSpawnTimerInfobox(BufferedImage image, InfernoPlugin plugin) {
        super(image, (Plugin)plugin);
        this.setPriority(InfoBoxPriority.HIGH);
        this.running = false;
        this.timeRemaining = 210L;
    }

    void run() {
        this.startTime = Instant.now().getEpochSecond();
        this.running = true;
    }

    void reset() {
        this.running = false;
        this.timeRemaining = 210L;
    }

    void pause() {
        if (this.running) {
            this.running = false;
            long timeElapsed = Instant.now().getEpochSecond() - this.startTime;
            this.timeRemaining = Math.max(0L, this.timeRemaining - timeElapsed);
            this.timeRemaining += 105L;
        }
    }

    public String getText() {
        long seconds = this.running ? Math.max(0L, this.timeRemaining - (Instant.now().getEpochSecond() - this.startTime)) : this.timeRemaining;
        long minutes = seconds % 3600L / 60L;
        long secs = seconds % 60L;
        return String.format("%02d:%02d", minutes, secs);
    }

    public Color getTextColor() {
        long seconds;
        long l = seconds = this.running ? Math.max(0L, this.timeRemaining - (Instant.now().getEpochSecond() - this.startTime)) : this.timeRemaining;
        return seconds <= 30L ? Color.RED : (seconds <= 120L ? Color.ORANGE : Color.GREEN);
    }

    public boolean render() {
        return true;
    }

    public boolean cull() {
        return false;
    }

    boolean isRunning() {
        return this.running;
    }
}
