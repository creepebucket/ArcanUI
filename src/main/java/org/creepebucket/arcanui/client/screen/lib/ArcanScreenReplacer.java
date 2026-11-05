package org.creepebucket.arcanui.client.screen.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public interface ArcanScreenReplacer {
    boolean matches(Screen screen);

    Screen replace(Screen screen, Minecraft minecraft);
}
