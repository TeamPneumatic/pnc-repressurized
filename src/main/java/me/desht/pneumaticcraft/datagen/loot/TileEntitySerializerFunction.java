package me.desht.pneumaticcraft.datagen.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.tileentity.ISerializableTanks;
import me.desht.pneumaticcraft.common.tileentity.ISideConfigurable;
import me.desht.pneumaticcraft.common.tileentity.SideConfigurator;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootFunction;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.conditions.ILootCondition;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.lib.NBTKeys.NBT_AIR_AMOUNT;
import static me.desht.pneumaticcraft.lib.NBTKeys.NBT_SIDE_CONFIG;

/**
 * Handle the standard serialization of PNC tile entity data to the dropped itemstack.
 * Saved to the "BlockEntityTag" NBT tag, so will be copied directly back to the TE's NBT
 * by {@link net.minecraft.item.BlockItem#setTileEntityNBT(World, PlayerEntity, BlockPos, ItemStack)}
 */
public class TileEntitySerializerFunction extends LootFunction {
    private TileEntitySerializerFunction(ILootCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ItemStack doApply(ItemStack stack, LootContext context) {
        return getDroppedStack(context.get(LootParameters.BLOCK_ENTITY));
    }

    public static LootFunction.Builder<?> builder() {
        return builder(TileEntitySerializerFunction::new);
    }

    private ItemStack getDroppedStack(TileEntity te) {
        if (te == null) return ItemStack.EMPTY;

        ItemStack teStack = new ItemStack(te.getBlockState().getBlock());

        CompoundNBT subTag = teStack.getChildTag("BlockEntityTag");
        if (subTag == null) subTag = new CompoundNBT();

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

        if (te instanceof TileEntityBase && ((TileEntityBase) te).shouldPreserveStateOnBreak()) {
            // upgrades (only when wrenched)
            TileEntityBase.UpgradeHandler upgradeHandler = ((TileEntityBase) te).getUpgradeHandler();
            for (int i = 0; i < upgradeHandler.getSlots(); i++) {
                if (!upgradeHandler.getStackInSlot(i).isEmpty()) {
                    // store creative status directly since it's queried for item model rendering (performance)
                    if (((TileEntityBase) te).getUpgrades(IItemRegistry.EnumUpgrade.CREATIVE) > 0) {
                        NBTUtil.setBoolean(teStack, UpgradableItemUtils.NBT_CREATIVE, true);
                    } else {
                        NBTUtil.removeTag(teStack, UpgradableItemUtils.NBT_CREATIVE);
                    }
                    subTag.put(UpgradableItemUtils.NBT_UPGRADE_TAG, upgradeHandler.serializeNBT());
                    break;
                }
            }

            // saved air (only when wrenched)
            if (te instanceof IPneumaticMachine) {
                IAirHandler airHandler = ((IPneumaticMachine) te).getAirHandler(null);
                if (airHandler != null && airHandler.getPressure() != 0f) {
                    subTag.putInt(NBT_AIR_AMOUNT, airHandler.getAir());
                }
            }
        }

        if (!subTag.isEmpty()) {
            CompoundNBT tag = teStack.getOrCreateTag();
            tag.put("BlockEntityTag", subTag);
        }
        return teStack;
    }

    public static class Serializer extends LootFunction.Serializer<TileEntitySerializerFunction> {
        public Serializer() {
            super(RL("te_serializer"), TileEntitySerializerFunction.class);
        }

        @Override
        public TileEntitySerializerFunction deserialize(JsonObject object, JsonDeserializationContext ctx, ILootCondition[] conditionsIn) {
            return new TileEntitySerializerFunction(conditionsIn);
        }
    }
}
