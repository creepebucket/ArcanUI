package org.creepebucket.arcanui.client.screen.lib;

import net.minecraft.client.OptionInstance;

import java.util.Map;

public interface ArcanOptionsScreen extends ArcanScreen {
    Map<OptionInstance<?>, Runnable> commandQueue();
}
