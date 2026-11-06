package org.creepebucket.arcanui.client.screen.lib;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.creepebucket.arcanui.Arcanui;

@EventBusSubscriber(modid = Arcanui.MODID, value = Dist.CLIENT)
public final class ArcanScreenEvents {
    private ArcanScreenEvents() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof ArcanScreen screen)) {
            return;
        }

        for (GuiEventListener widget : List.copyOf(event.getListenersList())) {
            if (widget.getClass().getName().matches("net\\.(minecraft|neoforged)\\..*")) event.removeListener(widget);
        }

        screen.ui().begin(event.getScreen());
        screen.buildUi(screen.ui());
    }

    @SubscribeEvent
    public static void onRenderBackground(ScreenEvent.Render.Background event) {
        if (event.getScreen() instanceof ArcanScreen screen) {
            ArcanUiRuntime ui = screen.ui();
            ui.renderBackground(event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
            screen.applyBounds(ui.left, ui.top, ui.width, ui.height);
        }
    }

    @SubscribeEvent
    public static void onRenderPost(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof ArcanScreen screen) || event.getScreen() instanceof AbstractContainerScreen<?>) {
            return;
        }

        screen.ui().renderForeground(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onContainerForeground(ContainerScreenEvent.Render.Foreground event) {
        if (event.getContainerScreen() instanceof ArcanScreen screen) {
            screen.ui().renderForeground(
                    event.getGuiGraphics(),
                    event.getMouseX(),
                    event.getMouseY(),
                    Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks()
            );
        }
    }

    @SubscribeEvent
    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Post event) {
        if (event.getScreen() instanceof ArcanScreen screen) {
            screen.ui().mouseClicked(event.getMouseButtonEvent(), event.isDoubleClick());
        }
    }

    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Post event) {
        if (event.getScreen() instanceof ArcanScreen screen) {
            screen.ui().mouseReleased(event.getMouseButtonEvent());
        }
    }

    @SubscribeEvent
    public static void onMouseDragged(ScreenEvent.MouseDragged.Post event) {
        if (event.getScreen() instanceof ArcanScreen screen) {
            screen.ui().mouseDragged(event.getMouseButtonEvent(), event.getDragX(), event.getDragY());
        }
    }

    @SubscribeEvent
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Post event) {
        if (event.getScreen() instanceof ArcanScreen screen) {
            screen.ui().mouseScrolled(
                    event.getMouseX(), event.getMouseY(), event.getScrollDeltaX(), event.getScrollDeltaY());
        }
    }

    @SubscribeEvent
    public static void onKeyPressedPre(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof ArcanScreen screen) || !event.getKeyEvent().isEscape()) {
            return;
        }

        if (!event.getScreen().shouldCloseOnEsc()) {
            return;
        }

        screen.playQuitAnimationAndQuit();
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Post event) {
        if (event.getScreen() instanceof ArcanScreen screen) {
            screen.ui().keyPressed(event.getKeyEvent());
        }
    }

    @SubscribeEvent
    public static void onKeyReleased(ScreenEvent.KeyReleased.Post event) {
        if (event.getScreen() instanceof ArcanScreen screen) {
            screen.ui().keyReleased(event.getKeyEvent());
        }
    }

    @SubscribeEvent
    public static void onCharTyped(ScreenEvent.CharacterTyped.Post event) {
        if (event.getScreen() instanceof ArcanScreen screen) {
            screen.ui().charTyped(event.getCharacterEvent());
        }
    }

    @SubscribeEvent
    public static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof ArcanScreen screen) {
            screen.disposeUi();
            screen.ui().destroy();
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().screen instanceof ArcanScreen screen) {
            screen.ui().tick();
        }
    }
}
