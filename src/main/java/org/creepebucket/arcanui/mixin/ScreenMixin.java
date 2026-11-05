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
