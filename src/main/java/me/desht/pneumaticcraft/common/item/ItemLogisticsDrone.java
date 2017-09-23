package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityLogisticsDrone;
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
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack iStack = player.getHeldItem(hand);
        if (!world.isRemote) {
            EntityDrone drone = new EntityLogisticsDrone(world, player);

            BlockPos placePos = pos.offset(facing);
            drone.setPosition(placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5);
            world.spawnEntity(drone);

            NBTTagCompound stackTag = iStack.getTagCompound();
            NBTTagCompound entityTag = new NBTTagCompound();
            drone.writeEntityToNBT(entityTag);
            if (stackTag != null) {
                entityTag.setFloat("currentAir", stackTag.getFloat("currentAir"));
                entityTag.setInteger("color", stackTag.getInteger("color"));
                NBTTagCompound invTag = stackTag.getCompoundTag("UpgradeInventory");
                entityTag.setTag("Inventory", invTag.copy());
            }
            drone.readEntityFromNBT(entityTag);
            addLogisticsProgram(pos, drone.progWidgets);
            if (iStack.hasDisplayName()) drone.setCustomNameTag(iStack.getDisplayName());

            drone.naturallySpawned = false;
            //TODO 1.8 check if valid replacement drone.onSpawnWithEgg(null);
            drone.onInitialSpawn(world.getDifficultyForLocation(placePos), null);
            iStack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
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
