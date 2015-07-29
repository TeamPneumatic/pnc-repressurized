package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.entity.living.EntityLogisticsDrone;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetArea;
import pneumaticCraft.common.progwidgets.ProgWidgetLogistics;
import pneumaticCraft.common.progwidgets.ProgWidgetStart;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;

public class ItemLogisticsDrone extends ItemDrone{

    public ItemLogisticsDrone(){
        setMaxStackSize(64);
    }

    @Override
    public boolean onItemUse(ItemStack iStack, EntityPlayer player, World world, int x, int y, int z, int side, float vecX, float vecY, float vecZ){
        if(!world.isRemote) {
            EntityDrone drone = new EntityLogisticsDrone(world, player);
            ForgeDirection dir = ForgeDirection.getOrientation(side);
            drone.setPosition(x + 0.5 + dir.offsetX, y + 0.5 + dir.offsetY, z + 0.5 + dir.offsetZ);
            world.spawnEntityInWorld(drone);

            NBTTagCompound stackTag = iStack.getTagCompound();
            NBTTagCompound entityTag = new NBTTagCompound();
            drone.writeEntityToNBT(entityTag);
            if(stackTag != null) {
                entityTag.setFloat("currentAir", stackTag.getFloat("currentAir"));
                entityTag.setInteger("color", stackTag.getInteger("color"));
                NBTTagCompound invTag = stackTag.getCompoundTag("UpgradeInventory");
                if(invTag != null) entityTag.setTag("Inventory", invTag.copy());
            }
            drone.readEntityFromNBT(entityTag);
            addLogisticsProgram(x, y, z, drone.progWidgets);
            if(iStack.hasDisplayName()) drone.setCustomNameTag(iStack.getDisplayName());

            drone.naturallySpawned = false;
            drone.onSpawnWithEgg(null);
            iStack.stackSize--;
        }
        return true;
    }

    private void addLogisticsProgram(int x, int y, int z, List<IProgWidget> widgets){
        ProgWidgetStart start = new ProgWidgetStart();
        start.setX(0);
        start.setY(0);
        widgets.add(start);

        ProgWidgetLogistics logistics = new ProgWidgetLogistics();
        logistics.setX(0);
        logistics.setY(11);
        widgets.add(logistics);

        ProgWidgetArea area = new ProgWidgetArea();
        area.setX(15);
        area.setY(11);
        area.x1 = x - 16;
        area.y1 = y - 16;
        area.z1 = z - 16;
        area.x2 = x + 16;
        area.y2 = y + 16;
        area.z2 = z + 16;
        widgets.add(area);
        TileEntityProgrammer.updatePuzzleConnections(widgets);
    }

    @Override
    public boolean canProgram(ItemStack stack){
        return false;
    }
}
