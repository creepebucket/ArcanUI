package org.creepebucket.arcanui.client.screen.lib;

import net.minecraft.client.gui.screens.Screen;
import org.creepebucket.creepebucketlib.lib.api.Widget;
import org.creepebucket.creepebucketlib.lib.ui.WidgetManager;

public interface ArcanScreen extends WidgetManager {
    ArcanUiRuntime ui();

    void buildUi(ArcanUiRuntime ui);

    default void disposeUi() {
    }

    default void applyBounds(int left, int top, int width, int height) {
    }

    @Override
    default Widget.Root getRoot() {
        return ui().getRoot();
    }

    @Override
    default void setRoot(Widget.Root root) {
        ui().setRoot(root);
    }

    @Override
    default Screen getScreen() {
        return (Screen) this;
    }
}
