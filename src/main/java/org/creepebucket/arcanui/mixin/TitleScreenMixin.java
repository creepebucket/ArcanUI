package org.creepebucket.arcanui.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 干掉原版的渲染
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    public TitleScreenMixin() {
        super(Component.empty());
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void arcanui$skipVanillaChrome(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!((Object) this instanceof ArcanScreen)) {
            return;
        }

        this.extractPanorama(graphics, partialTick);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        ci.cancel();
    }
}
