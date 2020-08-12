package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class SemiblockProvider {

    public static class Data implements IServerDataProvider<TileEntity> {
        @Override
        public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, TileEntity tileEntity) {
            CompoundNBT tag = new CompoundNBT();
            SemiblockTracker.getInstance().getAllSemiblocks(world, tileEntity.getPos())
                    .forEach((semiBlock) -> {
                        NonNullList<ItemStack> drops = semiBlock.getDrops();
                        if (!drops.isEmpty()) {
                            tag.put(Integer.toString(semiBlock.getTrackingId()), semiBlock.serializeNBT(new CompoundNBT()));
                        }
                    });
            compoundNBT.put("semiBlocks", tag);
        }
    }

    public static class Component implements IComponentProvider {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
            CompoundNBT tag = accessor.getServerData().getCompound("semiBlocks");

            for (String name : tag.keySet()) {
                try {
                    int entityId = Integer.parseInt(name);
                    ISemiBlock entity = ISemiBlock.byTrackingId(accessor.getWorld(), entityId);
                    if (entity != null) {
                        tooltip.add(new StringTextComponent("[")
                                .append(entity.getDisplayName())
                                .append(new StringTextComponent("]"))
                                .mergeStyle(TextFormatting.YELLOW));
                        entity.addTooltip(tooltip, accessor.getPlayer(), tag.getCompound(name), accessor.getPlayer().isSneaking());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }
}
