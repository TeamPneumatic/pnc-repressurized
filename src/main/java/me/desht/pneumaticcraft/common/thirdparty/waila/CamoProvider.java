package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import me.desht.pneumaticcraft.common.item.ItemCamoApplicator;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class CamoProvider {
    public static class Component implements IComponentProvider {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
            TileEntity te = accessor.getTileEntity();
            if (te instanceof ICamouflageableTE && ((ICamouflageableTE) te).getCamouflage() != null) {
                ITextComponent str = ItemCamoApplicator.getCamoStateDisplayName(((ICamouflageableTE) te).getCamouflage());
                tooltip.add(new StringTextComponent("[ Camo: ").append(str).appendString("]").mergeStyle(TextFormatting.YELLOW));
            }
        }
    }
}
