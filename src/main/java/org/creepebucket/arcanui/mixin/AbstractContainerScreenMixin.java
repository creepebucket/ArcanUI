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

/**
 * 去掉原版的槽位 hover 高亮。
 *
 * <p>为什么这里必须 mixin：{@code extractSlotHighlightBack/Front} 是
 * {@code AbstractContainerScreen} 的 <b>private</b> 方法，子类无法覆写。
 * 如果不拦，原版白色高亮会和 arcanUI 的紫色描边叠加，观感很脏。</p>
 *
 * <p>关键点是 <b>只对实现了 {@link ArcanScreen} 的屏幕生效</b>：
 * JEI / REI / 其它 mod 的容器界面完全不受影响。这是本项目 mixin 的标准写法——
 * 先判 scope，再 cancel，绝不做全局替换。</p>
 *
 * <p>两个目标方法签名完全一致，所以共用一个注入点。</p>
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow
    protected AbstractContainerMenu menu;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

//    @Inject(
//            method = {"extractSlotHighlightBack", "extractSlotHighlightFront"},
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void arcanui$skipVanillaHighlight(GuiGraphicsExtractor graphics, CallbackInfo ci) {
//        if ((Object) this instanceof ArcanScreen) {
//            ci.cancel();
//        }
//    }

    /**
     * 把 {@link ClientSlotManager} 里记录的客户端坐标写回槽位，原版 {@code extractSlots} 只认 {@code slot.x/y}。
     */
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
