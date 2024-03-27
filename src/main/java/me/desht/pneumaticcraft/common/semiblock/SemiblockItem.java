/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractSemiblockEntity;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class SemiblockItem extends Item {
    public SemiblockItem() {
        super(ModItems.defaultProps());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            return placeSemiblock(context);
        }
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
    public AbstractSemiblockEntity createEntity(Level world, ItemStack stack, Player player, BlockPos pos) {
        ResourceLocation regName = PneumaticCraftUtils.getRegistryName(this).orElseThrow();
        return BuiltInRegistries.ENTITY_TYPE.getOptional(regName).map(type -> {
            Entity e = type.create(world);
            if (e instanceof AbstractSemiblockEntity semi) {
                e.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0f, 0f);
                EntityType.updateCustomEntityTag(world, player, e, stack.getTag());
                return semi;
            }
            return null;
        }).orElse(null);
    }

    private InteractionResult placeSemiblock(UseOnContext context) {
        Level world = context.getLevel();
        ItemStack itemstack = context.getItemInHand();
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        Player player = context.getPlayer();

        AbstractSemiblockEntity eSemi = createEntity(context.getLevel(), itemstack, context.getPlayer(), blockpos);
        if (eSemi != null) {
            if (!eSemi.canPlace(direction)) {
                // if the semiblock can't go in the clicked pos, maybe it can go adjacent to it?
                eSemi.setPos(eSemi.getX() + direction.getStepX(), eSemi.getY() + direction.getStepY(), eSemi.getZ() + direction.getStepZ());
                if (!eSemi.canPlace(direction)) {
                    return InteractionResult.FAIL;
                }
            }

            if (eSemi instanceof IDirectionalSemiblock d) {
                d.setSide(direction);
            }

            if (SemiblockTracker.getInstance().getAllSemiblocks(world, eSemi.getBlockPos()).anyMatch(s -> !s.canCoexist(eSemi))) {
                return InteractionResult.FAIL;
            }

            world.addFreshEntity(eSemi);
            eSemi.onPlaced(player, context.getItemInHand(), direction);
            if (player != null && !player.isCreative()) {
                itemstack.shrink(1);
            }
        } else {
            Log.warning("can't get entity for semiblock item: " + this);
        }
        return InteractionResult.SUCCESS;
    }
}
