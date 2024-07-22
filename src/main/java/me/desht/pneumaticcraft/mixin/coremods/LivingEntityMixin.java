package me.desht.pneumaticcraft.mixin.coremods;

import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "onEquipItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;level()Lnet/minecraft/world/level/Level;"))
    public void onOnEquipItem(EquipmentSlot pSlot, ItemStack pOldItem, ItemStack pNewItem, CallbackInfo ci) {
        //noinspection ConstantValue
        if ((Object) this instanceof Player p && pOldItem.getItem() instanceof PneumaticArmorItem && pNewItem.getItem() instanceof PneumaticArmorItem) {
            if (!ItemStack.isSameItemSameComponents(pOldItem, pNewItem)) {
                CommonArmorHandler.getHandlerForPlayer(p).armorSwitched(pSlot);
            }
        }
    }
}
