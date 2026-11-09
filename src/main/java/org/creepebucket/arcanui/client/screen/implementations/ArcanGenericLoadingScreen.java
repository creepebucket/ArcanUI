package org.creepebucket.arcanui.client.screen.implementations;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.creepebucket.arcanui.ModColors;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.creepebucket.arcanui.client.screen.lib.ArcanUiRuntime;
import org.creepebucket.creepebucketlib.ModUtils;
import org.creepebucket.creepebucketlib.lib.api.*;
import org.creepebucket.creepebucketlib.lib.api.widgets.Renderable;
import org.creepebucket.creepebucketlib.lib.widgets.ProgressBarWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextWidget;

import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromCenter;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromTopLeft;

public class ArcanGenericLoadingScreen extends GenericMessageScreen implements ArcanScreen {
    public final ArcanUiRuntime ui = new ArcanUiRuntime();

    public ArcanGenericLoadingScreen(Component title) {
        super(title);
    }

    @Override
    public ArcanUiRuntime ui() {
        return this.ui;
    }

    @Override
    public void buildUi(ArcanUiRuntime ui) {
        ui.addWidget(new TextWidget(fromCenter(0, -3), getTitle()).centerAlign().bottomAlignY().addAnimation(new Animation.FadeIn.FromTop(0.3), 0));
        ui.addWidget(new BarWidget(fromCenter(0, 3), fromTopLeft(100, 2)).mainColor(ModColors.MAIN_COLOR_M.withAlpha(0xbf)).bgColor(0x3f000000).centerAlign().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0));
    }

    public static class BarWidget extends Widget implements Renderable {
        SmoothedValue x1, x2;
        double releaseX2;

        public BarWidget(Coordinate position, Coordinate size) {
            super(position, size);
            x1 = new SmoothedValue(0);
            x2 = new SmoothedValue(0);
            releaseX2 = ModUtils.now() + .3;

            smoothedValues.add(x1);
            smoothedValues.add(x2);
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(left(), top(), right(), bottom(), bgColorInt());

            if (x1.getInt() == w() && x2.getInt() == w()) {
                x1.setImmediate(0);
                x2.setImmediate(0);
                releaseX2 = ModUtils.now() + .3;
            } else {
                x1.set(w());
            }

            if (releaseX2 < ModUtils.now()) {
                x2.set(w());
            }

            graphics.fill(left() + x2.getInt(), top(), left() + x1.getInt(), bottom(), mainColorInt());
        }
    }
}
