package org.creepebucket.arcanui.client.screen.lib;

import net.minecraft.client.gui.screens.Screen;
import org.creepebucket.creepebucketlib.lib.api.Animation;
import org.creepebucket.creepebucketlib.lib.api.Widget;
import org.creepebucket.creepebucketlib.lib.ui.WidgetManager;
import org.creepebucket.creepebucketlib.lib.widgets.CullAreaWidget;

import static org.creepebucket.creepebucketlib.lib.api.Coordinate.ZERO;
import static org.creepebucket.creepebucketlib.lib.api.Coordinate.fromCenter;

public interface ArcanScreen extends WidgetManager {
    ArcanUiRuntime ui();

    void buildUi(ArcanUiRuntime ui);

    default Runnable playQuitAnimationAndDo(Runnable things) {
        return () -> {
            var widget = this.ui().addWidget(new Widget.BlankWidget(fromCenter(0, 0), ZERO));
            var centerY = widget.y();
            widget.addAnimation(new DelayAndDo(0.3, things), 0);
            for (Widget w : this.ui().root.allChild()) {
                if (w instanceof Widget.BlankWidget || w instanceof CullAreaWidget) {
                    // pass
                } else if (w.y() > centerY) {
                    w.addAnimation(new Animation.FadeOut.ToBottom(0.3), 0);
                } else {
                    w.addAnimation(new Animation.FadeOut.ToTop(0.3), 0);
                }
            }
        };
    }

    class DelayAndDo extends Animation {
        Runnable things;

        public DelayAndDo(double dur, Runnable things) {
            this.duration = dur;
            this.things = things;
        }

        @Override
        public void step(double v) {

        }

        @Override
        public void onExpire(Widget widget) {
            things.run();
        }
    }

    default void disposeUi() {
    }

    default void applyBounds(int left, int top, int width, int height) {
    }

    @Override
    default Widget.Root getRoot() {
        return ui().getRoot();
    }

    @Override
    default void setRoot(Widget.Root root) {
        ui().setRoot(root);
    }

    @Override
    default Screen getScreen() {
        return (Screen) this;
    }
}
