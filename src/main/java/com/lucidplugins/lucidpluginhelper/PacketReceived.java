package com.lucidplugins.lucidpluginhelper;

import lombok.Data;

@Data
public class PacketReceived {
    private final int packet;
    private final int length;

    public PacketReceived(int packet, int length) {
        this.packet = packet;
        this.length = length;
    }
}
