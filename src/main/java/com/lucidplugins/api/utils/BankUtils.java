package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.Collections.Bank;
import com.example.InteractionApi.BankInteraction;
import com.example.Packets.WidgetPackets;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;

public class BankUtils
{
    private static final int BANK_GROUP_ID = 12;
    private static final int BANK_INVENTORY_ITEMS_CONTAINER = 13;
    private static final int BANK_DEPOSIT_INVENTORY_GROUP = 12;
    private static final int BANK_DEPOSIT_INVENTORY_CHILD = 42;
    private static final int BANK_CLOSE_SCRIPT_ID = 29;
    static Client client = RuneLite.getInjector().getInstance(Client.class);

    public static boolean isOpen()
    {
        Widget bankWidget = client.getWidget(BANK_GROUP_ID, BANK_INVENTORY_ITEMS_CONTAINER);
        return bankWidget != null && !bankWidget.isSelfHidden();
    }

    public static void depositAll()
    {
        Widget depositInventory = client.getWidget(BANK_DEPOSIT_INVENTORY_GROUP, BANK_DEPOSIT_INVENTORY_CHILD);
        if (depositInventory != null && !depositInventory.isSelfHidden())
        {
            WidgetPackets.queueWidgetAction(depositInventory, "Deposit inventory");
        }
    }

    public static boolean withdraw1(int id)
    {
        if (!isOpen() || Bank.search().withId(id).first().isEmpty())
        {
            return false;
        }

        BankInteraction.useItem(id, "Withdraw-1");
        return true;
    }

    public static boolean withdrawAll(int id)
    {
        if (!isOpen() || Bank.search().withId(id).first().isEmpty())
        {
            return false;
        }

        BankInteraction.useItem(id, "Withdraw-All");
        return true;
    }

    public static void close()
    {
        if (isOpen())
        {
            client.runScript(BANK_CLOSE_SCRIPT_ID);
        }
    }
}
