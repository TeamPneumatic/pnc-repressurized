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

package me.desht.pneumaticcraft.datagen.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.common.tileentity.*;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.desht.pneumaticcraft.api.lib.NBTKeys.NBT_AIR_AMOUNT;
import static me.desht.pneumaticcraft.api.lib.NBTKeys.NBT_SIDE_CONFIG;

/**
 * Handle the standard serialization of PNC tile entity data to the dropped itemstack.
 * Saved to the "BlockEntityTag" NBT tag, so will be copied directly back to the TE's NBT
 * by {@link net.minecraft.item.BlockItem#updateCustomBlockEntityTag(World, PlayerEntity, BlockPos, ItemStack)}
 */
public class TileEntitySerializerFunction extends LootFunction {
    private TileEntitySerializerFunction(ILootCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        return applyTEdata(stack, context.getParamOrNull(LootParameters.BLOCK_ENTITY));
    }

    public static LootFunction.Builder<?> builder() {
        return simpleBuilder(TileEntitySerializerFunction::new);
    }

    private ItemStack applyTEdata(ItemStack teStack, TileEntity te) {
        // augment existing BlockEntityTag if present, otherwise create a new one
        CompoundNBT nbt = teStack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
        final CompoundNBT subTag = nbt == null ? new CompoundNBT() : nbt;

        // fluid tanks
        if (te instanceof ISerializableTanks) {
            CompoundNBT tankTag = ((ISerializableTanks) te).serializeTanks();
            if (!tankTag.isEmpty()) {
                subTag.put(NBTKeys.NBT_SAVED_TANKS, tankTag);
            }
        }

        // side configuration
        if (te instanceof ISideConfigurable) {
            CompoundNBT tag = SideConfigurator.writeToNBT((ISideConfigurable) te);
            if (!tag.isEmpty()) {
                subTag.put(NBT_SIDE_CONFIG, tag);
            }
        }

        // redstone mode
        if (te instanceof IRedstoneControl) {
            ((IRedstoneControl<?>) te).getRedstoneController().serialize(subTag);
        }

        if (te instanceof TileEntityBase) {
            TileEntityBase teB = (TileEntityBase) te;
            if (teB.shouldPreserveStateOnBreak()) {
                // upgrades (only when wrenched)
                TileEntityBase.UpgradeHandler upgradeHandler = teB.getUpgradeHandler();
                for (int i = 0; i < upgradeHandler.getSlots(); i++) {
                    if (!upgradeHandler.getStackInSlot(i).isEmpty()) {
                        // store creative status directly since it's queried for item model rendering (performance)
                        if (teB.getUpgrades(EnumUpgrade.CREATIVE) > 0) {
                            NBTUtils.setBoolean(teStack, UpgradableItemUtils.NBT_CREATIVE, true);
                        } else {
                            NBTUtils.removeTag(teStack, UpgradableItemUtils.NBT_CREATIVE);
                        }
                        subTag.put(UpgradableItemUtils.NBT_UPGRADE_TAG, upgradeHandler.serializeNBT());
                        break;
                    }
                }

                // saved air (only when wrenched)
                te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(h -> {
                    if (h.getPressure() != 0f) {
                        subTag.putInt(NBT_AIR_AMOUNT, h.getAir());
                    }
                });
            }

            teB.serializeExtraItemData(subTag, teB.shouldPreserveStateOnBreak());
        }

        if (!subTag.isEmpty()) {
            CompoundNBT tag = teStack.getOrCreateTag();
            tag.put(NBTKeys.BLOCK_ENTITY_TAG, subTag);
        } else {
            if (teStack.hasTag() && teStack.getTag().contains(NBTKeys.BLOCK_ENTITY_TAG)) {
                teStack.getTag().remove(NBTKeys.BLOCK_ENTITY_TAG);
            }
        }
        return teStack;
    }

    @Override
    public LootFunctionType getType() {
        return ModLootFunctions.TE_SERIALIZER;
    }

    public static class Serializer extends LootFunction.Serializer<TileEntitySerializerFunction> {
        @Override
        public TileEntitySerializerFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn) {
            return new TileEntitySerializerFunction(conditionsIn);
        }

    }
}
