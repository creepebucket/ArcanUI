package org.creepebucket.arcanui.client.screen.implementations;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import org.creepebucket.arcanui.ModColors;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.creepebucket.arcanui.client.screen.lib.ArcanUiRuntime;
import org.creepebucket.creepebucketlib.lib.api.Animation;
import org.creepebucket.creepebucketlib.lib.api.Widget;
import org.creepebucket.creepebucketlib.lib.widgets.MultilineTextWidget;
import org.creepebucket.creepebucketlib.lib.widgets.RectangleWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextButtonWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextWidget;

import static org.creepebucket.creepebucketlib.lib.api.Coordinate.*;

public class ArcanConfirmScreen extends ConfirmScreen implements ArcanScreen {
    public ArcanUiRuntime ui = new ArcanUiRuntime();

    public ArcanConfirmScreen(BooleanConsumer callback, Component title, Component message, Component yesButtonComponent, Component noButtonComponent) {
        super(callback, title, message, yesButtonComponent, noButtonComponent);
    }

    @Override
    public ArcanUiRuntime ui() {
        return this.ui;
    }

    @Override
    public void buildUi(ArcanUiRuntime ui) {
        var menu = ui.addWidget(new Widget.BlankWidget(CENTER, fromTopLeft(320, 120)).centerAlign().centerAlignY());
        menu.addChild(new RectangleWidget(ZERO, FULL).mainColor(0x7f000000).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0));

        // 标题与分隔线
        menu.addChild(new TextWidget(fromTopLeft(160, 12), getTitle()).noShadow().centerAlign().addAnimation(new Animation.FadeIn.FromTop(0.3), 0));
        menu.addChild(new RectangleWidget(fromTopLeft(20, 28), fromTopLeft(280, 1)).mainColor(ModColors.MAIN_COLOR_M.withAlpha(0x7f)).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.05));

        // 确认信息
        menu.addChild(new MultilineTextWidget(fromTopLeft(20, 34), fromTopLeft(280, 30), this.message).noShadow().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.1));

        // 确认 / 取消
        menu.addChild(new TextButtonWidget(fromTopLeft(15, 92), fromTopLeft(140, 20), this.yesButtonComponent, playQuitAnimationAndDo(() -> this.callback.accept(true)))
                .mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)).bgColor(0x3f000000).textColor(ModColors.TEXT_COLOR_T).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.2));

        menu.addChild(new TextButtonWidget(fromTopLeft(165, 92), fromTopLeft(140, 20), this.noButtonComponent, playQuitAnimationAndDo(() -> this.callback.accept(false)))
                .mainColor(0x3fffffff).bgColor(0x3f000000).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.2));
    }
}
