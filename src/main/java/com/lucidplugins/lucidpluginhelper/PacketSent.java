package com.lucidplugins.lucidpluginhelper;

import lombok.Data;

@Data
public class PacketSent {
    private final int packet;
    private final int length;

    public PacketSent(int packet, int length) {
        this.packet = packet;
        this.length = length;
    }
}
