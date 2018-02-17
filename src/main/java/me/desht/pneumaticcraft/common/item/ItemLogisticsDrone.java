package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityLogisticsDrone;
import me.desht.pneumaticcraft.common.inventory.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetLogistics;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetStart;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemLogisticsDrone extends ItemDrone {

    public ItemLogisticsDrone() {
        super("logistic_drone");
        setMaxStackSize(64);
    }

    @Override
    public void spawnDrone(EntityPlayer player, World world, BlockPos placePos, ItemStack iStack){
        EntityDrone drone = new EntityLogisticsDrone(world, player);

        drone.setPosition(placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5);
        world.spawnEntity(drone);

        NBTTagCompound stackTag = iStack.getTagCompound();
        NBTTagCompound entityTag = new NBTTagCompound();
        drone.writeEntityToNBT(entityTag);
        if (stackTag != null) {
            entityTag.setFloat("currentAir", stackTag.getFloat("currentAir"));
            entityTag.setInteger("color", stackTag.getInteger("color"));
            entityTag.setTag(ChargeableItemHandler.NBT_UPGRADE_TAG, stackTag.getCompoundTag(ChargeableItemHandler.NBT_UPGRADE_TAG));
        }
        drone.readEntityFromNBT(entityTag);
        addLogisticsProgram(placePos, drone.progWidgets);
        if (iStack.hasDisplayName()) drone.setCustomNameTag(iStack.getDisplayName());

        drone.naturallySpawned = false;
        //TODO 1.8 check if valid replacement drone.onSpawnWithEgg(null);
        drone.onInitialSpawn(world.getDifficultyForLocation(placePos), null);
    }

    private void addLogisticsProgram(BlockPos pos, List<IProgWidget> widgets) {
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
        area.x1 = pos.getX() - 16;
        area.y1 = pos.getY() - 16;
        area.z1 = pos.getZ() - 16;
        area.x2 = pos.getX() + 16;
        area.y2 = pos.getY() + 16;
        area.z2 = pos.getZ() + 16;
        widgets.add(area);
        TileEntityProgrammer.updatePuzzleConnections(widgets);
    }

    @Override
    public boolean canProgram(ItemStack stack) {
        return false;
    }
}
