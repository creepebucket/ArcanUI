package org.creepebucket.arcanui.client.screen.implementations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.ChatOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OnlineOptionsScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.options.SkinCustomizationScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.gui.screens.options.WorldOptionsScreen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.telemetry.TelemetryInfoScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.creepebucket.arcanui.ModColors;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.creepebucket.arcanui.client.screen.lib.ArcanUiRuntime;
import org.creepebucket.creepebucketlib.lib.api.Animation;
import org.creepebucket.creepebucketlib.lib.api.Coordinate;
import org.creepebucket.creepebucketlib.lib.api.ThemeTemplate;
import org.creepebucket.creepebucketlib.lib.api.Widget;
import org.creepebucket.creepebucketlib.lib.api.widgets.Lifecycle;
import org.creepebucket.creepebucketlib.lib.widgets.RectangleWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextButtonWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextWidget;

import static java.lang.Math.clamp;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.*;

public class ArcanOptionsMainScreen extends OptionsScreen implements ArcanScreen {
    public static final ThemeTemplate<Widget> BUTTON = new ThemeTemplate<>(w -> {
        w.mainColor(0x3fffffff);
        w.bgColor(0x7f000000);
        return w;
    });

    public ArcanUiRuntime ui = new ArcanUiRuntime();
    public Options gameOptions;
    public boolean inWorld;

    public ArcanOptionsMainScreen(Screen lastScreen, Options gameOptions, boolean inWorld) {
        super(lastScreen, gameOptions, inWorld);
        this.gameOptions = gameOptions;
        this.inWorld = inWorld;
    }

    @Override
    public ArcanUiRuntime ui() {
        return this.ui;
    }

    @Override
    public void buildUi(ArcanUiRuntime ui) {
        Minecraft minecraft = Minecraft.getInstance();
        var div = ui.addWidget(new Widget.BlankWidget(fromCenter(0, -8), fromTopLeft(290, 140)).centerAlign().centerAlignY());

        // 标题与分隔线
        div.addChild(new TextWidget(fromTopLeft(0, -28), getTitle()).scaled(2).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0));
        div.addChild(new RectangleWidget(fromTopLeft(0, -10), fromTopLeft(290, 2)).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0));

        // 选项入口
        // FOV 滑条
        div.addChild(new FovSettingWidget(fromTopLeft(0, 0), fromTopLeft(140, 20)).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.05));

        // 在线 / 世界选项
        if (this.inWorld) {
            div.addChild(new TextButtonWidget(fromTopLeft(150, 0), fromTopLeft(140, 20), Component.translatable("options.worldOptions.button"),
                    playQuitAnimationAndDo(() -> minecraft.setScreen(new WorldOptionsScreen(this, minecraft.level)))).applyTheme(BUTTON).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.05));
        } else {
            div.addChild(new TextButtonWidget(fromTopLeft(150, 0), fromTopLeft(140, 20), Component.translatable("options.online"),
                    playQuitAnimationAndDo(() -> minecraft.setScreen(new OnlineOptionsScreen(this, this.gameOptions)))).applyTheme(BUTTON).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.05));
        }

        div.addChild(new TextButtonWidget(fromTopLeft(0, 24), fromTopLeft(140, 20), Component.translatable("options.skinCustomisation"),
                playQuitAnimationAndDo(() -> Minecraft.getInstance().setScreen(new SkinCustomizationScreen(this, this.gameOptions)))).applyTheme(BUTTON).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
        div.addChild(new TextButtonWidget(fromTopLeft(150, 24), fromTopLeft(140, 20), Component.translatable("options.sounds"),
                playQuitAnimationAndDo(() -> Minecraft.getInstance().setScreen(new SoundOptionsScreen(this, this.gameOptions)))).applyTheme(BUTTON).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
        div.addChild(new TextButtonWidget(fromTopLeft(0, 48), fromTopLeft(140, 20), Component.translatable("options.video"),
                playQuitAnimationAndDo(() -> Minecraft.getInstance().setScreen(new VideoSettingsScreen(this, minecraft, this.gameOptions)))).applyTheme(BUTTON).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.15));
        div.addChild(new TextButtonWidget(fromTopLeft(150, 48), fromTopLeft(140, 20), Component.translatable("options.controls"),
                playQuitAnimationAndDo(() -> Minecraft.getInstance().setScreen(new ControlsScreen(this, this.gameOptions)))).applyTheme(BUTTON).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.15));
        div.addChild(new TextButtonWidget(fromTopLeft(0, 72), fromTopLeft(140, 20), Component.translatable("options.language"),
                playQuitAnimationAndDo(() -> Minecraft.getInstance().setScreen(new LanguageSelectScreen(this, this.gameOptions, minecraft.getLanguageManager())))).applyTheme(BUTTON).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.2));
        div.addChild(new TextButtonWidget(fromTopLeft(150, 72), fromTopLeft(140, 20), Component.translatable("options.chat"),
                playQuitAnimationAndDo(() -> Minecraft.getInstance().setScreen(new ChatOptionsScreen(this, this.gameOptions)))).applyTheme(BUTTON).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.2));
        div.addChild(new TextButtonWidget(fromTopLeft(0, 96), fromTopLeft(140, 20), Component.translatable("options.resourcepack"),
                playQuitAnimationAndDo(() -> Minecraft.getInstance().setScreen(new PackSelectionScreen(
                        minecraft.getResourcePackRepository(), packs -> {
                            this.gameOptions.updateResourcePacks(packs);
                            minecraft.setScreen(this);
                        }, minecraft.getResourcePackDirectory(), Component.translatable("resourcePack.title"))))).applyTheme(BUTTON).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.25));
        div.addChild(new TextButtonWidget(fromTopLeft(150, 96), fromTopLeft(140, 20), Component.translatable("options.accessibility"),
                playQuitAnimationAndDo(() -> Minecraft.getInstance().setScreen(new AccessibilityOptionsScreen(this, this.gameOptions)))).applyTheme(BUTTON).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.25));
        div.addChild(new TextButtonWidget(fromTopLeft(0, 120), fromTopLeft(140, 20), Component.translatable("options.telemetry"),
                playQuitAnimationAndDo(() -> Minecraft.getInstance().setScreen(new TelemetryInfoScreen(this, this.gameOptions)))).applyTheme(BUTTON).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.3));
        div.addChild(new TextButtonWidget(fromTopLeft(150, 120), fromTopLeft(140, 20), Component.translatable("options.credits_and_attribution"),
                playQuitAnimationAndDo(() -> Minecraft.getInstance().setScreen(new CreditsAndAttributionScreen(this)))).applyTheme(BUTTON).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.3));

        // 完成
        div.addChild(new TextButtonWidget(fromTopLeft(0, 150), fromTopLeft(290, 20), CommonComponents.GUI_DONE,
                this::playQuitAnimationAndQuit).applyTheme(BUTTON).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.35));
    }

    public static class FovSettingWidget extends Widget implements Lifecycle {
        public OptionInstance<Integer> instance = Minecraft.getInstance().options.fov();
        public TextWidget text;
        public int current;

        public FovSettingWidget(Coordinate pos, Coordinate size) {
            super(pos, size);
            this.current = instance.get();
        }

        public Component displayText(int value) {
            return Component.literal(instance.caption.getString() + ": ").append(OptionsSubScreenWidgets.displayText(instance, value));
        }

        @Override
        public void onInitialize() {
            addChild(new RectangleWidget(fromTopLeft(2 * h() + 2, 0), fromBottomRight(-4 * h() - 4, 0)).applyTheme(ThemeTemplate.DARK_BG));
            text = new TextWidget(CENTER, displayText(current)).noShadow();
            addChild(text.centerAlign().centerAlignY());

            addChild(new OptionsSubScreenWidgets.HoverableRectWidget(fromTopLeft(0, 0), fromTopLeft(h(), h()), 0x7f000000)).addClickBehavior(
                    () -> {
                        current = 30;
                        instance.set(current);
                        text.setText(displayText(current));
                    }
            );
            addChild(new TextWidget(fromTopLeft(h()/2, h()/2), Component.literal("|<")).noShadow().centerAlignY().centerAlign());
            addChild(new OptionsSubScreenWidgets.HoverableRectWidget(fromTopLeft(h() + 1, 0), fromTopLeft(h(), h()), 0x7f000000)).addClickBehavior(
                    () -> {
                        current = clamp(current - (Minecraft.getInstance().hasShiftDown() ? 10 : 1), 30, 110);
                        instance.set(current);
                        text.setText(displayText(current));
                    }
            );
            addChild(new TextWidget(fromTopLeft(h() + h()/2 + 1, h()/2), Component.literal("<")).noShadow().centerAlignY().centerAlign());

            addChild(new OptionsSubScreenWidgets.HoverableRectWidget(fromTopRight(-h() - 1, 0), fromTopLeft(h(), h()), 0x7f000000)).rightAlign().addClickBehavior(
                    () -> {
                        current = clamp(current + (Minecraft.getInstance().hasShiftDown() ? 10 : 1), 30, 110);
                        instance.set(current);
                        text.setText(displayText(current));
                    }
            );
            addChild(new TextWidget(fromTopRight(-h() - h()/2 - 1, h()/2), Component.literal(">")).noShadow().centerAlignY().centerAlign());
            addChild(new OptionsSubScreenWidgets.HoverableRectWidget(fromTopRight(0, 0), fromTopLeft(h(), h()), 0x7f000000)).rightAlign().addClickBehavior(
                    () -> {
                        current = 110;
                        instance.set(current);
                        text.setText(displayText(current));
                    }
            );
            addChild(new TextWidget(fromTopRight(-h()/2, h()/2), Component.literal(">|")).noShadow().centerAlignY().centerAlign());
        }
    }
}
