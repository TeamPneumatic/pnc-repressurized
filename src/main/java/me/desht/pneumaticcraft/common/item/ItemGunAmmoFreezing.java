package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.TemporaryBlockManager;
import me.desht.pneumaticcraft.common.config.Config;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.Random;

public class ItemGunAmmoFreezing extends ItemGunAmmo {
    public ItemGunAmmoFreezing() {
        super(DEFAULT_PROPS.maxDamage(Config.Common.Minigun.freezingAmmoCartridgeSize), "gun_ammo_freezing");
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x0040A0FF;
    }

    @Override
    protected float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        double mul = super.getDamageMultiplier(target, ammoStack);
        if (target != null && target.isImmuneToFire()) {
            mul *= 1.5;
        }
        return (float) mul;
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        double knockback = -1;
        if (target instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) target;
            living.addPotionEffect(new EffectInstance(Effects.SLOWNESS, living.getRNG().nextInt(40) + 40, 3));
            if (minigun.dispenserWeightedPercentage(Config.Common.Minigun.freezingAmmoEntityIceChance)) {
                // temporarily stop the target getting knocked back, since it might be knocked out of the freeze zone
                knockback = living.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getBaseValue();
                living.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
                encaseInFakeIce(minigun, target);
            }
        }
        if (knockback != -1) {
            ((LivingEntity) target).getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(knockback);
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    private void encaseInFakeIce(Minigun minigun, Entity target) {
        World world = target.world;
        Random rnd = world.rand;
        TemporaryBlockManager mgr = TemporaryBlockManager.getManager(world);
        AxisAlignedBB aabb = target.getBoundingBox();
        for (int y = (int) aabb.minY; y <= aabb.maxY; y++) {
            for (int x = (int) Math.floor(aabb.minX); x <= aabb.maxX; x++) {
                for (int z = (int) Math.floor(aabb.minZ); z <= aabb.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (world.getBlockState(pos).isAir(world, pos) || world.getFluidState(pos).isTagged(FluidTags.WATER)) {
                        mgr.trySetBlock(minigun.getPlayer(), Direction.UP, pos, ModBlocks.FAKE_ICE.getDefaultState(), 60 + rnd.nextInt(40));
                    }
                }
            }
        }
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockRayTraceResult brtr) {
        World world = minigun.getWorld();
        BlockPos pos = brtr.getPos();
        // field_223228_b_ = NETHER
        if (world.getDimension().getType() != DimensionType.field_223228_b_ && minigun.dispenserWeightedPercentage(Config.Common.Minigun.freezingAmmoBlockIceChance)) {
            BlockPos pos1;
            if (world.getBlockState(pos).getShape(world, pos) == VoxelShapes.fullCube() || brtr.getFace() != Direction.UP) {
                pos1 = pos.offset(brtr.getFace());
            } else {
                pos1 = pos;
            }
            BlockState newState = null;
            if (world.isAirBlock(pos1) && !world.isAirBlock(pos1.down())) {
                // form snow layers on solid blocks
                newState = Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, 1);
            } else if (world.getBlockState(pos1).getBlock() == Blocks.SNOW) {
                // grow existing snow layers
                BlockState state = world.getBlockState(pos1);
                int level = state.get(SnowBlock.LAYERS);
                if (level < 8) {
                    newState = Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, level + 1);
                } else {
                    newState = Blocks.SNOW_BLOCK.getDefaultState();
                }
            } else if (world.getBlockState(pos1).getBlock() == Blocks.WATER) {
                // freeze surface water
                Vec3d eye = minigun.getPlayer().getEyePosition(0f);
                RayTraceContext ctx = new RayTraceContext(eye, brtr.getHitVec(), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, minigun.getPlayer());
                BlockRayTraceResult res = world.rayTraceBlocks(ctx);
                if (res.getType() == RayTraceResult.Type.BLOCK) {
                    pos1 = res.getPos();
                    newState = Blocks.ICE.getDefaultState();
                }
            }
            if (newState != null) {
                PneumaticCraftUtils.tryPlaceBlock(world, pos1, minigun.getPlayer(), brtr.getFace(), newState);
            }
        }
        return super.onBlockHit(minigun, ammo, brtr);
    }

}
