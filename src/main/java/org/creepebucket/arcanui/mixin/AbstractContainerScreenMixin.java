package org.creepebucket.arcanui.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.creepebucket.arcanui.client.screen.lib.ArcanScreen;
import org.creepebucket.creepebucketlib.lib.api.ClientSlotManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow
    protected AbstractContainerMenu menu;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    // 干掉原版容器界面的标题和背包文字
    @Inject(method = "extractLabels", at = @At("HEAD"), cancellable = true)
    private void arcanui$skipVanillaLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if ((Object) this instanceof ArcanScreen) {
            ci.cancel();
        }
    }

    @Inject(method = "extractContents", at = @At("HEAD"))
    private void arcanui$applySlotPositions(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!((Object) this instanceof ArcanScreen)) {
            return;
        }

        for (Slot slot : this.menu.slots) {
            var clientPos = ClientSlotManager.getClientPosition(slot);
            slot.x = clientPos == null ? -10000 : clientPos.getFirst() - this.leftPos;
            slot.y = clientPos == null ? -10000 : clientPos.getSecond() - this.topPos;
        }
    }
}
