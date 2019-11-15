package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import me.desht.pneumaticcraft.common.tileentity.IRedstoneControl;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedstoneControlProvider {
    public static class Data implements IServerDataProvider<TileEntity> {
        @Override
        public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, TileEntity te) {
            if (te instanceof IRedstoneControl) {
                compoundNBT.putInt("redstoneMode", ((IRedstoneControl) te).getRedstoneMode());
            }
        }
    }

    public static class Component implements IComponentProvider {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
            CompoundNBT tag = accessor.getServerData();
            // This is used so that we can split values later easier and have them all in the same layout.
            Map<String, String> values = new HashMap<>();

            if (tag.contains("redstoneMode")) {
                int mode = tag.getInt("redstoneMode");
                TileEntity te = accessor.getTileEntity();
                if (te instanceof TileEntityBase) {
                    values.put(((TileEntityBase) te).getRedstoneTabTitle(), ((TileEntityBase) te).getRedstoneButtonText(mode));
                }
            }

            // Get all the values from the map and put them in the list.
            values.forEach((k, v) -> tooltip.add(
                    new TranslationTextComponent(k)
                            .appendText(": ")
                            .appendSibling(new TranslationTextComponent(v).applyTextStyle(TextFormatting.RED))
            ));
        }
    }
}
