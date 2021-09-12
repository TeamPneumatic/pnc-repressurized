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
        super(ModItems.defaultProps().stacksTo(16));
    }

    @Nonnull
    public ActionResult<ItemStack> use(World world, PlayerEntity player, @Nonnull Hand hand) {
        player.startUsingItem(hand);
        return ActionResult.success(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        if (entityLiving instanceof PlayerEntity) {
            entityLiving.setHealth(entityLiving.getHealth() + 6f);
            stack.shrink(1);
            ((PlayerEntity) entityLiving).getCooldowns().addCooldown(stack.getItem(), 160);
            if (worldIn.isClientSide) {
                Vector3d pos = entityLiving.getEyePosition(1f).add(entityLiving.getLookAngle().scale(0.5));
                for (int i = 0; i < 5; i++) {
                    worldIn.addParticle(ParticleTypes.HEART, pos.x + worldIn.random.nextFloat() - 0.5, pos.y + worldIn.random.nextFloat() - 0.5, pos.z + worldIn.random.nextFloat() - 0.5, 0, 0.05, 0);
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
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.BOW;
    }
}
