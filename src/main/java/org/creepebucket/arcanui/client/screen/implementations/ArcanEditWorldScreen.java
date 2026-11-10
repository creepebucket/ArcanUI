package org.creepebucket.arcanui.client.screen.implementations;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.OptimizeWorldScreen;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.FileUtil;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;
import org.creepebucket.arcanui.ModColors;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.creepebucket.arcanui.client.screen.lib.ArcanUiRuntime;
import org.creepebucket.creepebucketlib.lib.api.Animation;
import org.creepebucket.creepebucketlib.lib.api.Widget;
import org.creepebucket.creepebucketlib.lib.widgets.InputBoxWidget;
import org.creepebucket.creepebucketlib.lib.widgets.RectangleWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextButtonWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextWidget;

import static org.creepebucket.creepebucketlib.lib.api.Coordinate.*;

public class ArcanEditWorldScreen extends Screen implements ArcanScreen {
    public ArcanUiRuntime ui = new ArcanUiRuntime();
    public BooleanConsumer callback;
    public LevelStorageSource.LevelStorageAccess levelAccess;
    public String worldName;
    public InputBoxWidget nameEdit;
    public TextButtonWidget saveButton;
    public TextButtonWidget resetIconButton;

    public ArcanEditWorldScreen(LevelStorageSource.LevelStorageAccess levelAccess, String worldName, BooleanConsumer callback) {
        super(Component.translatable("selectWorld.edit.title"));
        this.levelAccess = levelAccess;
        this.worldName = worldName;
        this.callback = callback;
    }

    @Override
    public ArcanUiRuntime ui() {
        return this.ui;
    }

    @Override
    public void buildUi(ArcanUiRuntime ui) {
        var menu = ui.addWidget(new Widget.BlankWidget(CENTER, fromTopLeft(220, 206)).centerAlign().centerAlignY());
        menu.addChild(new RectangleWidget(ZERO, FULL).mainColor(0x7f000000).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0));

        // 标题与分隔线
        menu.addChild(new TextWidget(fromTopLeft(110, 12), getTitle()).noShadow().centerAlign().addAnimation(new Animation.FadeIn.FromTop(0.3), 0));
        menu.addChild(new RectangleWidget(fromTopLeft(20, 28), fromTopLeft(180, 1)).mainColor(ModColors.MAIN_COLOR_M.withAlpha(0x7f)).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.05));

        // 世界名称
        menu.addChild(new TextWidget(fromTopLeft(20, 36), Component.translatable("selectWorld.enterName")).noShadow().mainColor(0xff808080).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
        nameEdit = new InputBoxWidget(fromTopLeft(20, 46), fromTopLeft(180, 18), this.worldName, 64);
        menu.addChild(nameEdit.mainColor(0x3fffffff).bgColor(0x3f000000).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));

        // 保存 / 取消
        saveButton = (TextButtonWidget) menu.addChild(new TextButtonWidget(fromTopLeft(20, 68), fromTopLeft(86, 18), Component.translatable("selectWorld.edit.save"), playQuitAnimationAndDo(() -> this.onRename(this.nameEdit.box.getValue()))).mainColor(ModColors.MAIN_COLOR_M.withAlpha(0x7f)).bgColor(0x3f000000).textColor(ModColors.TEXT_COLOR_M).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.15));
        menu.addChild(new TextButtonWidget(fromTopLeft(114, 68), fromTopLeft(86, 18), CommonComponents.GUI_CANCEL, playQuitAnimationAndDo(this::onClose)).mainColor(0x3fffffff).bgColor(0x3f000000).addAnimation(new Animation.FadeIn.FromBottom(0.3), 0.15));

        // 图标、文件夹、备份与优化
        resetIconButton = (TextButtonWidget) menu.addChild(new TextButtonWidget(fromTopLeft(20, 90), fromTopLeft(180, 18), Component.translatable("selectWorld.edit.resetIcon"), () -> {
            this.levelAccess.getIconFile().ifPresent(path -> FileUtils.deleteQuietly(path.toFile()));
            this.resetIconButton.addAnimation(new Animation.FadeOut.ToRight(0.3).noDeletion(), 0);
        }).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)).bgColor(0x3f000000).textColor(ModColors.TEXT_COLOR_T).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.1));

        if (this.levelAccess.getIconFile().filter(Files::isRegularFile).isEmpty()) this.resetIconButton.disable();

        menu.addChild(new TextButtonWidget(fromTopLeft(20, 112), fromTopLeft(180, 18), Component.translatable("selectWorld.edit.openFolder"), () -> Util.getPlatform().openPath(this.levelAccess.getLevelPath(LevelResource.ROOT)))
                .mainColor(0x3fffffff).bgColor(0x3f000000).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.15));
        menu.addChild(new TextButtonWidget(fromTopLeft(20, 134), fromTopLeft(180, 18), Component.translatable("selectWorld.edit.backup"), () ->
                EditWorldScreen.makeBackupAndShowToast(this.levelAccess).thenAcceptAsync(success -> this.callback.accept(!success), this.minecraft))
                .mainColor(0x3fffffff).bgColor(0x3f000000).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.2));
        menu.addChild(new TextButtonWidget(fromTopLeft(20, 156), fromTopLeft(180, 18), Component.translatable("selectWorld.edit.backupFolder"), () -> {
            Path path = this.minecraft.getLevelSource().getBackupPath();
            try {
                FileUtil.createDirectoriesSafe(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Util.getPlatform().openPath(path);
        }).mainColor(0x3fffffff).bgColor(0x3f000000).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.3));

        menu.addChild(new TextButtonWidget(fromTopLeft(20, 178), fromTopLeft(180, 18), Component.translatable("selectWorld.edit.optimize"), () ->
                this.minecraft.setScreen(new BackupConfirmScreen(() -> this.minecraft.setScreen(this),
                        (backup, eraseCache) -> EditWorldScreen.conditionallyMakeBackupAndShowToast(backup, this.levelAccess)
                                .thenAcceptAsync(ignored -> this.minecraft.setScreen(OptimizeWorldScreen.create(this.minecraft, this.callback, this.minecraft.getFixerUpper(), this.levelAccess, eraseCache)), this.minecraft),
                        Component.translatable("optimizeWorld.confirm.title"), Component.translatable("optimizeWorld.confirm.description"), Component.translatable("optimizeWorld.confirm.proceed"), true)))
                .mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)).bgColor(0x3f000000).textColor(ModColors.TEXT_COLOR_T).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.35));

        this.nameEdit.box.setResponder(newName -> {
            if (StringUtil.isBlank(newName)) this.saveButton.disable();
            else this.saveButton.enable();
        });
        if (StringUtil.isBlank(this.worldName)) this.saveButton.disable();
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    public void onRename(String newName) {
        try {
            this.levelAccess.renameLevel(newName);
        } catch (NbtException | ReportedNbtException | IOException e) {
            LogUtils.getLogger().error("Failed to access world '{}'", this.levelAccess.getLevelId(), e);
            SystemToast.onWorldAccessFailure(this.minecraft, this.levelAccess.getLevelId());
        }

        this.callback.accept(true);
    }
}
