package org.creepebucket.arcanui.client.screen.implementations;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;
import org.creepebucket.arcanui.ModColors;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.creepebucket.arcanui.client.screen.lib.ArcanUiRuntime;
import org.creepebucket.creepebucketlib.lib.api.Animation;
import org.creepebucket.creepebucketlib.lib.api.Coordinate;
import org.creepebucket.creepebucketlib.lib.api.Widget;
import org.creepebucket.creepebucketlib.lib.api.widgets.Clickable;
import org.creepebucket.creepebucketlib.lib.api.widgets.Lifecycle;
import org.creepebucket.creepebucketlib.lib.api.widgets.Renderable;
import org.creepebucket.creepebucketlib.lib.ui.WidgetManager;
import org.creepebucket.creepebucketlib.lib.widgets.*;

import static org.creepebucket.creepebucketlib.lib.api.Coordinate.*;

public class ArcanSelectWorldScreen extends SelectWorldScreen implements ArcanScreen {
    public final ArcanUiRuntime ui = new ArcanUiRuntime();
    public CompletableFuture<List<LevelSummary>> future;
    public Widget worldListContent, worldListDiv;
    public WorldDetailWidget worldDetail;
    public InputBoxWidget searchBox;
    public ScrollRegionWidget worldScroll;
    public ScrollbarWidget worldBar;
    public List<WorldItemWidget> worldItems = new ArrayList<>();

    public ArcanSelectWorldScreen(SelectWorldScreen parent) {
        super(parent.lastScreen);
    }

    @Override
    public ArcanUiRuntime ui() {
        return this.ui;
    }

    @Override
    public void buildUi(ArcanUiRuntime ui) {
        this.setFocused(null);
        Minecraft minecraft = Minecraft.getInstance();

        worldItems.clear();
        worldDetail = null;
        future = null;
        worldListDiv = ui.addWidget(new Widget.BlankWidget(CENTER, fromBottomLeft(150, -60)).centerAlign().centerAlignY());
        // 世界选择菜单
        worldListDiv.addChild(new RectangleWidget(fromTopLeft(0, 20), fromBottomRight(0, -40)).mainColor(0x7f000000).addAnimation(new Animation.FadeIn.FromRight(0.3), 0));

        searchBox = new InputBoxWidget(fromTopLeft(0, 0), fromTopRight(0, 18), "", 64);
        worldListDiv.addChild(searchBox.mainColor(0).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
        searchBox.box.setHint(Component.translatable("gui.selectWorld.search").withStyle(EditBox.SEARCH_HINT_STYLE));

        var cull = worldListDiv.addChild(new CullAreaWidget(fromTopLeft(0, 20), fromBottomRight(0, -40)));
        worldListContent = cull.addChild(new Widget.BlankWidget(ZERO, FULL));
        worldScroll = (ScrollRegionWidget) cull.addChild(new ScrollRegionWidget(ZERO, FULL, ZERO, 32, worldListContent.dy));
        worldBar = (ScrollbarWidget) cull.addChild(new ScrollbarWidget(fromTopRight(0, 0), fromBottomLeft(4, 0), ZERO, worldListContent.dy, "y")
                .reverseDirection().rightAlign().addAnimation(new Animation.FadeIn.FromRight(0.3), 0.3));

        refreshWorldList("");
        searchBox.box.setResponder(v -> refreshWorldList(v));

        // 扫描世界
        try {
            LevelStorageSource source = minecraft.getLevelSource();
            future = source.loadLevelSummaries(source.findLevelCandidates());
        } catch (LevelStorageException e) {
            // 读不了 saves 目录
            return;
        }

        // 新的世界按钮
        worldListDiv.addChild(new TextButtonWidget(fromBottomLeft(0, 0), fromTopRight(0, 18), Component.translatable("selectWorld.create"),
                playQuitAnimationAndDo(() -> CreateWorldScreen.openFresh(minecraft, () -> minecraft.setScreen(this)))).bottomAlignY()
                .mainColor(ModColors.MAIN_COLOR_M.withAlpha(0x7f)).bgColor(0x7f000000).textColor(ModColors.TEXT_COLOR_M).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));

        /*

        div.addChild(new ActionButton(fromTopLeft(228, 212), fromTopLeft(72, 20), CommonComponents.GUI_BACK,
                this::playQuitAnimationAndQuit)
                .mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)).bgColor(0x7f000000).textColor(ModColors.TEXT_COLOR_T));

        updateButtons();
        pendingLevels = loadLevels();

         */
    }

    @Override
    public void tick() {
        super.tick();

        if (future != null && future.isDone()) {
            List<LevelSummary> levels = future.join();
            future = null;
            // 在这里添加扫描完成的世界

            var lastPlayed = true;
            WorldItemWidget lastPlayedWorld = null;

            for (LevelSummary summary : levels) {
                var a = worldListContent.addChild(new WorldItemWidget(ZERO, fromTopRight(0, 34), summary, lastPlayed).disable());
                worldItems.add((WorldItemWidget) a);

                if (lastPlayed) lastPlayedWorld = (WorldItemWidget) a;
                lastPlayed = false;
            }

            refreshWorldList(searchBox.box.getValue());

            if (lastPlayedWorld != null) worldDetail = new WorldDetailWidget(fromCenter(80, 0), fromBottomLeft(150, -60), lastPlayedWorld);
            ui().addWidget(worldDetail.centerAlign().centerAlignY().addAnimation(new Animation.FadeIn.FromRight(0.3), 0));

        }
    }

    public void refreshWorldList(String search) {
        var accumulatedDy = 5;

        for (WorldItemWidget widget : worldItems) {
            if (!search.isEmpty() && !widget.summary.getLevelName().toLowerCase(Locale.ROOT).contains(search) && !widget.summary.getLevelId().toLowerCase(Locale.ROOT).contains(search)) {
                widget.animations.clear();
                widget.addAnimation(new Animation.FadeOut(0.3).noDeletion(), 0);
                widget.dy.set(accumulatedDy);
            } else {
                widget.dy.set(accumulatedDy);
                if (!widget.enabled) {
                    widget.animations.clear();
                    widget.enable();
                    widget.addAnimation(new Animation.FadeIn.FromLeft(0.3), (double) accumulatedDy / 1000);
                }
                accumulatedDy += 40;
            }
        }

        if (accumulatedDy > worldListContent.h()) {
            worldScroll.region = fromTopLeft(-accumulatedDy + worldListContent.h(), 0);
            worldBar.region = fromTopLeft(-accumulatedDy + worldListContent.h(), 0);
            worldBar.animations.clear();
            if (!worldBar.enabled) worldBar.addAnimation(new Animation.FadeIn(0.3), 0);
            worldBar.enable();
            worldListContent.dw.set(-3);
        } else {
            worldScroll.region = ZERO;
            worldBar.animations.clear();
            worldBar.addAnimation(new Animation.FadeOut(0.3).noDeletion(), 0);
            worldListContent.dy.set(0);
            worldListContent.dw.set(0);
        }
    }

    public static class WorldItemWidget extends Widget implements Lifecycle, Renderable, Clickable {
        public LevelSummary summary;
        public FaviconTexture icon;
        public Widget hoverHighlight, selectionHighlight;
        public boolean lastPlayed;

        public WorldItemWidget(Coordinate pos, Coordinate size, LevelSummary summary, boolean lastPlayed) {
            super(pos, size);
            this.summary = summary;
            this.lastPlayed = lastPlayed;
        }

        @Override
        public void onInitialize() {
            this.icon = FaviconTexture.forWorld(Minecraft.getInstance().getTextureManager(), summary.getLevelId());
            Path iconFile = summary.getIcon();

            if (icon != null && Files.isRegularFile(iconFile)) {
                try (InputStream stream = Files.newInputStream(iconFile)) {
                    icon.upload(NativeImage.read(stream));
                } catch (Throwable ignored) {
                    icon.clear();
                }
            }

            String id, date = "从未玩过";
            id = "ID: " + summary.getLevelId();
            if (summary.getLastPlayed() != -1L) {
                ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(summary.getLastPlayed()), ZoneId.systemDefault());
                date = lastPlayed ? "上次游玩" : WorldSelectionList.DATE_FORMAT.format(time);
            }

            selectionHighlight = addChild(new RectangleWidget(ZERO, fromBottomLeft(0, 0)).mainColor(ModColors.MAIN_COLOR_M.withAlpha(0x3f)));

            var div = addChild(new BlankWidget(fromTopRight(0, 0), FULL).rightAlign());

            hoverHighlight = addChild(new RectangleWidget(ZERO, fromBottomLeft(0, 0)));
            div.dw = hoverHighlight.dw.multiply(-1);

            var name = div.addChild(new TextWidget(fromTopLeft(5, 3), Component.literal(summary.getLevelName())).noShadow());
            if (w() - name.w() > 10) {
                div.addChild(new RectangleWidget(fromTopRight(-7, 7), fromTopRight(-name.w() - 15, 1)).rightAlign());
                div.addChild(new OutlineWidget(fromTopRight(-5, 6), fromTopLeft(3, 3)).rightAlign());
            }
            div.addChild(new TextWidget(fromTopLeft(5, 13), Component.literal(id)).noShadow().mainColor(0xff808080));
            div.addChild(new TextWidget(fromTopLeft(5, 23), Component.literal(date)).noShadow().mainColor(0xff808080));

            if (lastPlayed) {
                selectionHighlight.dw.set(w());
            }
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (isInBounds(mouseX, mouseY)) {
                hoverHighlight.dw.set(7);
            } else {
                hoverHighlight.dw.set(0);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (!(parent.parent.x() <= event.x() && event.x() <= parent.parent.x() + parent.parent.w() && parent.parent.y() <= event.y() && event.y() <=parent.parent.y() + parent.parent.h())) return false;

            if (isInBounds(event.x(), event.y())) {
                selectionHighlight.dw.set(w());
                var screen = (ArcanSelectWorldScreen) this.screen;
                if (screen.worldDetail != null && screen.worldDetail.worldItem != this) {
                    screen.worldDetail.changeWorldTo(this);
                } else if (screen.worldDetail == null) {
                    screen.worldDetail = new WorldDetailWidget(fromCenter(80, 0), fromBottomLeft(150, -60), this);
                    screen.ui().addWidget(screen.worldDetail.centerAlign().centerAlignY().addAnimation(new Animation.FadeIn.FromRight(0.3), 0));
                }
            } else {
                selectionHighlight.dw.set(0);
            }
            return false;
        }
    }

    public static class WorldDetailWidget extends Widget implements Lifecycle {
        public WorldItemWidget worldItem;
        public Widget icon, playButton, editButton, deleteButton, recreateButton, details;

        public WorldDetailWidget(Coordinate pos, Coordinate size, WorldItemWidget worldItem) {
            super(pos, size);
            this.worldItem = worldItem;
        }

        @Override
        public void onInitialize() {
            if (((ArcanSelectWorldScreen) screen).worldListDiv.dx.target == 0) {
                ((ArcanSelectWorldScreen) screen).worldListDiv.dx.set(-80);
            }

            addChild(new RectangleWidget(ZERO, FULL).mainColor(0x7f000000));
            rebuildDetails(true);
        }

        public void rebuildDetails(boolean buttonAnimations) {
            icon = addChild(new OutlineWidget(fromCenterTop(0, 75), fromTopLeft(136, 136)).centerAlign().centerAlignY().addAnimation(new Animation.FadeIn.FromLeft(0.3), 0));
            icon.addChild(new TextureWidget(CENTER, worldItem.icon.textureLocation(), fromTopLeft(128, 128)).centerAlign().centerAlignY());

            details = addChild(new BlankWidget(ZERO, FULL).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
            details.addChild(new MultilineTextWidget(fromTopLeft(7, 150), fromTopLeft(136, 999), Component.literal(worldItem.summary.getLevelName())).noShadow());
            details.addChild(new MultilineTextWidget(fromTopLeft(7, 163), fromTopLeft(136, 999), worldItem.summary.getInfo()).noShadow());

            // 底部按钮
            var screen = (ArcanSelectWorldScreen) this.screen;
            this.playButton =     addChild(new TextButtonWidget(fromBottomLeft(7, -25), fromTopLeft(136, 18), LevelSummary.PLAY_WORLD,                        screen.playQuitAnimationAndDo(() -> screen.joinWorld(this.worldItem.summary))    ).bgColor(0x3f000000).mainColor(ModColors.MAIN_COLOR_M.withAlpha(0x7f)).textColor(ModColors.TEXT_COLOR_M));
            this.editButton =     addChild(new TextButtonWidget(fromBottomLeft(7, -45), fromTopLeft(136, 18), Component.translatable("selectWorld.edit"),     screen.playQuitAnimationAndDo(() -> screen.editWorld(this.worldItem.summary))    ).bgColor(0x3f000000).mainColor(0x3fffffff));
            this.deleteButton =   addChild(new TextButtonWidget(fromBottomLeft(7, -65), fromTopLeft(136, 18), Component.translatable("selectWorld.delete"),   screen.playQuitAnimationAndDo(() -> screen.deleteWorld(this.worldItem.summary))  ).bgColor(0x3f000000).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)).textColor(ModColors.TEXT_COLOR_T));
            this.recreateButton = addChild(new TextButtonWidget(fromBottomLeft(7, -85), fromTopLeft(136, 18), Component.translatable("selectWorld.recreate"), screen.playQuitAnimationAndDo(() -> screen.recreateWorld(this.worldItem.summary))).bgColor(0x3f000000).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)).textColor(ModColors.TEXT_COLOR_T));

            if (buttonAnimations) {
                this.playButton    .addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.15);
                this.editButton    .addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.20);
                this.deleteButton  .addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.25);
                this.recreateButton.addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.30);
            }
        }

        public void changeWorldTo(WorldItemWidget worldItemWidget) {
            this.worldItem = worldItemWidget;

            icon.addAnimation(new Animation.FadeOut.ToRight(0.3), 0);
            details.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.1);
            playButton    .removeMyself();
            editButton    .removeMyself();
            deleteButton  .removeMyself();
            recreateButton.removeMyself();
            rebuildDetails(false);
        }
    }

    public void joinWorld(LevelSummary summary) {
        if (summary instanceof LevelSummary.SymlinkLevelSummary) {
            minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> minecraft.setScreen(this)));
            return;
        }

        minecraft.createWorldOpenFlows().openWorld(summary.getLevelId(), () -> minecraft.setScreen(this));
    }

    public void editWorld(LevelSummary summary) {
        String levelId = summary.getLevelId();
        minecraft.setScreenAndShow(new ArcanGenericLoadingScreen(Component.translatable("selectWorld.data_read")));

        LevelStorageSource.LevelStorageAccess access;
        try {
            access = minecraft.getLevelSource().validateAndCreateAccess(levelId);
        } catch (IOException e) {
            SystemToast.onWorldAccessFailure(minecraft, levelId);
            minecraft.setScreen(this);
            return;
        } catch (ContentValidationException e) {
            minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> minecraft.setScreen(this)));
            return;
        }

        EditWorldScreen editScreen;
        try {
            editScreen = EditWorldScreen.create(minecraft, access, result -> {
                access.safeClose();
                minecraft.setScreen(this);
            });
        } catch (NbtException | ReportedNbtException | IOException e) {
            access.safeClose();
            SystemToast.onWorldAccessFailure(minecraft, levelId);
            minecraft.setScreen(this);
            return;
        }

        minecraft.setScreen(editScreen);
    }

    public void deleteWorld(LevelSummary summary) {
        minecraft.setScreen(new ConfirmScreen(result -> {
            if (result) {
                minecraft.setScreen(new ProgressScreen(true));
                try (LevelStorageSource.LevelStorageAccess access = minecraft.getLevelSource().createAccess(summary.getLevelId())) {
                    access.deleteLevel();
                } catch (IOException e) {
                    SystemToast.onWorldDeleteFailure(minecraft, summary.getLevelId());
                }
            }

            minecraft.setScreen(this);
        }, Component.translatable("selectWorld.deleteQuestion"),
                Component.translatable("selectWorld.deleteWarning", summary.getLevelName()),
                Component.translatable("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));
    }

    public void recreateWorld(LevelSummary summary) {
        minecraft.setScreen(new ArcanGenericLoadingScreen(Component.translatable("selectWorld.data_read")));

        try (LevelStorageSource.LevelStorageAccess access = minecraft.getLevelSource().validateAndCreateAccess(summary.getLevelId())) {
            Pair<LevelSettings, WorldCreationContext> recreated = minecraft.createWorldOpenFlows().recreateWorldData(access);
            LevelSettings settings = recreated.getFirst();
            WorldCreationContext context = recreated.getSecond();
            Path dataPackDir = CreateWorldScreen.createTempDataPackDirFromExistingWorld(
                    access.getLevelPath(LevelResource.DATAPACK_DIR), minecraft);
            context.validate();

            if (context.options().isOldCustomizedWorld()) {
                minecraft.setScreen(new ConfirmScreen(result -> minecraft.setScreen(result
                        ? CreateWorldScreen.createFromExisting(minecraft, () -> minecraft.setScreen(this), settings, context, dataPackDir)
                        : this),
                        Component.translatable("selectWorld.recreate.customized.title"),
                        Component.translatable("selectWorld.recreate.customized.text"),
                        CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
            } else {
                minecraft.setScreen(CreateWorldScreen.createFromExisting(minecraft, () -> minecraft.setScreen(this), settings, context, dataPackDir));
            }
        } catch (ContentValidationException e) {
            minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> minecraft.setScreen(this)));
        } catch (Exception e) {
            minecraft.setScreen(new AlertScreen(() -> minecraft.setScreen(this),
                    Component.translatable("selectWorld.recreate.error.title"),
                    Component.translatable("selectWorld.recreate.error.text")));
        }
    }
}
