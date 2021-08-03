package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import me.desht.pneumaticcraft.common.item.ItemCamoApplicator;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class CamoProvider {
    public static class Component implements IComponentProvider {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
            TileEntity te = accessor.getTileEntity();
            if (te instanceof ICamouflageableTE && ((ICamouflageableTE) te).getCamouflage() != null) {
                ITextComponent str = ItemCamoApplicator.getCamoStateDisplayName(((ICamouflageableTE) te).getCamouflage());
                tooltip.add(xlate("pneumaticcraft.waila.camo", str));
            }
        }
    }
}
