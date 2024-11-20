package com.lucidplugins.api.utils;

import lombok.Getter;

@Getter
public class DialogOption {
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
