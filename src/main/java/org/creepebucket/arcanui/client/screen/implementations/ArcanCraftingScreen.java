package org.creepebucket.arcanui.client.screen.implementations;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.creepebucket.arcanui.client.screen.lib.ArcanUiRuntime;

public class ArcanCraftingScreen extends CraftingScreen implements ArcanScreen {
    public final ArcanUiRuntime ui = new ArcanUiRuntime();

    public ArcanCraftingScreen(CraftingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public ArcanUiRuntime ui() {
        return this.ui;
    }

    @Override
    protected void init() {
        super.init();

        this.clearWidgets();
    }

    @Override
    public void buildUi(ArcanUiRuntime ui) {
    }

    @Override
    public void applyBounds(int left, int top, int width, int height) {
        this.leftPos = left;
        this.topPos = top;
        this.imageWidth = width;
        this.imageHeight = height;
    }

    @Override
    public void disposeUi() {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }
}
