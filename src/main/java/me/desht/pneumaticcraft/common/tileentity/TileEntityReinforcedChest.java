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

package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerReinforcedChest;
import me.desht.pneumaticcraft.common.inventory.handler.ComparatorItemStackHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityReinforcedChest extends TileEntityBase implements INamedContainerProvider, IComparatorSupport {
    public static final int CHEST_SIZE = 36;

    public static final String NBT_ITEMS = "Items";
    private static final String NBT_LOOT_TABLE = "LootTable";
    private static final String NBT_LOOT_TABLE_SEED = "LootTableSeed";

    private ResourceLocation lootTable;
    private long lootTableSeed;

    private final ComparatorItemStackHandler inventory = new ComparatorItemStackHandler(this, CHEST_SIZE) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() != ModBlocks.REINFORCED_CHEST.get().asItem() && super.isItemValid(slot, stack);
        }
    };
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> inventory);

    public TileEntityReinforcedChest() {
        super(ModTileEntities.REINFORCED_CHEST.get());
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return inventoryCap;
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        if (lootTable != null) {
            tag.putString(NBT_LOOT_TABLE, lootTable.toString());
            if (lootTableSeed != 0L) {
                tag.putLong(NBT_LOOT_TABLE_SEED, lootTableSeed);
            }
        } else {
            tag.put(NBT_ITEMS, inventory.serializeNBT());
        }
        return super.save(tag);
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        if (tag.contains(NBT_LOOT_TABLE, Constants.NBT.TAG_STRING)) {
            lootTable = new ResourceLocation(tag.getString(NBT_LOOT_TABLE));
            lootTableSeed = tag.getLong(NBT_LOOT_TABLE_SEED);
        } else {
            inventory.deserializeNBT(tag.getCompound(NBT_ITEMS));
        }
    }

    @Override
    public boolean shouldPreserveStateOnBreak() {
        return true;  // always, even when broken with a pick
    }

    @Override
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        // drop nothing; contents are serialized to the dropped item
    }

    @Override
    public void serializeExtraItemData(CompoundNBT blockEntityTag, boolean preserveState) {
        super.serializeExtraItemData(blockEntityTag, preserveState);

        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                blockEntityTag.put(NBT_ITEMS, inventory.serializeNBT());
                break;
            }
        }
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
        maybeFillWithLoot(player);
        return new ContainerReinforcedChest(windowId, inv, getBlockPos());
    }

    private void maybeFillWithLoot(PlayerEntity player) {
        if (lootTable != null && level instanceof ServerWorld) {
            LootTable table = level.getServer().getLootTables().get(this.lootTable);
            lootTable = null;
            LootContext.Builder contextBuilder = new LootContext.Builder((ServerWorld)this.level).withOptionalRandomSeed(this.lootTableSeed);
            contextBuilder.withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(this.worldPosition));
            if (player != null) {
                contextBuilder.withLuck(player.getLuck());
            }

            RecipeWrapper invWrapper = new RecipeWrapper(inventory);  // handy forge-provided IInventory->IItemHandlerModifiable adapter
            LootContext context = contextBuilder.create(LootParameterSets.CHEST);
            table.fill(invWrapper, context);

            setChanged();
        }
    }

    @Override
    public int getComparatorValue() {
        return inventory.getComparatorValue();
    }
}
