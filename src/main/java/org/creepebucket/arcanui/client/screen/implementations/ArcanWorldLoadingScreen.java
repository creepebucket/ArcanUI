package org.creepebucket.arcanui.client.screen.implementations;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.network.chat.Component;
import org.creepebucket.arcanui.ModColors;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.creepebucket.arcanui.client.screen.lib.ArcanUiRuntime;
import org.creepebucket.creepebucketlib.lib.api.Animation;
import org.creepebucket.creepebucketlib.lib.api.DynamicValue;
import org.creepebucket.creepebucketlib.lib.widgets.ProgressBarWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextWidget;

import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromCenter;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromTopLeft;

public class ArcanWorldLoadingScreen extends LevelLoadingScreen implements ArcanScreen {
    public final ArcanUiRuntime ui = new ArcanUiRuntime();

    public ArcanWorldLoadingScreen(LevelLoadTracker loadTracker, Reason reason) {
        super(loadTracker, reason);
    }

    @Override
    public ArcanUiRuntime ui() {
        return this.ui;
    }

    @Override
    public void buildUi(ArcanUiRuntime ui) {
        ui.addWidget(new TextWidget(fromCenter(0, -3),  Component.translatable("multiplayer.downloadingTerrain")).centerAlign().bottomAlignY().addAnimation(new Animation.FadeIn.FromTop(0.3), 0));
        ui.addWidget(new ProgressBarWidget(fromCenter(0, 3), fromTopLeft(100, 2), DynamicValue.fromSupplier(() -> (double) this.loadTracker.serverProgress()), DynamicValue.staticValue(1.0))
                .mainColor(ModColors.MAIN_COLOR_M.withAlpha(0xbf)).bgColor(0x3f000000).centerAlign().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }
}
