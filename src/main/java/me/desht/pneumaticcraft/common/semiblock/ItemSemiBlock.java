package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.semiblock.EntitySemiblockBase;
import me.desht.pneumaticcraft.common.item.ItemPneumatic;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemSemiBlock extends ItemPneumatic {
    public ItemSemiBlock() {
        super(ModItems.defaultProps());
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (context.getWorld().isRemote) {
            return ActionResultType.SUCCESS;
        } else {
            return placeSemiblock(context);
        }
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return super.onItemUse(context);
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
        Entity e = type.create(world, stack.getTag(), null, player, pos, SpawnReason.NATURAL, false, true);
        return e instanceof EntitySemiblockBase ? (EntitySemiblockBase) e : null;
    }

    private ActionResultType placeSemiblock(ItemUseContext context) {
        World world = context.getWorld();
        ItemStack itemstack = context.getItem();
        BlockPos blockpos = context.getPos();
        Direction direction = context.getFace();
        PlayerEntity player = context.getPlayer();

        EntitySemiblockBase eSemi = createEntity(context.getWorld(), itemstack, context.getPlayer(), blockpos);
        if (eSemi != null) {
            if (SemiblockTracker.getInstance().getAllSemiblocks(world, blockpos).anyMatch(s -> !s.canCoexist(eSemi))) {
                return ActionResultType.FAIL;
            }

            if (!eSemi.canPlace(direction)) {
                eSemi.setPosition(eSemi.posX + direction.getXOffset(), eSemi.posY + direction.getYOffset(), eSemi.posZ + direction.getZOffset());
                if (!eSemi.canPlace(direction)) {
                    return ActionResultType.FAIL;
                }
            }

            if (eSemi instanceof IDirectionalSemiblock) {
                ((IDirectionalSemiblock) eSemi).setSide(direction);
            }

            world.addEntity(eSemi);
            eSemi.onPlaced(player, context.getItem(), direction);
            world.notifyNeighborsOfStateChange(blockpos, world.getBlockState(blockpos).getBlock());
            itemstack.shrink(1);
        } else {
            Log.warning("can't get entity for semiblock item " + getRegistryName());
        }
        return ActionResultType.SUCCESS;
    }
}
