package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.semiblock.EntitySemiblockBase;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemSemiBlock extends Item {
    public ItemSemiBlock() {
        super(ModItems.defaultProps());
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (context.getLevel().isClientSide) {
            return ActionResultType.SUCCESS;
        } else {
            return placeSemiblock(context);
        }
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        return super.useOn(context);
    }

    /**
     * Create a semiblock entity from the given itemstack, loading any saved NBT into the entity.  Does not add the
     * entity to the world.
     *
     * @param world the world
     * @param stack the item
     * @param player player, may be null
     * @param pos block the entity will be placed at (pass BlockPos.ZERO) if you don't plan to add the entity to the world
     * @return the semiblock entity, not added to the world
     */
    public EntitySemiblockBase createEntity(World world, ItemStack stack, PlayerEntity player, BlockPos pos) {
        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(getRegistryName());
//        Entity e = type.create(world, stack.getTag(), null, player, pos, SpawnReason.NATURAL, false, true);
        if (type != null) {
            Entity e = type.create(world);
            if (e != null) {
                e.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0f, 0f);
                EntityType.updateCustomEntityTag(world, player, e, stack.getTag());
                return e instanceof EntitySemiblockBase ? (EntitySemiblockBase) e : null;
            }
        }
        return null;
    }

    private ActionResultType placeSemiblock(ItemUseContext context) {
        World world = context.getLevel();
        ItemStack itemstack = context.getItemInHand();
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        PlayerEntity player = context.getPlayer();

        EntitySemiblockBase eSemi = createEntity(context.getLevel(), itemstack, context.getPlayer(), blockpos);
        if (eSemi != null) {
            if (!eSemi.canPlace(direction)) {
                // if the semiblock can't go in the clicked pos, maybe it can go adjacent to it?
                eSemi.setPos(eSemi.getX() + direction.getStepX(), eSemi.getY() + direction.getStepY(), eSemi.getZ() + direction.getStepZ());
                if (!eSemi.canPlace(direction)) {
                    return ActionResultType.FAIL;
                }
            }

            if (eSemi instanceof IDirectionalSemiblock) {
                ((IDirectionalSemiblock) eSemi).setSide(direction);
            }

            if (SemiblockTracker.getInstance().getAllSemiblocks(world, eSemi.getBlockPos()).anyMatch(s -> !s.canCoexist(eSemi))) {
                return ActionResultType.FAIL;
            }

            world.addFreshEntity(eSemi);
            eSemi.onPlaced(player, context.getItemInHand(), direction);
            world.updateNeighborsAt(blockpos, world.getBlockState(blockpos).getBlock());
            if (!player.isCreative()) {
                itemstack.shrink(1);
            }
        } else {
            Log.warning("can't get entity for semiblock item " + getRegistryName());
        }
        return ActionResultType.SUCCESS;
    }
}
