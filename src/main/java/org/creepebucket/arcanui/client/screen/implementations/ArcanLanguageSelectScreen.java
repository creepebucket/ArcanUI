package org.creepebucket.arcanui.client.screen.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import org.creepebucket.arcanui.ModColors;
import org.creepebucket.creepebucketlib.lib.api.widgets.Clickable;
import org.creepebucket.creepebucketlib.lib.api.widgets.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.FontOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.creepebucket.arcanui.client.screen.lib.ArcanUiRuntime;
import org.creepebucket.creepebucketlib.lib.api.Animation;
import org.creepebucket.creepebucketlib.lib.api.Coordinate;
import org.creepebucket.creepebucketlib.lib.api.Widget;
import org.creepebucket.creepebucketlib.lib.api.widgets.Lifecycle;
import org.creepebucket.creepebucketlib.lib.ui.WidgetManager;
import org.creepebucket.creepebucketlib.lib.widgets.*;

import static java.lang.Math.clamp;
import static java.lang.Math.max;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.*;

public class ArcanLanguageSelectScreen extends LanguageSelectScreen implements ArcanScreen {
    public final ArcanUiRuntime ui = new ArcanUiRuntime();
    public final LanguageManager languageManager;
    public String selectedLanguage;

    public ArcanLanguageSelectScreen(Screen lastScreen, Options options, LanguageManager languageManager) {
        super(lastScreen, options, languageManager);

        this.languageManager = languageManager;
        this.selectedLanguage = languageManager.getSelected();
    }

    @Override
    public ArcanUiRuntime ui() {
        return this.ui;
    }

    @Override
    public void buildUi(ArcanUiRuntime ui) {
        this.setFocused(null);

        var div = ui.addWidget(new Widget.BlankWidget(fromCenter(0, 0), fromBottomLeft(300, -50)).centerAlign().centerAlignY());

        // 搜索
        var searchbox = new InputBoxWidget(fromTopLeft(0, 0), fromTopRight(0, 15), "", 32);
        div.addChild(searchbox.mainColor(0).addAnimation(new Animation.FadeIn.FromRight(0.3), 0));
        searchbox.box.setHint(Component.translatable("gui.language.search").withStyle(EditBox.SEARCH_HINT_STYLE));

        // 语言列表
        var cull = div.addChild(new CullAreaWidget(fromTopLeft(0, 25), fromBottomRight(0, -55)).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
        cull.addChild(new RectangleWidget(ZERO, FULL).mainColor(0x7f000000));
        var list = cull.addChild(new Widget.BlankWidget(ZERO, FULL));
        var scroll = new ScrollRegionWidget(ZERO, FULL, ZERO, 16, list.dy);
        cull.addChild(scroll);
        var bar = new ScrollbarWidget(fromTopRight(-4, 0), fromBottomLeft(4, 0), ZERO, list.dy, "y").reverseDirection();
        cull.addChild(bar);
        List<LanguageItemWidget> items = new ArrayList<>();

        for (var entry : this.languageManager.getLanguages().entrySet()) {
            var item = new LanguageItemWidget(ZERO, fromTopRight(0, 18), entry.getKey(), entry.getValue());
            items.add(item);
            list.addChild(item.disable());
        }

        refreshLanguageList("", items, list, scroll, bar);
        searchbox.box.setResponder(v -> refreshLanguageList(v, items, list, scroll, bar));

        // 按钮
        div.addChild(new TextButtonWidget(fromBottomLeft(0, -20), fromCenterTop(-5, 20), Component.translatable("options.font"),
                playQuitAnimationAndDo(() -> this.minecraft.setScreen(new FontOptionsScreen(this, this.options)))).mainColor(0x7fffffff).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.2));

        div.addChild(new TextButtonWidget(fromBottomRight(0, -20), fromCenterTop(-5, 20), CommonComponents.GUI_DONE,
                playQuitAnimationAndDo(() -> {
                    if (!selectedLanguage.equals(this.languageManager.getSelected())) {
                        this.languageManager.setSelected(selectedLanguage);
                        this.options.languageCode = selectedLanguage;
                        this.minecraft.reloadResourcePacks();
                    }

                    this.minecraft.setScreen(this.lastScreen);
                })).rightAlign().mainColor(0x7fffffff).addAnimation(new Animation.FadeIn.FromRight(0.3), 0.2));
    }

    public static class LanguageItemWidget extends Widget implements Lifecycle, Renderable, Clickable {
        String language;
        LanguageInfo languageInfo;
        RectangleWidget hoverHighlight, selectionHighlight;

        public LanguageItemWidget(Coordinate pos, Coordinate size, String language, LanguageInfo languageInfo) {
            super(pos, size);
            this.language = language;
            this.languageInfo = languageInfo;
        }

        @Override
        public void onInitialize() {
            hoverHighlight = new RectangleWidget(ZERO, fromBottomLeft(0, 0));
            selectionHighlight = new RectangleWidget(ZERO, fromBottomLeft(0, 0));
            addChild(selectionHighlight.mainColor(ModColors.MAIN_COLOR_M.withAlpha(0x3f)));
            addChild(hoverHighlight);
            hoverHighlight.addChild(new TextWidget(fromCenterRight(5, 0), languageInfo.toComponent()).noShadow().centerAlignY());
        }

        @Override
        public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mx, int my, float pt) {
            if (isInBounds(mx, my) && parent.parent.x() <= mx && mx <= parent.parent.x() + parent.parent.w() && parent.parent.y() <= my && my <=parent.parent.y() + parent.parent.h()) {
                hoverHighlight.dw.set(7);
            } else {
                hoverHighlight.dw.set(0);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
            if (!(parent.parent.x() <= event.x() && event.x() <= parent.parent.x() + parent.parent.w() && parent.parent.y() <= event.y() && event.y() <=parent.parent.y() + parent.parent.h())) return false;

            if (isInBounds(event.x(), event.y())) {
                selectionHighlight.dw.set(w());
                ((ArcanLanguageSelectScreen) screen).selectedLanguage = language;
                return true;
            } else {
                selectionHighlight.dw.set(0);
                return false;
            }
        }
    }

    public void refreshLanguageList(String search, List<LanguageItemWidget> items, Widget list, ScrollRegionWidget region, ScrollbarWidget bar) {
        var accumulatedDy = 0;

        for (LanguageItemWidget widget : items) {
            if (!search.isEmpty() && !widget.languageInfo.name().toLowerCase(Locale.ROOT).contains(search) && !widget.languageInfo.region().toLowerCase(Locale.ROOT).contains(search)) {
                widget.animations.clear();
                widget.addAnimation(new Animation.FadeOut(0.3).noDeletion(), 0);
                widget.dy.set(accumulatedDy);
            } else {
                widget.dy.set(accumulatedDy);
                if (!widget.enabled) {
                    widget.animations.clear();
                    widget.enable();
                    widget.addAnimation(new Animation.FadeIn.FromRight(0.3), 0);
                }
                accumulatedDy += 18;
            }
        }

        if (accumulatedDy > region.h()) {
            region.region = fromTopLeft(-accumulatedDy + region.h(), 0);
            bar.region = fromTopLeft(-accumulatedDy + region.h(), 0);
            bar.animations.clear();
            if (!bar.enabled) bar.addAnimation(new Animation.FadeIn(0.3), 0);
            bar.enable();
        } else {
            region.region = ZERO;
            bar.animations.clear();
            bar.addAnimation(new Animation.FadeOut(0.3).noDeletion(), 0);
            list.dy.set(-max(accumulatedDy - region.h(), 0));
        }
    }

}
