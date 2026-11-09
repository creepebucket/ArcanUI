package org.creepebucket.arcanui.client.screen.implementations;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonLinks;
import net.neoforged.neoforge.client.gui.ModListScreen;
import org.creepebucket.arcanui.ModColors;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.creepebucket.arcanui.client.screen.lib.ArcanUiRuntime;
import org.creepebucket.creepebucketlib.lib.api.Animation;
import org.creepebucket.creepebucketlib.lib.api.Widget;
import org.creepebucket.creepebucketlib.lib.widgets.RectangleWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextButtonWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextWidget;

import static org.creepebucket.creepebucketlib.lib.api.Coordinate.*;

public class ArcanPauseScreen extends PauseScreen implements ArcanScreen {
    public ArcanUiRuntime ui = new ArcanUiRuntime();

    public ArcanPauseScreen(boolean showPauseMenu) {
        super(showPauseMenu);
    }

    @Override
    public ArcanUiRuntime ui() {
        return this.ui;
    }

    @Override
    public void buildUi(ArcanUiRuntime ui) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!showsPauseMenu()) {
            ui.addWidget(new TextWidget(fromCenterTop(0, 10), getTitle()).noShadow().centerAlign().addAnimation(new Animation.FadeIn(0.3), 0));
            return;
        }

        // 背景遮罩
        var bg = ui.addWidget(new RectangleWidget(CENTER, fromTopRight(0, 250)).mainColor(0x3f000000).centerAlign().centerAlignY());
        bg.dw.current = -bg.w();

        var menu = ui.addWidget(new Widget.BlankWidget(CENTER, fromTopLeft(204, 140)).centerAlign().centerAlignY());

        // 标题
        menu.addChild(new TextWidget(fromTopLeft(102, -34), getTitle()).scaled(2).noShadow().centerAlign().addAnimation(new Animation.FadeIn.FromTop(0.3), 0));
        menu.addChild(new RectangleWidget(fromTopLeft(0, -6), fromTopLeft(204, 1)).addAnimation(new Animation.FadeIn.FromTop(0.3), 0.1));

        // 返回游戏
        menu.addChild(new TextButtonWidget(fromTopLeft(0, 0), fromTopLeft(204, 20), Component.translatable("menu.returnToGame"),
                this::playQuitAnimationAndQuit).mainColor(ModColors.MAIN_COLOR_M.withAlpha(0x7f)).bgColor(0x7f000000).textColor(ModColors.TEXT_COLOR_M).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0));

        // 进度与统计
        menu.addChild(new TextButtonWidget(fromTopLeft(0, 24), fromTopLeft(100, 20), Component.translatable("gui.advancements"),
                playQuitAnimationAndDo(() -> minecraft.setScreen(new AdvancementsScreen(minecraft.player.connection.getAdvancements(), this))))
                .bgColor(0x7f000000).mainColor(0x3fffffff).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.05));

        menu.addChild(new TextButtonWidget(fromTopLeft(104, 24), fromTopLeft(100, 20), Component.translatable("gui.stats"),
                playQuitAnimationAndDo(() -> minecraft.setScreen(new StatsScreen(this, minecraft.player.getStats()))))
                .bgColor(0x7f000000).mainColor(0x3fffffff).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.05));

        // 反馈与报告漏洞
        menu.addChild(new TextButtonWidget(fromTopLeft(0, 48), fromTopLeft(100, 20), Component.translatable("menu.sendFeedback"),
                () -> ConfirmLinkScreen.confirmLinkNow(this, SharedConstants.getCurrentVersion().stable() ? CommonLinks.RELEASE_FEEDBACK : CommonLinks.SNAPSHOT_FEEDBACK))
                .bgColor(0x7f000000).mainColor(0x3fffffff).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.1));

        var reportBugs = new TextButtonWidget(fromTopLeft(104, 48), fromTopLeft(100, 20), Component.translatable("menu.reportBugs"),
                () -> ConfirmLinkScreen.confirmLinkNow(this, CommonLinks.SNAPSHOT_BUGS_FEEDBACK));
        if (SharedConstants.getCurrentVersion().dataVersion().isSideSeries()) reportBugs.disable();
        menu.addChild(reportBugs.bgColor(0x7f000000).mainColor(0x3fffffff).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.1));

        // 选项与分享/举报
        menu.addChild(new TextButtonWidget(fromTopLeft(0, 72), fromTopLeft(100, 20), Component.translatable("menu.options"),
                playQuitAnimationAndDo(() -> minecraft.setScreen(new OptionsScreen(this, minecraft.options, true))))
                .bgColor(0x7f000000).mainColor(0x3fffffff).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.15));

        boolean shareToLan = minecraft.hasSingleplayerServer() && !minecraft.getSingleplayerServer().isPublished();
        menu.addChild(new TextButtonWidget(fromTopLeft(104, 72), fromTopLeft(100, 20), Component.translatable(shareToLan ? "menu.shareToLan" : "menu.playerReporting"),
                playQuitAnimationAndDo(() -> minecraft.setScreen(shareToLan ? new ShareToLanScreen(this) : new SocialInteractionsScreen(this))))
                .bgColor(0x7f000000).mainColor(0x3fffffff).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.15));

        // 模组列表
        menu.addChild(new TextButtonWidget(fromTopLeft(0, 96), fromTopLeft(204, 20), Component.translatable("fml.menu.mods"),
                playQuitAnimationAndDo(() -> minecraft.setScreen(new ModListScreen(this))))
                .bgColor(0x7f000000).mainColor(0x3fffffff).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.2));

        // 断开连接
        menu.addChild(new TextButtonWidget(fromTopLeft(0, 120), fromTopLeft(204, 20), CommonComponents.disconnectButtonLabel(minecraft.isLocalServer()),
                playQuitAnimationAndDo(() -> minecraft.getReportingContext().draftReportHandled(minecraft, this,
                        () -> minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE), true)))
                .mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)).bgColor(0x7f000000).textColor(ModColors.TEXT_COLOR_T)
                .addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.25));
    }
}
