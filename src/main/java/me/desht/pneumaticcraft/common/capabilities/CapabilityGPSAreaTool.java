package me.desht.pneumaticcraft.common.capabilities;

import java.util.HashSet;
import java.util.Set;

import me.desht.pneumaticcraft.common.progwidgets.IVariableProvider;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilityGPSAreaTool implements IVariableProvider{
    @CapabilityInject(CapabilityGPSAreaTool.class)
    public static Capability<CapabilityGPSAreaTool> INSTANCE;

    private ProgWidgetArea area = new ProgWidgetArea();

    public CapabilityGPSAreaTool(){
        area.setVariableProvider(this);
    }
    
    public void setPos(BlockPos pos, int index){
        area.setAreaPoint(pos, index);
    }
    
    public void setVariable(String variable, int index){
        if(index == 0){
            area.setCoord1Variable(variable);
        }else{
            area.setCoord2Variable(variable);
        }
    }
    
    public String getVariable(int index){
        return index == 0 ? area.getCoord1Variable() : area.getCoord2Variable();
    }
    
    public BlockPos getPos(int index){
        return area.getRawAreaPoint(index);
    }
    
    public void updateAreaFromNBT(NBTTagCompound tag){
        area.readFromNBT(tag);
    }
    
    public Set<BlockPos> getArea(){
        Set<BlockPos> set = new HashSet<BlockPos>();
        area.getArea(set);
        return set;
    }
    
    @Override
    public BlockPos getCoordinate(String varName){
        if(area.getCoord1Variable().equals(varName)){
            return getPos(0);
        }else if(area.getCoord2Variable().equals(varName)){
            return getPos(1);
        }else{
            return null;
        }
    }
    
    public ProgWidgetArea createWidget(){
        return (ProgWidgetArea)area.copy();
    }
    
    public ProgWidgetArea getWidget(){
        return area;
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
