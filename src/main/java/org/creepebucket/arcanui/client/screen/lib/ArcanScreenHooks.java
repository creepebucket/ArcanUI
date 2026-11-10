package org.creepebucket.arcanui.client.screen.lib;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.creepebucket.arcanui.Arcanui;
import org.creepebucket.arcanui.client.screen.implementations.ArcanConfirmLinkScreen;
import org.creepebucket.arcanui.client.screen.implementations.ArcanConfirmScreen;
import org.creepebucket.arcanui.client.screen.implementations.ArcanCraftingScreen;
import org.creepebucket.arcanui.client.screen.implementations.ArcanEditWorldScreen;
import org.creepebucket.arcanui.client.screen.implementations.ArcanGenericLoadingScreen;
import org.creepebucket.arcanui.client.screen.implementations.ArcanKeyBindsScreen;
import org.creepebucket.arcanui.client.screen.implementations.ArcanLanguageSelectScreen;
import org.creepebucket.arcanui.client.screen.implementations.ArcanOptionsMainScreen;
import org.creepebucket.arcanui.client.screen.implementations.ArcanPauseScreen;
import org.creepebucket.arcanui.client.screen.implementations.ArcanSelectWorldScreen;
import org.creepebucket.arcanui.client.screen.implementations.ArcanTitleScreen;
import org.creepebucket.arcanui.client.screen.implementations.ArcanWorldLoadingScreen;

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
                    return screen instanceof SelectWorldScreen && !(screen instanceof ArcanSelectWorldScreen);
                }

                @Override
                public Screen replace(Screen screen, Minecraft minecraft) {
                    return new ArcanSelectWorldScreen((SelectWorldScreen) screen);
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
                    return screen instanceof PauseScreen && !(screen instanceof ArcanPauseScreen);
                }

                @Override
                public Screen replace(Screen screen, Minecraft minecraft) {
                    return new ArcanPauseScreen(((PauseScreen) screen).showsPauseMenu());
                }
            },
            new ArcanScreenReplacer() {
                @Override
                public boolean matches(Screen screen) {
                    return screen instanceof OptionsScreen && !(screen instanceof ArcanOptionsMainScreen);
                }

                @Override
                public Screen replace(Screen screen, Minecraft minecraft) {
                    OptionsScreen options = (OptionsScreen) screen;
                    return new ArcanOptionsMainScreen(options.getLastScreen(), minecraft.options, minecraft.level != null);
                }
            },
            new ArcanScreenReplacer() {
                @Override
                public boolean matches(Screen screen) {
                    return screen instanceof KeyBindsScreen && !(screen instanceof ArcanKeyBindsScreen);
                }

                @Override
                public Screen replace(Screen screen, Minecraft minecraft) {
                    return new ArcanKeyBindsScreen(minecraft.screen, minecraft.options);
                }
            },
            new ArcanScreenReplacer() {
                @Override
                public boolean matches(Screen screen) {
                    return screen instanceof LevelLoadingScreen && !(screen instanceof ArcanWorldLoadingScreen);
                }

                @Override
                public Screen replace(Screen screen, Minecraft minecraft) {
                    LevelLoadingScreen loading = (LevelLoadingScreen) screen;
                    return new ArcanWorldLoadingScreen(loading.loadTracker, loading.reason);
                }
            },
            new ArcanScreenReplacer() {
                @Override
                public boolean matches(Screen screen) {
                    if (!(screen instanceof GenericMessageScreen) || screen instanceof ArcanGenericLoadingScreen) {
                        return false;
                    }

                    String title = screen.getTitle().getString();
                    return title.equals(Component.translatable("selectWorld.resource_load").getString())
                            || title.equals(Component.translatable("menu.savingLevel").getString());
                }

                @Override
                public Screen replace(Screen screen, Minecraft minecraft) {
                    return new ArcanGenericLoadingScreen(screen.getTitle());
                }
            },
            new ArcanScreenReplacer() {
                @Override
                public boolean matches(Screen screen) {
                    return screen instanceof ConfirmLinkScreen && !(screen instanceof ArcanConfirmLinkScreen);
                }

                @Override
                public Screen replace(Screen screen, Minecraft minecraft) {
                    ConfirmLinkScreen link = (ConfirmLinkScreen) screen;
                    return new ArcanConfirmLinkScreen(link.callback, link.url, !link.showWarning);
                }
            },
            new ArcanScreenReplacer() {
                @Override
                public boolean matches(Screen screen) {
                    return screen instanceof EditWorldScreen;
                }

                @Override
                public Screen replace(Screen screen, Minecraft minecraft) {
                    EditWorldScreen edit = (EditWorldScreen) screen;
                    return new ArcanEditWorldScreen(edit.levelAccess, edit.nameEdit.getValue(), edit.callback);
                }
            },
            new ArcanScreenReplacer() {
                @Override
                public boolean matches(Screen screen) {
                    return screen instanceof ConfirmScreen && !(screen instanceof ArcanConfirmScreen)
                            && screen.getTitle().getString().equals(Component.translatable("selectWorld.deleteQuestion").getString());
                }

                @Override
                public Screen replace(Screen screen, Minecraft minecraft) {
                    ConfirmScreen confirm = (ConfirmScreen) screen;
                    return new ArcanConfirmScreen(confirm.callback, confirm.getTitle(), confirm.message, confirm.yesButtonComponent, confirm.noButtonComponent);
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
