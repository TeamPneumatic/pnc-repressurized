package me.desht.pneumaticcraft.common.capabilities;

import java.util.HashSet;
import java.util.Set;

import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilityGPSAreaTool{
    @CapabilityInject(CapabilityGPSAreaTool.class)
    public static Capability<CapabilityGPSAreaTool> INSTANCE;

    private ProgWidgetArea area = new ProgWidgetArea();

    public void setPos(BlockPos pos, int index){
        area.setAreaPoint(pos, index);
    }
    
    public Set<BlockPos> getArea(){
        Set<BlockPos> set = new HashSet<BlockPos>();
        area.getArea(set);
        return set;
    }
    
    public ProgWidgetArea createWidget(){
        return (ProgWidgetArea)area.copy();
    }
    
    
    public static void register(){
        CapabilityManager.INSTANCE.register(CapabilityGPSAreaTool.class, new Capability.IStorage<CapabilityGPSAreaTool>(){
            @Override
            public NBTBase writeNBT(Capability<CapabilityGPSAreaTool> capability, CapabilityGPSAreaTool instance, EnumFacing side){
                NBTTagCompound tag = new NBTTagCompound();
                instance.area.writeToNBT(tag);
                return tag;
            }

            @Override
            public void readNBT(Capability<CapabilityGPSAreaTool> capability, CapabilityGPSAreaTool instance, EnumFacing side, NBTBase base){
                NBTTagCompound tag = (NBTTagCompound)base;
                instance.area.readFromNBT(tag);
            }

        }, CapabilityGPSAreaTool::new);
    }

    public static class Provider implements ICapabilitySerializable<NBTBase>{
        private final CapabilityGPSAreaTool cap = new CapabilityGPSAreaTool();

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing){
            return capability == INSTANCE;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing){
            if(hasCapability(capability, facing)) {
                return (T)cap;
            } else {
                return null;
            }
        }

        @Override
        public NBTBase serializeNBT(){
            return INSTANCE.getStorage().writeNBT(INSTANCE, cap, null);
        }

        @Override
        public void deserializeNBT(NBTBase nbt){
            INSTANCE.getStorage().readNBT(INSTANCE, cap, null, nbt);
        }
    }
}
