package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class ItemVortexCannon extends ItemPressurizable {

    public ItemVortexCannon() {
        super(PneumaticValues.VORTEX_CANNON_MAX_AIR, PneumaticValues.VORTEX_CANNON_VOLUME);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity playerIn, Hand handIn) {
        ItemStack iStack = playerIn.getHeldItem(handIn);

        IAirHandlerItem airHandler = iStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                .orElseThrow(RuntimeException::new);
        float factor = 0.2F * airHandler.getPressure();

        if (world.isRemote) {
            if (airHandler.getPressure() > 0.1f) {
                world.playSound(playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ(), ModSounds.AIR_CANNON.get(), SoundCategory.PLAYERS, 1.0F, 0.7F + factor * 0.2F, false);
            } else {
                playerIn.playSound(SoundEvents.BLOCK_COMPARATOR_CLICK, 1.0f, 2f);
                return ActionResult.resultFail(iStack);
            }
        } else {
            if (airHandler.getPressure() > 0.1f) {
                world.playSound(playerIn, playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ(), ModSounds.AIR_CANNON.get(), SoundCategory.PLAYERS, 1.0F, 0.7F + factor * 0.2F);
                EntityVortex vortex = ModEntities.VORTEX.get().create(world);
                if (vortex != null) {
                    Vector3d directionVec = playerIn.getLookVec().normalize().scale(playerIn.isSprinting() ? -0.35 : -0.15);
                    Vector3d vortexPos = playerIn.getPositionVec().add(0, playerIn.getEyeHeight() / 2, 0).add(directionVec);
                    vortex.setPosition(vortexPos.x, vortexPos.y, vortexPos.z);
                    vortex.func_234612_a_(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F * factor, 0.0F);
                    world.addEntity(vortex);
                    airHandler.addAir(-PneumaticValues.USAGE_VORTEX_CANNON);
                }
            }
        }
        return ActionResult.resultSuccess(iStack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
}
