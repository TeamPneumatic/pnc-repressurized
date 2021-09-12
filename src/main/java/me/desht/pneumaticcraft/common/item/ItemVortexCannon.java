package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.core.ModItems;
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
        super(ModItems.toolProps(), PneumaticValues.VORTEX_CANNON_MAX_AIR, PneumaticValues.VORTEX_CANNON_VOLUME);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity playerIn, Hand handIn) {
        ItemStack iStack = playerIn.getItemInHand(handIn);

        IAirHandlerItem airHandler = iStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                .orElseThrow(RuntimeException::new);
        float factor = 0.2F * airHandler.getPressure();

        if (world.isClientSide) {
            if (airHandler.getPressure() > 0.1f) {
                world.playLocalSound(playerIn.getX(), playerIn.getY(), playerIn.getZ(), ModSounds.AIR_CANNON.get(), SoundCategory.PLAYERS, 1.0F, 0.7F + factor * 0.2F, false);
            } else {
                playerIn.playSound(SoundEvents.COMPARATOR_CLICK, 1.0f, 2f);
                return ActionResult.fail(iStack);
            }
        } else {
            if (airHandler.getPressure() > 0.1f) {
                world.playSound(playerIn, playerIn.getX(), playerIn.getY(), playerIn.getZ(), ModSounds.AIR_CANNON.get(), SoundCategory.PLAYERS, 1.0F, 0.7F + factor * 0.2F);
                EntityVortex vortex = ModEntities.VORTEX.get().create(world);
                if (vortex != null) {
                    Vector3d directionVec = playerIn.getLookAngle().normalize().scale(playerIn.isSprinting() ? -0.35 : -0.15);
                    Vector3d vortexPos = playerIn.position().add(0, playerIn.getEyeHeight() / 2, 0).add(directionVec);
                    vortex.setPos(vortexPos.x, vortexPos.y, vortexPos.z);
                    vortex.shootFromRotation(playerIn, playerIn.xRot, playerIn.yRot, 0.0F, 1.5F * factor, 0.0F);
                    world.addFreshEntity(vortex);
                    airHandler.addAir(-PneumaticValues.USAGE_VORTEX_CANNON);
                }
            }
        }
        return ActionResult.success(iStack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
}
