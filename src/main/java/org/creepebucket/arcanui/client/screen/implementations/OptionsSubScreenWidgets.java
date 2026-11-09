package org.creepebucket.arcanui.client.screen.implementations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.creepebucket.arcanui.ModColors;
import org.creepebucket.arcanui.client.screen.lib.ArcanOptionsScreen;
import org.creepebucket.creepebucketlib.lib.api.*;
import org.creepebucket.creepebucketlib.lib.api.widgets.Clickable;
import org.creepebucket.creepebucketlib.lib.api.widgets.Lifecycle;
import org.creepebucket.creepebucketlib.lib.api.widgets.Tickable;
import org.creepebucket.creepebucketlib.lib.widgets.RectangleWidget;
import org.creepebucket.creepebucketlib.lib.widgets.TextWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.Math.clamp;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.*;

public class OptionsSubScreenWidgets {
    public static <T> Component displayText(OptionInstance<T> instance, T value) {
        return displayText(instance.caption, instance.toString.apply(value));
    }

    public static Component displayText(Component caption, Component text) {
        String title = caption.getString();
        String full = text.getString();
        return Component.literal(full.startsWith(title) ? full.substring(title.length()).replaceFirst("^\\s*[:：]\\s*", "") : full);
    }

    private static <T> void queueOptionChange(Screen screen, OptionInstance<T> instance, T value) {
        var commandQueue = ((ArcanOptionsScreen) screen).commandQueue();

        if (value.equals(instance.get())) {
            commandQueue.remove(instance);
            return;
        }

        commandQueue.put(instance, () -> {
            instance.set(value);
            ((OptionsSubScreen) (Object) screen).resetOption(instance);
        });
    }

    public static <T> void queueCycleChange(Screen screen, CycleButton<T> cycle, T value) {
        var commandQueue = ((ArcanOptionsScreen) screen).commandQueue();

        if (value.equals(cycle.getValue())) {
            commandQueue.remove(cycle);
            return;
        }

        commandQueue.put(cycle, () -> {
            cycle.setValue(value);
            cycle.onValueChange.onValueChange(cycle, value);
        });
    }

    @SuppressWarnings("unchecked")
    public static List<?> enumValues(OptionInstance<?> instance) {
        if (instance.values() instanceof OptionInstance.Enum<?> e) return e.values();
        if (instance.values() instanceof OptionInstance.SliderableEnum<?> e) return e.values();
        if (instance.values() instanceof OptionInstance.LazyEnum<?> e) return e.values().get();
        if (instance.values() instanceof OptionInstance.AltEnum<?> e) return e.altCondition().getAsBoolean() ? e.altValues() : e.values();
        if (instance.values() instanceof OptionInstance.IntRange r) return IntStream.rangeClosed(r.minInclusive(), r.maxInclusive()).boxed().toList();
        if (instance.values() instanceof OptionInstance.ClampingLazyMaxIntRange r) return IntStream.rangeClosed(r.minInclusive(), r.maxInclusive()).boxed().toList();
        if (instance.values() instanceof OptionInstance.SliderableValueSet<?> s) {
            var set = (OptionInstance.SliderableValueSet<Object>) s;
            var values = new ArrayList<Object>();
            values.add(instance.get());

            for (Object value = instance.get(); ; ) {
                var next = set.next(value);
                if (next.isEmpty() || next.get().equals(value)) break;
                value = next.get();
                values.add(value);
            }

            for (Object value = instance.get(); ; ) {
                var previous = set.previous(value);
                if (previous.isEmpty() || previous.get().equals(value)) break;
                value = previous.get();
                values.addFirst(value);
            }

            return values.size() > 1 ? values : null;
        }
        return null;
    }

    public static class OptionsTitleWidget extends HoverableRectWidget implements Tickable, Clickable, Lifecycle {
        public Runnable reset;
        public Widget resetText, target, title;
        public Component caption;
        public Object key;
        public boolean shown;
        public Map<Object, Runnable> commandQueue;

        public OptionsTitleWidget(Coordinate pos, Coordinate size, Runnable reset, Widget target, Map<Object, Runnable> commandQueue, Component caption, Object key) {
            super(pos, size, 0x3fffffff);
            this.reset = reset;
            this.target = target;
            this.commandQueue = commandQueue;
            this.caption = caption;
            this.key = key;
        }

        @Override
        public void tick() {
            boolean pending = commandQueue.containsKey(key);
            if (pending == shown) return;

            shown = pending;
            dw.set(pending ? 0 : -originalW());
            if (pending) {
                resetText.enable();
                resetText.animations.removeIf(animation -> animation instanceof Animation.FadeIn);
                resetText.addAnimation(new Animation.FadeIn(0.3), 0);
                title.dx.set(5);
            } else {
                resetText.animations.removeIf(animation -> animation instanceof Animation.FadeOut);
                resetText.addAnimation(new Animation.FadeOut(0.3).noDeletion(), 0);
                title.dx.set(0);
            }
        }

        @Override
        public void onInitialize() {
            resetText = addChild(new TextWidget(CENTER, Component.literal("x")).noShadow().centerAlignY().centerAlign().mainColor(-1));
            dw.setImmediate(-originalW());
            resetText.disable();

            title = addChild(new TextWidget(fromTopRight(0, 8), caption).centerAlignY().mainColor(-1));
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
            if (!isInBounds(event.x(), event.y())) return false;
            if (event.y() < parent.parent.y() || event.y() > parent.parent.y() + parent.parent.h()) return false;

            reset.run();
            return true;
        }
    }

    public static class SettingSwitchWidget extends Widget implements Lifecycle, Clickable {
        public OptionInstance<Boolean> instance;
        public CycleButton<Boolean> cycle;
        public RectangleWidget highlight;
        public boolean val;

        public SettingSwitchWidget(Coordinate pos, Coordinate size, OptionInstance<Boolean> instance) {
            super(pos, size);

            this.instance = instance;
            this.val = instance.get();
        }

        public SettingSwitchWidget(Coordinate pos, Coordinate size, CycleButton<Boolean> cycle) {
            super(pos, size);

            this.cycle = cycle;
            this.val = cycle.getValue();
        }

        @Override
        public void onInitialize() {
            addChild(new RectangleWidget(ZERO, FULL).applyTheme(ThemeTemplate.DARK_BG));
            highlight = new HoverableRectWidget(ZERO, fromCenterBottom(0, 0), 0x3fffffff);
            highlight.dx.setImmediate(val ? (double) w() / 2 : 0);
            addChild(highlight);
            addChild(new TextWidget(custom(0.25, 0, 0.5, 0), CommonComponents.OPTION_OFF).noShadow().centerAlignY().centerAlign());
            addChild(new TextWidget(custom(0.75, 0, 0.5, 0), CommonComponents.OPTION_ON).noShadow().centerAlignY().centerAlign());
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
            if (!isInBounds(event.x(), event.y()) || !(parent.parent.x() <= event.x() && event.x() <= parent.parent.x() + parent.parent.w() && parent.parent.y() <= event.y() && event.y() <=parent.parent.y() + parent.parent.h())) return false;
            highlight.dx.set(highlight.dx.target > 0 ? 0 : (double) w() / 2);
            val = !val;
            if (instance != null) queueOptionChange(screen, instance, val);
            else queueCycleChange(screen, cycle, val);
            return true;
        }
    }

    public static class HoverableRectWidget extends RectangleWidget {
        public boolean hover = false;
        public Color OriginalMainColorActually;

        public HoverableRectWidget(Coordinate pos, Coordinate size, Color color) {
            super(pos, size);
            this.OriginalMainColorActually = color;
            this.mainColor(color.toArgb());
        }

        public HoverableRectWidget(Coordinate pos, Coordinate size, int color) {
            this(pos, size, new Color(color));
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mx, int my, float pt) {
            boolean in = isInBounds(mx, my);
            if (in != hover) {
                hover = in;
                animations.removeIf(animation -> animation instanceof Animation.TransitionColor);
                addAnimation(new Animation.TransitionColor(0.2, in ? ModColors.MAIN_COLOR_M.withAlpha(127) : OriginalMainColorActually, Animation.TransitionColor.Target.MAIN), 0);
            }
            super.render(graphics, mx, my, pt);
        }
    }

    public static class SettingEnumWidget extends Widget implements Lifecycle{
        public OptionInstance<Object> instance;
        public CycleButton<Object> cycle;
        public List<?> values;
        public int selectionIndex;
        public Widget text;

        public SettingEnumWidget(Coordinate pos, Coordinate size, OptionInstance<?> instance) {
            this(pos, size, instance, ((OptionInstance.Enum<?>) instance.values()).values());
        }

        @SuppressWarnings("unchecked")
        public SettingEnumWidget(Coordinate pos, Coordinate size, OptionInstance<?> instance, List<?> values) {
            super(pos, size);

            this.instance = (OptionInstance<Object>) instance;
            this.values = values;
            this.selectionIndex = values.indexOf(instance.get());
        }

        @SuppressWarnings("unchecked")
        public SettingEnumWidget(Coordinate pos, Coordinate size, CycleButton<?> cycle) {
            super(pos, size);

            this.cycle = (CycleButton<Object>) cycle;
            this.values = this.cycle.values.getSelectedList();
            this.selectionIndex = values.indexOf(cycle.getValue());
        }

        @Override
        public void onInitialize() {
            addChild(new RectangleWidget(fromTopLeft(h() + 1, 0), fromBottomRight(-2 * h() - 2, 0)).applyTheme(ThemeTemplate.DARK_BG));

            // 初始化text
            text = addChild(new TextWidget(CENTER, displayText(instance != null ? instance.get() : cycle.getValue())).noShadow().centerAlignY().centerAlign());

            // 左
            addChild(new HoverableRectWidget(ZERO, fromTopLeft(h(), h()), 0x7f000000)).addClickBehavior(
                    () -> {
                        selectionIndex = Math.floorMod(selectionIndex - (Minecraft.getInstance().hasShiftDown() ? 5 : 1), values.size());
                        var next = values.get(selectionIndex);
                        if (instance != null) queueOptionChange(screen, instance, next);
                        else queueCycleChange(screen, cycle, next);
                        text.addAnimation(new Animation.FadeOut.ToRight(0.3), 0);
                        text = addChild(new TextWidget(CENTER, displayText(next)).noShadow().centerAlignY().centerAlign().addAnimation(new Animation.FadeIn.FromLeft(0.3), 0));
                    }
            );
            addChild(new TextWidget(fromTopLeft(h()/2, h()/2), Component.literal("<")).noShadow().centerAlignY().centerAlign());
            // 右
            addChild(new HoverableRectWidget(fromTopRight(-h(), 0), fromTopLeft(h(), h()), 0x7f000000)).addClickBehavior(
                    () -> {
                        selectionIndex = Math.floorMod(selectionIndex + (Minecraft.getInstance().hasShiftDown() ? 5 : 1), values.size());
                        var next = values.get(selectionIndex);
                        if (instance != null) queueOptionChange(screen, instance, next);
                        else queueCycleChange(screen, cycle, next);
                        text.addAnimation(new Animation.FadeOut.ToLeft(0.3), 0);
                        text = addChild(new TextWidget(CENTER, displayText(next)).noShadow().centerAlignY().centerAlign().addAnimation(new Animation.FadeIn.FromRight(0.3), 0));
                    }
            );
            addChild(new TextWidget(fromTopRight(-h()/2, h()/2), Component.literal(">")).noShadow().centerAlignY().centerAlign());
        }

        public Component displayText(Object value) {
            if (instance != null) return OptionsSubScreenWidgets.displayText(instance, value);
            return OptionsSubScreenWidgets.displayText(cycle.name, cycle.valueStringifier.apply(value));
        }
    }

    public static class SettingDoubleRangeWidget extends Widget implements Lifecycle {
        public OptionInstance<Double> instance;
        public OptionInstance.SliderableValueSet<Double> slider;
        public TextWidget text;
        public double current;

        @SuppressWarnings("unchecked")
        public SettingDoubleRangeWidget(Coordinate pos, Coordinate size, OptionInstance<?> instance) {
            super(pos, size);
            this.instance = (OptionInstance<Double>) instance;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onInitialize() {
            slider = (OptionInstance.SliderableValueSet<Double>) instance.values();
            current = instance.get();

            addChild(new RectangleWidget(fromTopLeft(2 * h() + 2, 0), fromBottomRight(-4 * h() - 4, 0)).applyTheme(ThemeTemplate.DARK_BG));
            text = new TextWidget(CENTER, displayText(instance, current)).noShadow();
            addChild(text.centerAlign().centerAlignY());

            addChild(new HoverableRectWidget(fromTopLeft(0, 0), fromTopLeft(h(), h()), 0x7f000000)).addClickBehavior(
                    () -> {
                        current = slider.fromSliderValue(0);
                        queueOptionChange(screen, instance, current);
                        text.setText(displayText(instance, current));
                    }
            );
            addChild(new TextWidget(fromTopLeft(h()/2, h()/2), Component.literal("|<")).noShadow().centerAlignY().centerAlign());
            addChild(new HoverableRectWidget(fromTopLeft(h() + 1, 0), fromTopLeft(h(), h()), 0x7f000000)).addClickBehavior(
                    () -> {
                        current = slider.fromSliderValue(clamp(slider.toSliderValue(current) - (Minecraft.getInstance().hasShiftDown() ? 0.1 : 0.01), 0, 1));
                        queueOptionChange(screen, instance, current);
                        text.setText(displayText(instance, current));
                    }
            );
            addChild(new TextWidget(fromTopLeft(h() + h()/2 + 1, h()/2), Component.literal("<")).noShadow().centerAlignY().centerAlign());

            addChild(new HoverableRectWidget(fromTopRight(-h() - 1, 0), fromTopLeft(h(), h()), 0x7f000000)).rightAlign().addClickBehavior(
                    () -> {
                        current = slider.fromSliderValue(clamp(slider.toSliderValue(current) + (Minecraft.getInstance().hasShiftDown() ? 0.1 : 0.01), 0, 1));
                        queueOptionChange(screen, instance, current);
                        text.setText(displayText(instance, current));
                    }
            );
            addChild(new TextWidget(fromTopRight(-h() - h()/2 - 1, h()/2), Component.literal(">")).noShadow().centerAlignY().centerAlign());
            addChild(new HoverableRectWidget(fromTopRight(0, 0), fromTopLeft(h(), h()), 0x7f000000)).rightAlign().addClickBehavior(
                    () -> {
                        current = slider.fromSliderValue(1);
                        queueOptionChange(screen, instance, current);
                        text.setText(displayText(instance, current));
                    }
            );
            addChild(new TextWidget(fromTopRight(-h()/2, h()/2), Component.literal(">|")).noShadow().centerAlignY().centerAlign());
        }
    }
}
