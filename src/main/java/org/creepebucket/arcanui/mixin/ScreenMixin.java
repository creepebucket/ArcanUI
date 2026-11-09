package org.creepebucket.arcanui.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 干掉原版界面的模糊背景和菜单背景
@Mixin(Screen.class)
public abstract class ScreenMixin {
    // 已初始化过的界面被重新打开时不会触发 Init.Post，这里补一次 Arcan UI 构建
    // init(II) 是 final 的，repositionElements 只在 initialized 为真（即重新打开）时被调用
    @Inject(
            method = "init(II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;repositionElements()V", shift = At.Shift.AFTER)
    )
    private void arcanui$rebuildOnReopen(int width, int height, CallbackInfo ci) {
        if ((Object) this instanceof ArcanScreen screen) {
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
