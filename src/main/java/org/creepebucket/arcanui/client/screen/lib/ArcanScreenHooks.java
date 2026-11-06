package org.creepebucket.arcanui.client.screen.lib;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.creepebucket.arcanui.Arcanui;
import org.creepebucket.arcanui.client.screen.implementations.ArcanCraftingScreen;
import org.creepebucket.arcanui.client.screen.implementations.ArcanLanguageSelectScreen;
import org.creepebucket.arcanui.client.screen.implementations.ArcanTitleScreen;

@EventBusSubscriber(modid = Arcanui.MODID, value = Dist.CLIENT)
public final class ArcanScreenHooks {

    public interface ArcanScreenReplacer {
        boolean matches(Screen screen);

        Screen replace(Screen screen, Minecraft minecraft);
    }

    private static final List<ArcanScreenReplacer> REPLACERS = List.of(
            new ArcanScreenReplacer() {
                @Override
                public boolean matches(Screen screen) {
                    return screen instanceof TitleScreen && !(screen instanceof ArcanTitleScreen);
                }

                @Override
                public Screen replace(Screen screen, Minecraft minecraft) {
                    return new ArcanTitleScreen();
                }
            },
            new ArcanScreenReplacer() {
                @Override
                public boolean matches(Screen screen) {
                    return screen instanceof LanguageSelectScreen && !(screen instanceof ArcanLanguageSelectScreen);
                }

                @Override
                public Screen replace(Screen screen, Minecraft minecraft) {
                    return new ArcanLanguageSelectScreen(minecraft.screen, minecraft.options, minecraft.getLanguageManager());
                }
            },
            new ArcanScreenReplacer() {
                @Override
                public boolean matches(Screen screen) {
                    return Minecraft.getInstance().player != null
                            && screen instanceof CraftingScreen && !(screen instanceof ArcanCraftingScreen);
                }

                @Override
                public Screen replace(Screen screen, Minecraft minecraft) {
                    CraftingScreen crafting = (CraftingScreen) screen;
                    return new ArcanCraftingScreen(
                            crafting.getMenu(),
                            minecraft.player.getInventory(),
                            crafting.getTitle()
                    );
                }
            }
    );

    private ArcanScreenHooks() {
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Minecraft minecraft = Minecraft.getInstance();

        Screen screen = event.getNewScreen();
        for (ArcanScreenReplacer replacer : REPLACERS) {
            if (replacer.matches(screen)) {
                event.setNewScreen(replacer.replace(screen, minecraft));
                return;
            }
        }
    }
}
