package org.creepebucket.arcanui.mixin;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.creepebucket.arcanui.ModColors;
import org.creepebucket.arcanui.client.screen.implementations.OptionsSubScreenWidgets;
import org.creepebucket.arcanui.client.screen.lib.ArcanOptionsScreen;
import org.creepebucket.arcanui.client.screen.lib.ArcanUiRuntime;
import org.creepebucket.creepebucketlib.lib.api.*;
import org.creepebucket.creepebucketlib.lib.widgets.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.clamp;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.*;

// 所有 options 子屏（字体、声音、聊天、视频、控制等）的公共父类
@Mixin(OptionsSubScreen.class)
public abstract class OptionsSubScreenMixin implements ArcanOptionsScreen {
    public ArcanUiRuntime ui = new ArcanUiRuntime();
    public Map<OptionInstance<?>, Runnable> commandQueue = new LinkedHashMap<>();

    @Shadow protected OptionsList list;

    @Override
    public ArcanUiRuntime ui() {
        return this.ui;
    }

    @Override
    public Map<OptionInstance<?>, Runnable> commandQueue() {
        return commandQueue;
    }

    @Override
    public void buildUi(ArcanUiRuntime ui) {
        commandQueue.clear();

        var bg = ui.addWidget(new RectangleWidget(fromCenterTop(0, 0), fromBottomLeft(310, 0)).mainColor(0x5f000000).centerAlign().addAnimation(new Animation.FadeIn(0.3), 0));
        bg.dh.current = -bg.h();

        var cull = ui.addWidget(new CullAreaWidget(CENTER, fromBottomLeft(290, -100)).centerAlign().centerAlignY());
        var div = cull.addChild(new Widget.BlankWidget(ZERO, FULL));
        var accumulatedDy = 0;

        for (OptionsList.AbstractEntry row : this.list.children()) {
            if (row instanceof OptionsList.HeaderEntry entry) {
                div.addChild(new RectangleWidget(fromTopLeft(0, accumulatedDy + 25), fromTopLeft(283, 6)).bottomAlignY().mainColor(ModColors.MAIN_COLOR_M.withAlpha(0x3f)).addAnimation(new Animation.FadeIn.FromRight(0.3), (double) accumulatedDy / 1000));
                div.addChild(new TextWidget(fromTopLeft(0, accumulatedDy + 25), ((StringWidget) entry.children().getFirst()).getMessage()).scaled(1.5).noShadow().bottomAlignY().addAnimation(new Animation.FadeIn.FromLeft(0.3), (double) accumulatedDy / 1000));
                accumulatedDy += 30;
                continue;
            }

            if (!(row instanceof OptionsList.Entry entry)) continue;

            for (OptionsList.OptionInstanceWidget option : entry.children) {
                OptionInstance<?> instance = option.optionInstance();
                var dy = accumulatedDy;
                accumulatedDy += 20;

                if (instance.get() instanceof Boolean) {
                    var setting = new OptionsSubScreenWidgets.SettingSwitchWidget(fromTopRight(0, dy + 2), fromTopLeft(120, 16), (OptionInstance<Boolean>) instance);
                    div.addChild(setting.rightAlign().addAnimation(new Animation.FadeIn.FromRight(0.3), (double) dy / 1000));

                    div.addChild(new OptionsSubScreenWidgets.OptionsTitleWidget(fromTopLeft(0, dy + 2), fromTopLeft(16, 16), () -> {
                        commandQueue.remove(instance);
                        setting.val = setting.instance.get();
                        setting.highlight.dx.set(setting.val ? (double) setting.w() / 2 : 0);
                    }, setting, commandQueue, instance).addAnimation(new Animation.FadeIn.FromLeft(0.3), (double) dy / 1000));
                } else if (OptionsSubScreenWidgets.enumValues(instance) instanceof List<?> values) {
                    var setting = new OptionsSubScreenWidgets.SettingEnumWidget(fromTopRight(0, dy + 2), fromTopLeft(120, 16), instance, values);
                    div.addChild(setting.rightAlign().addAnimation(new Animation.FadeIn.FromRight(0.3), (double) dy / 1000));

                    div.addChild(new OptionsSubScreenWidgets.OptionsTitleWidget(fromTopLeft(0, dy + 2), fromTopLeft(16, 16), () -> {
                        commandQueue.remove(instance);
                        setting.selectionIndex = setting.values.indexOf(instance.get());
                        ((TextWidget) setting.text).setText(setting.displayText(instance.get()));
                    }, setting, commandQueue, instance).addAnimation(new Animation.FadeIn.FromLeft(0.3), (double) dy / 1000));
                } else if (instance.get() instanceof Double) {
                    var setting = new OptionsSubScreenWidgets.SettingDoubleRangeWidget(fromTopRight(0, dy + 2), fromTopLeft(120, 16), instance);
                    div.addChild(setting.rightAlign().addAnimation(new Animation.FadeIn.FromRight(0.3), (double) dy / 1000));

                    div.addChild(new OptionsSubScreenWidgets.OptionsTitleWidget(fromTopLeft(0, dy + 2), fromTopLeft(16, 16), () -> {
                        setting.current = setting.instance.get();
                        setting.text.setText(OptionsSubScreenWidgets.displayText(setting.instance, setting.current));
                        commandQueue.remove(instance);
                    }, setting, commandQueue, instance).addAnimation(new Animation.FadeIn.FromLeft(0.3), (double) dy / 1000));
                }
            }
        }

        if (accumulatedDy > cull.h()) {
            cull.addChild(new ScrollRegionWidget(ZERO, FULL, fromTopLeft(-accumulatedDy + cull.h(), 0), 20, div.dy));
            cull.addChild(new ScrollbarWidget(fromTopRight(0, 0), fromBottomLeft(4, 0), fromTopLeft(-accumulatedDy + cull.h(), 0), div.dy, "y").reverseDirection().rightAlign().addAnimation(new Animation.FadeIn.FromRight(0.3), 0.3));
            div.dw.set(-7);
        }

        // 页面标题与分割线
        ui.addWidget(new RectangleWidget(fromCenterTop(0, 35), fromTopLeft(290, 10)).centerAlign().mainColor(ModColors.MAIN_COLOR_M.withAlpha(0x7f)).addAnimation(new Animation.FadeIn.FromTop(0.3), 0.2));
        ui.addWidget(new TextWidget(fromCenterTop(-145, 45), getScreen().getTitle()).scaled(2).noShadow().bottomAlignY().addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.2));
        ui.addWidget(new RectangleWidget(fromCenterTop(0, 47), fromTopLeft(290, 1)).centerAlign().addAnimation(new Animation.FadeIn.FromTop(0.3), 0.1));
        ui.addWidget(new RectangleWidget(fromCenterBottom(0, -48), fromTopLeft(290, 1)).centerAlign().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0));

        // 保存退出按钮
        ui.addWidget(new TextButtonWidget(fromCenterBottom(-55, -30), fromTopLeft(90, 20), Component.translatable("gui.options.apply_changes"), () -> {
            for (Runnable i : commandQueue.values()) i.run();
            commandQueue.clear();
        }).mainColor(0x3fffffff).bgColor(0x7f000000).rightAlign().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.2));

        ui.addWidget(new TextButtonWidget(fromCenterBottom(0, -30), fromTopLeft(90, 20), Component.translatable("gui.options.discard_and_exit"), () -> {
            commandQueue.clear();
            playQuitAnimationAndQuit();
        }).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)).bgColor(0x7f000000).textColor(ModColors.TEXT_COLOR_T).centerAlign().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.1));

        ui.addWidget(new TextButtonWidget(fromCenterBottom(55, -30), fromTopLeft(90, 20), Component.translatable("gui.options.save_and_exit"), () -> {
            for (Runnable i : commandQueue.values()) i.run();
            commandQueue.clear();
            playQuitAnimationAndQuit();
        }).mainColor(0x3fffffff).bgColor(0x7f000000).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.2));
    }
}
