package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemBandage extends Item {
    public ItemBandage() {
        super(ModItems.defaultProps().maxStackSize(16));
    }

    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        player.setActiveHand(hand);
        return ActionResult.resultSuccess(player.getHeldItem(hand));
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        if (entityLiving instanceof PlayerEntity) {
            entityLiving.setHealth(entityLiving.getHealth() + 6f);
            stack.shrink(1);
            ((PlayerEntity) entityLiving).getCooldownTracker().setCooldown(stack.getItem(), 160);
            if (worldIn.isRemote) {
                Vector3d pos = entityLiving.getEyePosition(1f).add(entityLiving.getLookVec().scale(0.5));
                for (int i = 0; i < 5; i++) {
                    worldIn.addParticle(ParticleTypes.HEART, pos.x + worldIn.rand.nextFloat() - 0.5, pos.y + worldIn.rand.nextFloat() - 0.5, pos.z + worldIn.rand.nextFloat() - 0.5, 0, 0.05, 0);
                }
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }
}
