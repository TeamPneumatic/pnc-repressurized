package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemVortexCannon extends ItemPressurizable {

    public ItemVortexCannon() {
        super("vortex_cannon", PneumaticValues.VORTEX_CANNON_MAX_AIR, PneumaticValues.VORTEX_CANNON_VOLUME);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity playerIn, Hand handIn) {
        ItemStack iStack = playerIn.getHeldItem(handIn);

        iStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(airHandler -> {
            if (airHandler.getPressure() > 0.1f) {
                double factor = 0.2D * airHandler.getPressure();
                world.playSound(playerIn.posX, playerIn.posY, playerIn.posZ, ModSounds.AIR_CANNON, SoundCategory.PLAYERS, 1.0F, 0.7F + (float) factor * 0.2F, false);
                EntityVortex vortex = new EntityVortex(world, playerIn);
                Vec3d directionVec = playerIn.getLookVec().normalize();
                vortex.posX += directionVec.x;
                vortex.posY += directionVec.y;
                vortex.posZ += directionVec.z;
                vortex.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 0.0F);
                vortex.setMotion(vortex.getMotion().scale(factor));
                if (!world.isRemote) world.addEntity(vortex);
                airHandler.addAir(-PneumaticValues.USAGE_VORTEX_CANNON);
            }
        });

        return ActionResult.newResult(ActionResultType.SUCCESS, iStack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
}
