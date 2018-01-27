package me.desht.pneumaticcraft.common.progwidgets.area;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Sets;

import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea.EnumAreaType;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AreaTypeRandom extends AreaType{

    public static final String ID = "random";
    private int pickedAmount;
    
    public AreaTypeRandom(){
        super(ID);
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
        int size = (maxX - minX) * (maxY - minY) * (maxZ - minZ);
        
        if (pickedAmount >= size) { //If we pick >= than there are blocks, just pick all blocks
            BlockPos.getAllInBox(minX, minY, minZ, maxX, maxY, maxZ).forEach(areaAdder);
        }else{
            Set<BlockPos> filledArea = new HashSet<>(size);
            BlockPos.getAllInBox(minX, minY, minZ, maxX, maxY, maxZ).forEach(p -> filledArea.add(p));
            
            Random rand = new Random();
            Set<Integer> randomIndexes = new HashSet<Integer>();
            while (randomIndexes.size() < pickedAmount) {
                randomIndexes.add(rand.nextInt(filledArea.size()));
            }
            int curIndex = 0;
            for (BlockPos pos : filledArea) {
                if (randomIndexes.contains(curIndex)) areaAdder.accept(pos);
                curIndex++;
            }
        }
    }
    
    @Override
    public boolean isDeterministic(){
        return false;
    }
    
    @Override
    public void addUIWidgets(List<AreaTypeWidget> widgets){
        super.addUIWidgets(widgets);
        widgets.add(new AreaTypeWidgetInteger("gui.progWidget.area.type.random.blocksSelected", () -> pickedAmount, amount -> pickedAmount = amount));
    }
    
    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setInteger("pickedAmount", pickedAmount);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        pickedAmount = tag.getInteger("pickedAmount");
    }
    
    @Override
    public void convertFromLegacy(EnumAreaType oldAreaType, int typeInfo){
        pickedAmount = typeInfo;
    }
}
