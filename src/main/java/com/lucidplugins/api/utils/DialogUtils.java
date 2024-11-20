package com.lucidplugins.api.utils;

import com.example.Packets.WidgetPackets;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;

import java.util.ArrayList;
import java.util.List;

public class DialogUtils {
    static Client client = RuneLite.getInjector().getInstance(Client.class);
    private static List<Integer> continueParentIds = List.of(193, 229, 229, 231, 217, 11);
    private static List<Integer> continueChildIds = List.of(   0,   0,   2,   5,   5,  4);

    public static void queueResumePauseDialog(int widgetId, int childId) {
        WidgetPackets.queueResumePause(widgetId, childId);
    }

    public static List<DialogOption> getOptions() {
        List<DialogOption> out = new ArrayList<>();
        Widget widget = client.getWidget(219, 1);
        if (widget == null || widget.isSelfHidden()) {
            return out;
        }

        Widget[] children = widget.getChildren();
        if (children == null) {
            return out;
        }

        for (int i = 1; i < children.length; ++i) {
            if (children[i] != null && !children[i].getText().isBlank()) {
                out.add(new DialogOption(i, children[i].getText(), children[i].getTextColor()));
            }
        }

        return out;
    }

    public static boolean canContinue() {
        for (int i = 0; i < continueParentIds.size(); i++) {
            Widget widget = client.getWidget(continueParentIds.get(i), continueChildIds.get(i));
            if (widget != null && !widget.isHidden()) {
                return true;
            }
        }
        return false;
    }

    public static void handleContinue() {
        for (int i = 0; i < continueParentIds.size(); i++) {
            Widget widget = client.getWidget(continueParentIds.get(i), continueChildIds.get(i));
            if (widget != null && !widget.isHidden()) {
                queueResumePauseDialog(continueParentIds.get(i), continueChildIds.get(i));
                break;
            }
        }
    }

    public static int getOptionIndex(String option) {
        for (DialogOption opt : getOptions()) {
            if (opt.getOptionText().contains(option)) {
                return opt.getIndex();
            }
        }
        return -1;
    }
}

class DialogOption {
    private final int index;
    private final String text;
    private final int color;

    public DialogOption(int index, String text, int color) {
        this.index = index;
        this.text = text;
        this.color = color;
    }

    public int getIndex() {
        return index;
    }

    public String getOptionText() {
        return text;
    }

    public int getColor() {
        return color;
    }
}
