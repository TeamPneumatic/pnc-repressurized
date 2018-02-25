package me.desht.pneumaticcraft.common.entity.living;

import java.util.List;

import me.desht.pneumaticcraft.common.inventory.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryImport;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetLogistics;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetStart;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.DroneProgramBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Base class for all pre-programmed (and not programmable) drones.
 * @author MineMaarten
 *
 */
public abstract class EntityBasicDrone extends EntityDrone {

    public EntityBasicDrone(World world) {
        super(world);
    }

    public EntityBasicDrone(World world, EntityPlayer player) {
        super(world, player);
    }

    @Override
    protected ItemStack getDroppedStack() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setFloat("currentAir", currentAir);
        tag.setInteger("color", getDroneColor());
        NBTTagCompound invTag = new NBTTagCompound();
        writeEntityToNBT(invTag);
        tag.setTag(ChargeableItemHandler.NBT_UPGRADE_TAG, invTag.getTag(ChargeableItemHandler.NBT_UPGRADE_TAG));
        ItemStack drone = new ItemStack(getDroneItem());
        drone.setTagCompound(tag);
        return drone;
    }
    
    protected abstract Item getDroneItem();
    
    public abstract void addProgram(BlockPos clickPos, EnumFacing facing, BlockPos pos, List<IProgWidget> widgets);
    
    public void addBasicProgram(BlockPos pos, List<IProgWidget> widgets, IProgWidget mainProgram) {
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        builder.add(mainProgram, standard16x16x16Area(pos));
        widgets.addAll(builder.build());
    }
    
    protected static ProgWidgetArea standard16x16x16Area(BlockPos centerPos){
        return ProgWidgetArea.fromPositions(centerPos.add(-16, -16, -16), centerPos.add(16, 16, 16));
    }
   
}
