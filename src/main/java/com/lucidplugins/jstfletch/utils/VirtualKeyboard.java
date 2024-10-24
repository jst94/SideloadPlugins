package com.lucidplugins.jstfletch.utils;

public class VirtualKeyboard {
    public static void type(String input) {
        for (char c : input.toCharArray()) {
            typeChar(c);
        }
    }

    private static void typeChar(char c) {
        switch (Character.toLowerCase(c)) {
            case '1': pressKey(java.awt.event.KeyEvent.VK_1); break;
            case '2': pressKey(java.awt.event.KeyEvent.VK_2); break;
            case '3': pressKey(java.awt.event.KeyEvent.VK_3); break;
            case '4': pressKey(java.awt.event.KeyEvent.VK_4); break;
            case '5': pressKey(java.awt.event.KeyEvent.VK_5); break;
            case '6': pressKey(java.awt.event.KeyEvent.VK_6); break;
            case '7': pressKey(java.awt.event.KeyEvent.VK_7); break;
            case '8': pressKey(java.awt.event.KeyEvent.VK_8); break;
            case '9': pressKey(java.awt.event.KeyEvent.VK_9); break;
            case '0': pressKey(java.awt.event.KeyEvent.VK_0); break;
            case 'a': pressKey(java.awt.event.KeyEvent.VK_A); break;
            case 'b': pressKey(java.awt.event.KeyEvent.VK_B); break;
            case 'c': pressKey(java.awt.event.KeyEvent.VK_C); break;
            case 'd': pressKey(java.awt.event.KeyEvent.VK_D); break;
            case 'e': pressKey(java.awt.event.KeyEvent.VK_E); break;
            case 'f': pressKey(java.awt.event.KeyEvent.VK_F); break;
            case 'g': pressKey(java.awt.event.KeyEvent.VK_G); break;
            case 'h': pressKey(java.awt.event.KeyEvent.VK_H); break;
            case 'i': pressKey(java.awt.event.KeyEvent.VK_I); break;
            case 'j': pressKey(java.awt.event.KeyEvent.VK_J); break;
            case 'k': pressKey(java.awt.event.KeyEvent.VK_K); break;
            case 'l': pressKey(java.awt.event.KeyEvent.VK_L); break;
            case 'm': pressKey(java.awt.event.KeyEvent.VK_M); break;
            case 'n': pressKey(java.awt.event.KeyEvent.VK_N); break;
            case 'o': pressKey(java.awt.event.KeyEvent.VK_O); break;
            case 'p': pressKey(java.awt.event.KeyEvent.VK_P); break;
            case 'q': pressKey(java.awt.event.KeyEvent.VK_Q); break;
            case 'r': pressKey(java.awt.event.KeyEvent.VK_R); break;
            case 's': pressKey(java.awt.event.KeyEvent.VK_S); break;
            case 't': pressKey(java.awt.event.KeyEvent.VK_T); break;
            case 'u': pressKey(java.awt.event.KeyEvent.VK_U); break;
            case 'v': pressKey(java.awt.event.KeyEvent.VK_V); break;
            case 'w': pressKey(java.awt.event.KeyEvent.VK_W); break;
            case 'x': pressKey(java.awt.event.KeyEvent.VK_X); break;
            case 'y': pressKey(java.awt.event.KeyEvent.VK_Y); break;
            case 'z': pressKey(java.awt.event.KeyEvent.VK_Z); break;
            case ' ': pressKey(java.awt.event.KeyEvent.VK_SPACE); break;
            case '.': pressKey(java.awt.event.KeyEvent.VK_PERIOD); break;
            case ',': pressKey(java.awt.event.KeyEvent.VK_COMMA); break;
            case '\n': pressKey(java.awt.event.KeyEvent.VK_ENTER); break;
        }
    }
    private static void pressKey(int keyCode) {
        try {
            java.awt.Robot robot = new java.awt.Robot();
            robot.keyPress(keyCode);
            robot.delay(50);
            robot.keyRelease(keyCode);
            robot.delay(50);
        } catch (java.awt.AWTException e) {
            e.printStackTrace();
        }
    }}

