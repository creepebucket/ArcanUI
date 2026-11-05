package org.creepebucket.arcanui.client.screen.implementations;

import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforge.common.NeoForgeVersion;
import org.creepebucket.arcanui.Arcanui;
import org.creepebucket.arcanui.ModColors;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.creepebucket.arcanui.client.screen.lib.ArcanUiRuntime;
import org.creepebucket.creepebucketlib.lib.api.Animation;
import org.creepebucket.creepebucketlib.lib.widgets.ImageButtonWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextButtonWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextureWidget;

import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromBottomLeft;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromBottomRight;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromCenter;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromCenterTop;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromTopLeft;

public class ArcanTitleScreen extends TitleScreen implements ArcanScreen {
    public final ArcanUiRuntime ui = new ArcanUiRuntime();

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
        Minecraft minecraft = Minecraft.getInstance();
        boolean inIde = SharedConstants.IS_RUNNING_IN_IDE;

        ui.getRoot().mainColor(0x7fffffff);

        // logo
        ui.addWidget(new TextureWidget(fromCenter(0, -110), Identifier.withDefaultNamespace("textures/gui/title/minecraft.png"),
                fromTopLeft(256, 64)).centerAlign().mainColor(0xbfffffff).addAnimation(new Animation.FadeIn.FromTop(0.3), 0));

        // 按钮
        ui.addWidget(new TextButtonWidget(fromCenter(0, -48), fromTopLeft(200, 20), Component.translatable("menu.singleplayer"),
                () -> minecraft.setScreen(new SelectWorldScreen(this))).centerAlign().centerAlignY().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0));

        ui.addWidget(new TextButtonWidget(fromCenter(0, -24), fromTopLeft(200, 20), Component.translatable("menu.multiplayer"),
                () -> minecraft.setScreen(minecraft.options.skipMultiplayerWarning
                        ? new JoinMultiplayerScreen(this) : new SafetyScreen(this))).centerAlign().centerAlignY().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.1));

        ui.addWidget(new TextButtonWidget(fromCenter(0, 0), fromTopLeft(200, 20), Component.translatable("menu.online"),
                () -> minecraft.setScreen(new RealmsMainScreen(this))).centerAlign().centerAlignY().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.2));

        TextButtonWidget mods = new TextButtonWidget(fromCenter(inIde ? 2 : 0, 24), fromTopLeft(inIde ? 98 : 200, 20), Component.translatable("fml.menu.mods"),
                () -> minecraft.setScreen(new ModListScreen(this)));

        if (inIde) {
            mods.leftAlign();
        } else {
            mods.centerAlign();
        }
        ui.addWidget(mods.centerAlignY().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.3));

        if (inIde) ui.addWidget(new TextButtonWidget(fromCenter(-2, 24), fromTopLeft(98, 20), Component.literal("Create Test World"),
                () -> CreateWorldScreen.testWorld(minecraft, () -> minecraft.setScreen(this))).rightAlign().centerAlignY().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.3));

        ui.addWidget(new TextButtonWidget(fromCenter(-2, 60), fromTopLeft(98, 20), Component.translatable("menu.options"),
                () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options, false))).rightAlign().centerAlignY().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.4));

        ui.addWidget(new TextButtonWidget(fromCenter(2, 60), fromTopLeft(98, 20), Component.translatable("menu.quit"),
                minecraft::stop).leftAlign().centerAlignY().addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.4).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0xbf)).textColor(ModColors.TEXT_COLOR_T));

        ui.addWidget(new ImageButtonWidget(fromCenter(-114, 60), fromTopLeft(15, 15), Identifier.withDefaultNamespace("textures/gui/sprites/icon/language.png"),
                Identifier.withDefaultNamespace("textures/gui/sprites/icon/language.png"), () -> minecraft.setScreen(new LanguageSelectScreen(this, minecraft.options, minecraft.getLanguageManager())))
                .centerAlign().centerAlignY().mainColor(0xbfffffff).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.5));

        ui.addWidget(new ImageButtonWidget(fromCenter(114, 60), fromTopLeft(15, 15), Identifier.withDefaultNamespace("textures/gui/sprites/icon/accessibility.png"),
                Identifier.withDefaultNamespace("textures/gui/sprites/icon/accessibility.png"), () -> minecraft.setScreen(new AccessibilityOptionsScreen(this, minecraft.options)))
                .centerAlign().centerAlignY().mainColor(0xbfffffff).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.5));

        // 品牌信息
        ui.addWidget(new TextWidget(fromBottomLeft(2, -10), Component.translatable("fml.menu.branding", "NeoForge " + NeoForgeVersion.getVersion(), ModList.get().size())).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.4));
        ui.addWidget(new TextWidget(fromBottomLeft(2, -10 - this.font.lineHeight - 1), Component.literal("Minecraft " + SharedConstants.getCurrentVersion().name())).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.5));

        // 版权信息
        ui.addWidget(new TextWidget(fromBottomRight(-2, -10 - this.font.lineHeight - 1), Component.literal("ArcanUI " + ModList.get().getModContainerById(Arcanui.MODID).orElseThrow().getModInfo().getVersion())).rightAlign().addAnimation(new Animation.FadeIn.FromRight(0.3), 0.4));
        ui.addWidget(new TextWidget(fromBottomRight(-2, -10), Component.translatable("title.credits")).rightAlign().addClickBehavior(() -> minecraft.setScreen(new CreditsAndAttributionScreen(this))).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.5));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractPanorama(graphics, partialTick);
        this.ui.renderForeground(graphics, mouseX, mouseY, partialTick);
    }
}
