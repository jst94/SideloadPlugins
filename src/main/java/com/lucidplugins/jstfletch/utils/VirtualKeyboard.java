package com.lucidplugins.jstfletch.utils;

import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;

public class VirtualKeyboard {
    private static final Client client = RuneLite.getInjector().getInstance(Client.class);
    
    public static void type(String input) {
        Widget chatbox = client.getWidget(162, 45);
        if (chatbox != null) {
            for (char c : input.toCharArray()) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetActionPacket(1, chatbox.getId(), -1, (int)c);
                try {
                    Thread.sleep(50); // Small delay between characters
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            // Send enter key after typing
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(1, chatbox.getId(), -1, 84); // 84 is the keycode for Enter
        }
    }
}
