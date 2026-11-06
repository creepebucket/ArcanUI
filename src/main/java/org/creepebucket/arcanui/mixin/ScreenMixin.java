package org.creepebucket.arcanui.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 干掉原版界面的模糊背景和菜单背景
@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow private boolean initialized;

    // 已初始化过的界面被重新打开时不会触发 Init.Post，这里补一次 Arcan UI 构建
    @Inject(method = "added", at = @At("TAIL"))
    private void arcanui$rebuildOnReopen(CallbackInfo ci) {
        if (this.initialized && (Object) this instanceof ArcanScreen screen) {
            screen.ui().begin((Screen) (Object) this);
            screen.buildUi(screen.ui());
        }
    }

    @Inject(method = "extractBlurredBackground", at = @At("HEAD"), cancellable = true)
    private void arcanui$skipBlurredBackground(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if ((Object) this instanceof ArcanScreen) {
            ci.cancel();
        }
    }

    @Inject(method = "extractMenuBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At("HEAD"), cancellable = true)
    private void arcanui$skipMenuBackground(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if ((Object) this instanceof ArcanScreen) {
            ci.cancel();
        }
    }
}
