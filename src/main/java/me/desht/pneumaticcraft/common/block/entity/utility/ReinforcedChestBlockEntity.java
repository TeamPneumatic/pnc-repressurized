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

package me.desht.pneumaticcraft.common.block.entity.utility;

import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IComparatorSupport;
import me.desht.pneumaticcraft.common.inventory.ReinforcedChestMenu;
import me.desht.pneumaticcraft.common.inventory.handler.ComparatorItemStackHandler;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReinforcedChestBlockEntity extends AbstractPneumaticCraftBlockEntity implements MenuProvider, IComparatorSupport {
    public static final int CHEST_SIZE = 36;

    public static final String NBT_ITEMS = "Items";
    private static final String NBT_LOOT_TABLE = "LootTable";
    private static final String NBT_LOOT_TABLE_SEED = "LootTableSeed";

    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;

    private final ComparatorItemStackHandler inventory = new ComparatorItemStackHandler(this, CHEST_SIZE) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() != ModBlocks.REINFORCED_CHEST.get().asItem() && super.isItemValid(slot, stack);
        }
    };

    public ReinforcedChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.REINFORCED_CHEST.get(), pos, state);
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return inventory;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (lootTable != null) {
            tag.putString(NBT_LOOT_TABLE, lootTable.toString());
            if (lootTableSeed != 0L) {
                tag.putLong(NBT_LOOT_TABLE_SEED, lootTableSeed);
            }
        } else {
            tag.put(NBT_ITEMS, inventory.serializeNBT(provider));
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (tag.contains(NBT_LOOT_TABLE, Tag.TAG_STRING)) {
            lootTable = ResourceKey.create(Registries.LOOT_TABLE, new ResourceLocation(tag.getString(NBT_LOOT_TABLE)));
            lootTableSeed = tag.getLong(NBT_LOOT_TABLE_SEED);
        } else {
            inventory.deserializeNBT(provider, tag.getCompound(NBT_ITEMS));
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
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);

        inventory.loadContainerContents(componentInput.getOrDefault(ModDataComponents.BLOCK_ENTITY_SAVED_INV, ItemContainerContents.EMPTY));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);

        builder.set(ModDataComponents.BLOCK_ENTITY_SAVED_INV, inventory.toContainerContents());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        maybeFillWithLoot(player);
        return new ReinforcedChestMenu(windowId, inv, getBlockPos());
    }

    public void maybeFillWithLoot(Player player) {
        if (lootTable != null && level instanceof ServerLevel serverLevel) {
            LootTable table = level.getServer().reloadableRegistries().getLootTable(lootTable);
            lootTable = null;

            LootParams.Builder builder = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition));
            if (player != null) {
                builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }
            RecipeWrapper invWrapper = new RecipeWrapper(inventory);
            table.fill(invWrapper, builder.create(LootContextParamSets.CHEST), lootTableSeed);

            setChanged();
        }
    }

    @Override
    public int getComparatorValue() {
        return inventory.getComparatorValue();
    }
}
