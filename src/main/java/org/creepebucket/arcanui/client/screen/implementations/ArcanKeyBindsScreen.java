package org.creepebucket.arcanui.client.screen.implementations;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.creepebucket.arcanui.ModColors;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.creepebucket.arcanui.client.screen.lib.ArcanUiRuntime;
import org.creepebucket.creepebucketlib.lib.api.Animation;
import org.creepebucket.creepebucketlib.lib.api.Widget;
import org.creepebucket.creepebucketlib.lib.widgets.CullAreaWidget;
import org.creepebucket.creepebucketlib.lib.widgets.RectangleWidget;
import org.creepebucket.creepebucketlib.lib.widgets.ScrollRegionWidget;
import org.creepebucket.creepebucketlib.lib.widgets.ScrollbarWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextButtonWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextWidget;

import static org.creepebucket.creepebucketlib.lib.api.Coordinate.CENTER;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.FULL;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.ZERO;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromBottomLeft;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromCenterBottom;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromCenterTop;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromTopLeft;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromTopRight;

public class ArcanKeyBindsScreen extends KeyBindsScreen implements ArcanScreen {
    public final ArcanUiRuntime ui = new ArcanUiRuntime();
    public final Map<KeyMapping, TextButtonWidget> bindingButtons = new LinkedHashMap<>();
    public InputConstants.Key pendingMouse;
    public KeyMapping lastSelectedKey;

    public ArcanKeyBindsScreen(Screen lastScreen, Options options) {
        super(lastScreen, options);
    }

    @Override
    public ArcanUiRuntime ui() {
        return this.ui;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.selectedKey == null;
    }

    // 等待绑定时吞掉整次点击，鼠标键统一到 mouseReleased 里落定
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.selectedKey == null) return super.mouseClicked(event, doubleClick);

        this.pendingMouse = InputConstants.Type.MOUSE.getOrCreate(event.button());
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.pendingMouse == null) return super.mouseReleased(event);

        this.selectedKey.setKey(this.pendingMouse);
        this.pendingMouse = null;
        this.selectedKey = null;
        return true;
    }

    // 绑定完成后刷新按钮上的按键文本
    @Override
    public void tick() {
        super.tick();
        if (this.lastSelectedKey == this.selectedKey) return;

        this.lastSelectedKey = this.selectedKey;
        this.refreshBindings();
    }

    @Override
    public void buildUi(ArcanUiRuntime ui) {
        this.bindingButtons.clear();

        var bg = ui.addWidget(new RectangleWidget(fromCenterTop(0, 0), fromBottomLeft(310, 0)).mainColor(0x5f000000).centerAlign().addAnimation(new Animation.FadeIn(0.3), 0));
        bg.dh.current = -bg.h();

        var cull = ui.addWidget(new CullAreaWidget(CENTER, fromBottomLeft(290, -100)).centerAlign().centerAlignY());
        var div = cull.addChild(new Widget.BlankWidget(ZERO, FULL));
        var accumulatedDy = 0;

        var keyMappings = this.minecraft.options.keyMappings.clone();
        Arrays.sort(keyMappings);
        KeyMapping.Category category = null;

        for (KeyMapping key : keyMappings) {
            // 检测是否是新类别
            if (key.getCategory() != category) {
                category = key.getCategory();
                div.addChild(new RectangleWidget(fromTopLeft(0, accumulatedDy + 25), fromTopLeft(283, 6)).bottomAlignY().mainColor(ModColors.MAIN_COLOR_M.withAlpha(0x3f)).addAnimation(new Animation.FadeIn.FromRight(0.3), (double) accumulatedDy / 1000));
                div.addChild(new TextWidget(fromTopLeft(0, accumulatedDy + 25), category.label()).scaled(1.5).noShadow().bottomAlignY().addAnimation(new Animation.FadeIn.FromLeft(0.3), (double) accumulatedDy / 1000));
                accumulatedDy += 30;
            }

            var dy = accumulatedDy;
            accumulatedDy += 20;

            // 键位布局
            div.addChild(new TextWidget(fromTopLeft(0, dy + 10), key.getDisplayName()).noShadow().centerAlignY().addAnimation(new Animation.FadeIn.FromLeft(0.3), (double) dy / 1000));

            var binding = new TextButtonWidget(fromTopRight(-40, dy + 2), fromTopLeft(100, 16), this.bindingText(key), () -> {
                if (this.selectedKey != null) return;
                this.selectedKey = key;
            });
            this.bindingButtons.put(key, binding);
            div.addChild(binding.rightAlign().mainColor(0x3fffffff).bgColor(0x7f000000).addAnimation(new Animation.FadeIn.FromRight(0.3), (double) dy / 1000));

            div.addChild(new TextButtonWidget(fromTopRight(0, dy + 2), fromTopLeft(35, 16), Component.translatable("controls.reset"), () -> {
                key.setToDefault();
                this.refreshBindings();
            }).rightAlign().mainColor(0x3fffffff).bgColor(0x7f000000).addAnimation(new Animation.FadeIn.FromRight(0.3), (double) dy / 1000));
        }

        if (accumulatedDy > cull.h()) {
            cull.addChild(new ScrollRegionWidget(ZERO, FULL, fromTopLeft(-accumulatedDy + cull.h(), 0), 20, div.dy));
            cull.addChild(new ScrollbarWidget(fromTopRight(0, 0), fromBottomLeft(4, 0), fromTopLeft(-accumulatedDy + cull.h(), 0), div.dy, "y").reverseDirection().rightAlign().addAnimation(new Animation.FadeIn.FromRight(0.3), 0.3));
            div.dw.set(-7);
        }

        // 页面标题与分割线
        ui.addWidget(new RectangleWidget(fromCenterTop(0, 35), fromTopLeft(290, 10)).centerAlign().mainColor(ModColors.MAIN_COLOR_M.withAlpha(0x7f)).addAnimation(new Animation.FadeIn.FromTop(0.3), 0.2));
        ui.addWidget(new TextWidget(fromCenterTop(-145, 45), getTitle()).scaled(2).noShadow().bottomAlignY().addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.2));
        ui.addWidget(new RectangleWidget(fromCenterTop(0, 47), fromTopLeft(290, 1)).centerAlign().addAnimation(new Animation.FadeIn.FromTop(0.3), 0.1));
        ui.addWidget(new RectangleWidget(fromCenterBottom(0, -48), fromTopLeft(290, 1)).centerAlign().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0));

        // 重置全部与退出
        ui.addWidget(new TextButtonWidget(fromCenterBottom(-55, -30), fromTopLeft(90, 20), Component.translatable("controls.resetAll"), () -> {
            for (KeyMapping key : this.minecraft.options.keyMappings) {
                key.setToDefault();
            }

            this.refreshBindings();
        }).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)).bgColor(0x7f000000).textColor(ModColors.TEXT_COLOR_T).centerAlign().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.1));

        ui.addWidget(new TextButtonWidget(fromCenterBottom(55, -30), fromTopLeft(90, 20), CommonComponents.GUI_DONE, this::playQuitAnimationAndQuit).mainColor(0x3fffffff).bgColor(0x7f000000).centerAlign().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.2));
    }

    public Component bindingText(KeyMapping key) {
        if (key != this.selectedKey) return key.getTranslatedKeyMessage();

        return Component.literal("> ").append(key.getTranslatedKeyMessage()).append(" <");
    }

    public void refreshBindings() {
        for (var entry : this.bindingButtons.entrySet()) {
            ((TextWidget) entry.getValue().textWidget).setText(this.bindingText(entry.getKey()));
        }
    }
}
