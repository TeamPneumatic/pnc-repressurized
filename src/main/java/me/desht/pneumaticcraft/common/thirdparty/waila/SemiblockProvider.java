package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockBasic;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class SemiblockProvider {

    public static class Data implements IServerDataProvider<TileEntity> {
        @Override
        public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, TileEntity tileEntity) {
            List<SemiBlockBasic> semiBlocks = SemiBlockManager.getInstance(world).getSemiBlocksAsList(SemiBlockBasic.class, world, tileEntity.getPos());
            ListNBT tagList = new ListNBT();
            for (SemiBlockBasic<?> semiBlock : semiBlocks){
                CompoundNBT subTag = new CompoundNBT();
                semiBlock.addWailaInfoToTag(subTag);
                tagList.add(subTag);
            }
            compoundNBT.put("semiBlocks", tagList);
        }
    }

    public static class Component implements IComponentProvider {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
            CompoundNBT tag = accessor.getServerData();
            List<SemiBlockBasic> semiBlocks = SemiBlockManager.getInstance(accessor.getWorld()).getSemiBlocksAsList(SemiBlockBasic.class, accessor.getWorld(), accessor.getPosition());
            ListNBT tagList = tag.getList("semiBlocks", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < semiBlocks.size(); i++){
                NonNullList<ItemStack> l = NonNullList.create();
                semiBlocks.get(i).addDrops(l);
                if (!l.isEmpty()) {
                    tooltip.add(l.get(0).getDisplayName().applyTextStyle(TextFormatting.YELLOW));
                }
                semiBlocks.get(i).addTooltip(tooltip, tagList.getCompound(i), accessor.getPlayer().isSneaking());
            }

        }
    }
}
