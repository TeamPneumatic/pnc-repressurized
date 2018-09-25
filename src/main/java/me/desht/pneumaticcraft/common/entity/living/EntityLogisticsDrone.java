package me.desht.pneumaticcraft.common.entity.living;

import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetLogistics;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class EntityLogisticsDrone extends EntityBasicDrone {

    public EntityLogisticsDrone(World world) {
        super(world);
    }

    public EntityLogisticsDrone(World world, EntityPlayer player) {
        super(world, player);
    }

    @Override
    protected Item getDroneItem(){
        return Itemss.LOGISTICS_DRONE;
    }

    @Override
    public void addProgram(BlockPos clickPos, EnumFacing facing, BlockPos pos, List<IProgWidget> widgets) {
        addBasicProgram(pos, widgets, new ProgWidgetLogistics());
    }
    
}
