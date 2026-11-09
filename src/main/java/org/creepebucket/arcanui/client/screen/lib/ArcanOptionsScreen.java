package org.creepebucket.arcanui.client.screen.lib;

import java.util.Map;

public interface ArcanOptionsScreen extends ArcanScreen {
    Map<Object, Runnable> commandQueue();
}
