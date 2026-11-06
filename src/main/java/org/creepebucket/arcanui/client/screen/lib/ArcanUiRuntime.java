package org.creepebucket.arcanui.client.screen.lib;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.creepebucket.creepebucketlib.client.ClientUiContext;
import org.creepebucket.creepebucketlib.lib.api.Coordinate;
import org.creepebucket.creepebucketlib.lib.api.Widget;
import org.creepebucket.creepebucketlib.lib.ui.WidgetManager;

public final class ArcanUiRuntime implements WidgetManager {
    public Widget.Root root = new Widget.Root();
    public Screen screen;
    public int left;
    public int top;
    public int width;
    public int height;
    public boolean quitting;

    @Override
    public Widget.Root getRoot() {
        return this.root;
    }

    @Override
    public void setRoot(Widget.Root root) {
        this.root = root;
    }

    @Override
    public Screen getScreen() {
        return this.screen;
    }

    public void begin(Screen screen) {
        this.screen = screen;
        this.quitting = false;
        Coordinate.updateContext(screen.width, screen.height, 0, 0);

        resetWidgets();

        ClientUiContext.setFont(screen.getFont());
    }

    public void renderBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        renderWidgetLabels(graphics, mouseX, mouseY);

        List<Widget> widgets = this.root.allChild();
        this.left = widgets.stream().mapToInt(Widget::left).min().orElse(0);
        this.top = widgets.stream().mapToInt(Widget::top).min().orElse(0);
        this.width = widgets.stream().mapToInt(Widget::right).max().orElse(this.screen.width) - this.left;
        this.height = widgets.stream().mapToInt(Widget::bottom).max().orElse(this.screen.height) - this.top;

        Coordinate.updateContext(this.screen.width, this.screen.height, this.left, this.top);
    }

    public void renderForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.nextStratum();
        graphics.pose().pushMatrix();
        if (this.screen instanceof AbstractContainerScreen<?>) {
            graphics.pose().translate(-this.left, -this.top);
        }
        renderWidgets(graphics, mouseX, mouseY, partialTick);
        graphics.pose().popMatrix();
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return widgetMouseClicked(event, doubleClick);
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        return widgetMouseReleased(event);
    }

    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return widgetMouseDragged(event, dragX, dragY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return widgetMouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean keyPressed(KeyEvent event) {
        return widgetKeyPressed(event);
    }

    public boolean keyReleased(KeyEvent event) {
        return widgetKeyReleased(event);
    }

    public boolean charTyped(CharacterEvent event) {
        return widgetCharTyped(event);
    }

    public void tick() {
        tickWidgets();
    }

    public void destroy() {
        resetWidgets();
    }
}
