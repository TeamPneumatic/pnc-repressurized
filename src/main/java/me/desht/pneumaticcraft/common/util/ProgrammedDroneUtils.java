package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.inventory.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.progwidgets.*;
import net.minecraft.entity.EntityCreature;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;

public class ProgrammedDroneUtils {
    private static EntityDrone getChargedDispenserUpgradeDrone(World world, BlockPos pos) {
        EntityDrone drone = new EntityDrone(world, null);

        NBTTagCompound tag = new NBTTagCompound();
        drone.writeEntityToNBT(tag);
        ItemStackHandler upgrades = new ItemStackHandler(9);
        upgrades.setStackInSlot(0, new ItemStack(ItemRegistry.getInstance().getUpgrade(EnumUpgrade.DISPENSER), 64));
        upgrades.setStackInSlot(1, new ItemStack(ItemRegistry.getInstance().getUpgrade(EnumUpgrade.SPEED), 10));
        tag.setTag(ChargeableItemHandler.NBT_UPGRADE_TAG, upgrades.serializeNBT());
        tag.setTag("Inventory", new NBTTagCompound());
        tag.setFloat("currentAir", 100000);
        drone.readEntityFromNBT(tag);

        // FIXME: we really need to get a clientside localization here (on the server side)
        drone.setCustomNameTag(net.minecraft.util.text.translation.I18n.translateToLocal("drone.amadronDeliveryDrone"));

        drone.naturallySpawned = true; // Don't let the drone be dropped when wrenching it.

        int startY = world.getHeight(pos.add(30, 0, 0)).getY() + 27 + world.rand.nextInt(6);
        drone.setPosition(pos.getX() + 27 + world.rand.nextInt(6), startY, pos.getZ() + world.rand.nextInt(6) - 3);

        return drone;
    }

    public static EntityCreature deliverItemsAmazonStyle(World world, BlockPos pos, ItemStack... deliveredStacks) {
        if (world.isRemote) return null;
        Validate.isTrue(deliveredStacks.length > 0 && deliveredStacks.length <= 65, "You can only deliver between 0 & 65 stacks at once!");
        Arrays.stream(deliveredStacks).forEach(stack -> Validate.isTrue(!stack.isEmpty(), "You can't supply an empty stack to be delivered!"));

        EntityDrone drone = getChargedDispenserUpgradeDrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        builder.add(new ProgWidgetInventoryExport(), ProgWidgetArea.fromPosition(pos));
        ProgWidgetArea area = ProgWidgetArea.fromPosition(pos);
        if (drone.isBlockValidPathfindBlock(pos)) {
            for (int i = 0; i < 5 && drone.isBlockValidPathfindBlock(new BlockPos(area.x1, area.y1, area.z1)); i++) {
                area.y1 = pos.getY() + i;
            }
        } else {
            area.y1 = world.getHeight(pos).getY() + 10;
            if (!drone.isBlockValidPathfindBlock(new BlockPos(area.x1, area.y1, area.z1)))
                area.y1 = 260; // Worst case scenario; there are definitely no blocks here.
        }
        builder.add(new ProgWidgetDropItem(), area);
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.getPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        for (int i = 0; i < deliveredStacks.length; i++) {
            drone.getInv().setStackInSlot(i, deliveredStacks[i].copy());
        }
        world.spawnEntity(drone);
        return drone;
    }

    public static EntityCreature deliverFluidAmazonStyle(World world, BlockPos pos, FluidStack deliveredFluid) {
        if (world.isRemote) return null;
        Validate.notNull(deliveredFluid, "Can't deliver a null FluidStack");
        Validate.isTrue(deliveredFluid.amount > 0, "Can't deliver a FluidStack with an amount of <= 0");

        EntityDrone drone = getChargedDispenserUpgradeDrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        builder.add(new ProgWidgetLiquidExport(), ProgWidgetArea.fromPosition(pos));
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.getPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        drone.getTank().fill(deliveredFluid, true);
        world.spawnEntity(drone);
        return drone;
    }

    public static EntityCreature retrieveItemsAmazonStyle(World world, BlockPos pos, ItemStack... queriedStacks) {
        if (world.isRemote) return null;
        Validate.isTrue(queriedStacks.length > 0 && queriedStacks.length <= 65, "Must retrieve between 1 & 65 itemstacks!");
        Arrays.stream(queriedStacks).forEach(stack -> Validate.isTrue(!stack.isEmpty(), "Cannot retrieve an empty stack!"));

        EntityDrone drone = getChargedDispenserUpgradeDrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        for (ItemStack stack : queriedStacks) {
            ProgWidgetInventoryImport widgetImport = new ProgWidgetInventoryImport();
            widgetImport.setUseCount(true);
            widgetImport.setCount(stack.getCount());
            ProgWidgetItemFilter filter = ProgWidgetItemFilter.withFilter(stack);
            filter.useMetadata = true;
            filter.useNBT = true;
            builder.add(widgetImport, ProgWidgetArea.fromPosition(pos), filter);
        }
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.getPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        world.spawnEntity(drone);
        return drone;
    }

    public static EntityCreature retrieveFluidAmazonStyle(World world, BlockPos pos, FluidStack queriedFluid) {
        if (world.isRemote) return null;
        Validate.notNull(queriedFluid, "Can't retrieve a null FluidStack");
        Validate.isTrue(queriedFluid.amount > 0, "Can't retrieve a FluidStack with an amount of <= 0");

        EntityDrone drone = getChargedDispenserUpgradeDrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        ProgWidgetLiquidImport liquidImport = new ProgWidgetLiquidImport();
        liquidImport.setUseCount(true);
        liquidImport.setCount(queriedFluid.amount);
        builder.add(liquidImport, ProgWidgetArea.fromPosition(pos), ProgWidgetLiquidFilter.withFilter(queriedFluid.getFluid()));
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.getPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        world.spawnEntity(drone);
        return drone;
    }
}
