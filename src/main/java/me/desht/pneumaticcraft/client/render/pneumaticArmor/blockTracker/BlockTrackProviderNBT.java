package me.desht.pneumaticcraft.client.render.pneumaticArmor.blockTracker;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackProvider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockTrackProviderNBT implements IBlockTrackProvider{

    @Override
    public boolean canHandle(IBlockTrackEntry blockTracker){
        return true;
    }

    @Override
    public IBlockTrackHandler provideHandler(World world, BlockPos pos, TileEntity te, Set<IBlockTrackEntry> blockTrackers){
        return new BlockTrackHandlerNBT(te);
    }

    public static class BlockTrackHandlerNBT implements IBlockTrackHandler{

        private TileEntity te;
        private final NBTTagCompound sendingTag;
        
        public BlockTrackHandlerNBT(TileEntity te){
            this.te = te;
            sendingTag = new NBTTagCompound();
            te.writeToNBT(sendingTag);
        }
        
        @Override
        public void toBytes(ByteBuf buf){
            new PacketBuffer(buf).writeCompoundTag(sendingTag);
        }

        @Override
        public void fromBytes(ByteBuf buf){
            updateTE();
            if(te != null){
                try {
                    te.readFromNBT(new PacketBuffer(buf).readCompoundTag());
                } catch(IOException e) {
                    TrackerBlacklistManager.addInventoryTEToBlacklist(te, e);
                    e.printStackTrace();
                }
            }
        }
        
        private void updateTE(){
            if(te != null && te.isInvalid()){
                te = te.getWorld().getTileEntity(te.getPos());
            }
        }

        @Override
        public void addInformation(List<String> infoList, Set<IBlockTrackEntry> blockTrackers){
            updateTE();
            if(te != null){
                for(IBlockTrackEntry entry : blockTrackers){
                    entry.addInformation(te.getWorld(), te.getPos(), te, infoList);
                }
            }            
        }
        
    }
}
