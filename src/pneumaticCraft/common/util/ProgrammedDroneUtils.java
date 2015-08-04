package pneumaticCraft.common.util;

import java.util.List;

import net.minecraft.entity.EntityCreature;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetArea;
import pneumaticCraft.common.progwidgets.ProgWidgetDropItem;
import pneumaticCraft.common.progwidgets.ProgWidgetGoToLocation;
import pneumaticCraft.common.progwidgets.ProgWidgetInventoryExport;
import pneumaticCraft.common.progwidgets.ProgWidgetInventoryImport;
import pneumaticCraft.common.progwidgets.ProgWidgetItemFilter;
import pneumaticCraft.common.progwidgets.ProgWidgetLiquidExport;
import pneumaticCraft.common.progwidgets.ProgWidgetLiquidFilter;
import pneumaticCraft.common.progwidgets.ProgWidgetLiquidImport;
import pneumaticCraft.common.progwidgets.ProgWidgetStart;
import pneumaticCraft.common.progwidgets.ProgWidgetSuicide;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;

public class ProgrammedDroneUtils{
    private static EntityDrone getChargedDispenserUpgradeDrone(World world){
        EntityDrone drone = new EntityDrone(world);

        NBTTagCompound tag = new NBTTagCompound();
        drone.writeEntityToNBT(tag);

        NBTTagList upgradeList = new NBTTagList();
        NBTTagCompound slotEntry = new NBTTagCompound();
        slotEntry.setByte("Slot", (byte)0);
        new ItemStack(Itemss.machineUpgrade, 64, ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE).writeToNBT(slotEntry);
        upgradeList.appendTag(slotEntry);

        slotEntry = new NBTTagCompound();
        slotEntry.setByte("Slot", (byte)1);
        new ItemStack(Itemss.machineUpgrade, 10, ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE).writeToNBT(slotEntry);
        upgradeList.appendTag(slotEntry);

        NBTTagCompound inv = new NBTTagCompound();

        inv.setTag("Items", upgradeList);
        tag.setTag("Inventory", inv);
        tag.setFloat("currentAir", 100000);

        drone.readEntityFromNBT(tag);
        drone.setCustomNameTag(StatCollector.translateToLocal("drone.amadronDeliveryDrone"));

        drone.naturallySpawned = true;//Don't let the drone be dropped when wrenching it.

        return drone;
    }

    public static EntityCreature deliverItemsAmazonStyle(World world, int x, int y, int z, ItemStack... deliveredStacks){
        if(world.isRemote) return null;
        if(deliveredStacks.length == 0) throw new IllegalArgumentException("You need to deliver at least 1 stack!");
        if(deliveredStacks.length > 65) throw new IllegalArgumentException("You can only deliver up to 65 stacks at once!");
        for(ItemStack stack : deliveredStacks) {
            if(stack == null) throw new IllegalArgumentException("You can't supply a null stack to be delivered!");
            if(stack.getItem() == null) throw new IllegalArgumentException("You can't supply a stack with a null item to be delivered!");
        }

        EntityDrone drone = getChargedDispenserUpgradeDrone(world);

        //Program the drone
        int startY = world.getHeightValue(x + 30, z) + 30;
        drone.setPosition(x + 30, startY, z);
        List<IProgWidget> widgets = drone.progWidgets;

        ProgWidgetStart start = new ProgWidgetStart();
        start.setX(92);
        start.setY(41);
        widgets.add(start);

        ProgWidgetInventoryExport export = new ProgWidgetInventoryExport();
        export.setX(92);
        export.setY(52);
        widgets.add(export);

        ProgWidgetDropItem drop = new ProgWidgetDropItem();
        drop.setX(92);
        drop.setY(74);
        widgets.add(drop);

        ProgWidgetGoToLocation gotoPiece = new ProgWidgetGoToLocation();
        gotoPiece.setX(92);
        gotoPiece.setY(96);
        widgets.add(gotoPiece);

        ProgWidgetSuicide suicide = new ProgWidgetSuicide();
        suicide.setX(92);
        suicide.setY(107);
        widgets.add(suicide);

        ProgWidgetArea area = new ProgWidgetArea();
        area.setX(107);
        area.setY(52);
        area.x1 = x;
        area.y1 = y;
        area.z1 = z;
        widgets.add(area);

        area = new ProgWidgetArea();
        area.setX(107);
        area.setY(74);
        area.x1 = x;
        area.z1 = z;
        if(drone.isBlockValidPathfindBlock(x, y, z)) {
            for(int i = 0; i < 5 && drone.isBlockValidPathfindBlock(area.x1, i + y + 1, area.z1); i++) {
                area.y1 = y + i;
            }
        } else {
            area.y1 = world.getHeightValue(x, z) + 10;
            if(!drone.isBlockValidPathfindBlock(area.x1, area.y1, area.z1)) area.y1 = 260;//Worst case scenario, there are definately no blocks here.
        }
        widgets.add(area);

        area = new ProgWidgetArea();
        area.setX(107);
        area.setY(96);
        area.x1 = x + 30;
        area.y1 = startY;
        area.z1 = z;
        widgets.add(area);

        TileEntityProgrammer.updatePuzzleConnections(widgets);

        for(int i = 0; i < deliveredStacks.length; i++) {
            drone.getInventory().setInventorySlotContents(i, deliveredStacks[i].copy());
        }
        world.spawnEntityInWorld(drone);
        return drone;
    }

    public static EntityCreature deliverFluidAmazonStyle(World world, int x, int y, int z, FluidStack deliveredFluid){
        if(world.isRemote) return null;
        if(deliveredFluid == null) throw new IllegalArgumentException("Can't deliver a null FluidStack");
        if(deliveredFluid.amount <= 0) throw new IllegalArgumentException("Can't deliver a FluidStack with an amount of <= 0");

        EntityDrone drone = getChargedDispenserUpgradeDrone(world);

        //Program the drone
        int startY = world.getHeightValue(x + 30, z) + 30;
        drone.setPosition(x + 30, startY, z);
        List<IProgWidget> widgets = drone.progWidgets;

        ProgWidgetStart start = new ProgWidgetStart();
        start.setX(92);
        start.setY(41);
        widgets.add(start);

        ProgWidgetLiquidExport export = new ProgWidgetLiquidExport();
        export.setX(92);
        export.setY(52);
        widgets.add(export);

        ProgWidgetGoToLocation gotoPiece = new ProgWidgetGoToLocation();
        gotoPiece.setX(92);
        gotoPiece.setY(74);
        widgets.add(gotoPiece);

        ProgWidgetSuicide suicide = new ProgWidgetSuicide();
        suicide.setX(92);
        suicide.setY(85);
        widgets.add(suicide);

        ProgWidgetArea area = new ProgWidgetArea();
        area.setX(107);
        area.setY(52);
        area.x1 = x;
        area.y1 = y;
        area.z1 = z;
        widgets.add(area);

        area = new ProgWidgetArea();
        area.setX(107);
        area.setY(74);
        area.x1 = x + 30;
        area.y1 = startY;
        area.z1 = z;
        widgets.add(area);

        TileEntityProgrammer.updatePuzzleConnections(widgets);

        drone.getTank().fill(deliveredFluid, true);
        world.spawnEntityInWorld(drone);
        return drone;
    }

    public static EntityCreature retrieveItemsAmazonStyle(World world, int x, int y, int z, ItemStack... queriedStacks){
        if(world.isRemote) return null;
        if(queriedStacks.length == 0) throw new IllegalArgumentException("You need to query at least 1 stack!");
        if(queriedStacks.length > 65) throw new IllegalArgumentException("You can only query up to 65 stacks at once!");
        for(ItemStack stack : queriedStacks) {
            if(stack == null) throw new IllegalArgumentException("You can't query a null stack!");
            if(stack.getItem() == null) throw new IllegalArgumentException("You can't query a stack with a null item!");
        }

        EntityDrone drone = getChargedDispenserUpgradeDrone(world);

        //Program the drone
        int startY = world.getHeightValue(x + 30, z) + 30;
        drone.setPosition(x + 30, startY, z);
        List<IProgWidget> widgets = drone.progWidgets;

        ProgWidgetStart start = new ProgWidgetStart();
        start.setX(92);
        start.setY(41);
        widgets.add(start);

        int yBase = 52;

        for(ItemStack stack : queriedStacks) {
            ProgWidgetInventoryImport im = new ProgWidgetInventoryImport();
            im.setX(92);
            im.setY(yBase);
            im.setCount(stack.stackSize);
            im.setUseCount(true);
            widgets.add(im);

            ProgWidgetArea area = new ProgWidgetArea();
            area.setX(107);
            area.setY(yBase);
            area.x1 = x;
            area.y1 = y;
            area.z1 = z;
            widgets.add(area);

            ProgWidgetItemFilter filter = new ProgWidgetItemFilter();
            filter.setX(107);
            filter.setY(yBase + 11);
            filter.setFilter(stack);
            filter.useMetadata = true;
            filter.useNBT = true;
            widgets.add(filter);

            yBase += 22;
        }

        ProgWidgetGoToLocation gotoPiece = new ProgWidgetGoToLocation();
        gotoPiece.setX(92);
        gotoPiece.setY(yBase);
        widgets.add(gotoPiece);

        ProgWidgetArea area = new ProgWidgetArea();
        area.setX(107);
        area.setY(yBase);
        area.x1 = x + 30;
        area.y1 = startY;
        area.z1 = z;
        widgets.add(area);

        ProgWidgetSuicide suicide = new ProgWidgetSuicide();
        suicide.setX(92);
        suicide.setY(yBase + 11);
        widgets.add(suicide);

        TileEntityProgrammer.updatePuzzleConnections(widgets);

        world.spawnEntityInWorld(drone);
        return drone;
    }

    public static EntityCreature retrieveFluidAmazonStyle(World world, int x, int y, int z, FluidStack queriedFluid){
        if(world.isRemote) return null;
        if(queriedFluid == null) throw new IllegalArgumentException("Can't query a null FluidStack");
        if(queriedFluid.amount <= 0) throw new IllegalArgumentException("Can't query a FluidStack with an amount of <= 0");

        EntityDrone drone = getChargedDispenserUpgradeDrone(world);

        //Program the drone
        int startY = world.getHeightValue(x + 30, z) + 30;
        drone.setPosition(x + 30, startY, z);
        List<IProgWidget> widgets = drone.progWidgets;

        ProgWidgetStart start = new ProgWidgetStart();
        start.setX(92);
        start.setY(41);
        widgets.add(start);

        int yBase = 52;

        ProgWidgetLiquidImport im = new ProgWidgetLiquidImport();
        im.setX(92);
        im.setY(yBase);
        im.setCount(queriedFluid.amount);
        im.setUseCount(true);
        widgets.add(im);

        ProgWidgetArea area = new ProgWidgetArea();
        area.setX(107);
        area.setY(yBase);
        area.x1 = x;
        area.y1 = y;
        area.z1 = z;
        widgets.add(area);

        ProgWidgetLiquidFilter filter = new ProgWidgetLiquidFilter();
        filter.setX(107);
        filter.setY(yBase + 11);
        filter.setFluid(queriedFluid.getFluid());
        widgets.add(filter);

        yBase += 22;

        ProgWidgetGoToLocation gotoPiece = new ProgWidgetGoToLocation();
        gotoPiece.setX(92);
        gotoPiece.setY(yBase);
        widgets.add(gotoPiece);

        area = new ProgWidgetArea();
        area.setX(107);
        area.setY(yBase);
        area.x1 = x + 30;
        area.y1 = startY;
        area.z1 = z;
        widgets.add(area);

        ProgWidgetSuicide suicide = new ProgWidgetSuicide();
        suicide.setX(92);
        suicide.setY(yBase + 11);
        widgets.add(suicide);

        TileEntityProgrammer.updatePuzzleConnections(widgets);

        world.spawnEntityInWorld(drone);
        return drone;
    }
}
