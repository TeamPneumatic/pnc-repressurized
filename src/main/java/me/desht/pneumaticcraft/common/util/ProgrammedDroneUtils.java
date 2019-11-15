package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModEntityTypes;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.inventory.handler.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.progwidgets.*;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;

public class ProgrammedDroneUtils {
    /**
     * Create a delivery drone: 10 speed upgrades, 64 inventory upgrades, 100000mL of air
     *
     * @param world the world
     * @param pos drone's position
     * @return the delivery drone
     */
    private static EntityDrone makeDeliveryDrone(World world, BlockPos pos) {
        EntityDrone drone = new EntityDrone(ModEntityTypes.DRONE, world, null);

        CompoundNBT tag = new CompoundNBT();
        drone.writeAdditional(tag);
        ItemStackHandler upgrades = new ItemStackHandler(9);
        upgrades.setStackInSlot(0, new ItemStack(EnumUpgrade.INVENTORY.getItem(), 64));
        upgrades.setStackInSlot(1, new ItemStack(EnumUpgrade.SPEED.getItem(), 10));
        tag.put(ChargeableItemHandler.NBT_UPGRADE_TAG, upgrades.serializeNBT());
        tag.put("Inventory", new CompoundNBT());
        tag.putFloat("currentAir", 100000);
        drone.readAdditional(tag);

        drone.setCustomName(new TranslationTextComponent("drone.amadronDeliveryDrone"));

        drone.naturallySpawned = true; // Don't let the drone be dropped when wrenching it.

        int startY = world.getHeight(Heightmap.Type.WORLD_SURFACE, pos.add(30, 0, 0)).getY() + 27 + world.rand.nextInt(6);
        drone.setPosition(pos.getX() + 27 + world.rand.nextInt(6), startY, pos.getZ() + world.rand.nextInt(6) - 3);

        return drone;
    }

    public static CreatureEntity deliverItemsAmazonStyle(GlobalPos gPos, ItemStack... deliveredStacks) {
        World world = PneumaticCraftUtils.getWorldForGlobalPos(gPos);
        BlockPos pos = gPos.getPos();

        if (world == null || world.isRemote) return null;
        Validate.isTrue(deliveredStacks.length > 0 && deliveredStacks.length <= 65,
                "You can only deliver between 0 & 65 stacks at once!");
        Arrays.stream(deliveredStacks).forEach(stack -> Validate.isTrue(!stack.isEmpty(),
                "You can't supply an empty stack to be delivered!"));

        EntityDrone drone = makeDeliveryDrone(world, pos);

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
            area.y1 = world.getHeight(Heightmap.Type.WORLD_SURFACE, pos).getY() + 10;
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
        world.addEntity(drone);
        return drone;
    }

    public static CreatureEntity deliverFluidAmazonStyle(GlobalPos gPos, FluidStack deliveredFluid) {
        World world = PneumaticCraftUtils.getWorldForGlobalPos(gPos);
        BlockPos pos = gPos.getPos();

        if (world == null || world.isRemote) return null;
        Validate.notNull(deliveredFluid, "Can't deliver a null FluidStack");
        Validate.isTrue(deliveredFluid.getAmount() > 0, "Can't deliver a FluidStack with an amount of <= 0");

        EntityDrone drone = makeDeliveryDrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        builder.add(new ProgWidgetLiquidExport(), ProgWidgetArea.fromPosition(pos));
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.getPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        drone.getTank().fill(deliveredFluid, IFluidHandler.FluidAction.EXECUTE);
        world.addEntity(drone);
        return drone;
    }

    public static CreatureEntity retrieveItemsAmazonStyle(GlobalPos gPos, ItemStack... queriedStacks) {
        World world = PneumaticCraftUtils.getWorldForGlobalPos(gPos);
        BlockPos pos = gPos.getPos();

        if (world == null || world.isRemote) return null;
        Validate.isTrue(queriedStacks.length > 0 && queriedStacks.length <= 65, "Must retrieve between 1 & 65 itemstacks!");
        Arrays.stream(queriedStacks).forEach(stack -> Validate.isTrue(!stack.isEmpty(), "Cannot retrieve an empty stack!"));

        EntityDrone drone = makeDeliveryDrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        for (ItemStack stack : queriedStacks) {
            ProgWidgetInventoryImport widgetImport = new ProgWidgetInventoryImport();
            widgetImport.setUseCount(true);
            widgetImport.setCount(stack.getCount());
            ProgWidgetItemFilter filter = ProgWidgetItemFilter.withFilter(stack);
            filter.useItemDamage = true;
            filter.useNBT = true;
            builder.add(widgetImport, ProgWidgetArea.fromPosition(pos), filter);
        }
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.getPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        world.addEntity(drone);
        return drone;
    }

    public static CreatureEntity retrieveFluidAmazonStyle(GlobalPos gPos, FluidStack queriedFluid) {
        World world = PneumaticCraftUtils.getWorldForGlobalPos(gPos);
        BlockPos pos = gPos.getPos();

        if (world == null || world.isRemote) return null;
        Validate.notNull(queriedFluid, "Can't retrieve a null FluidStack");
        Validate.isTrue(queriedFluid.getAmount() > 0, "Can't retrieve a FluidStack with an amount of <= 0");

        EntityDrone drone = makeDeliveryDrone(world, pos);

        // Program the drone
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        ProgWidgetLiquidImport liquidImport = new ProgWidgetLiquidImport();
        liquidImport.setUseCount(true);
        liquidImport.setCount(queriedFluid.getAmount());
        builder.add(liquidImport, ProgWidgetArea.fromPosition(pos), ProgWidgetLiquidFilter.withFilter(queriedFluid.getFluid()));
        builder.add(new ProgWidgetGoToLocation(), ProgWidgetArea.fromPosition(drone.getPosition()));
        builder.add(new ProgWidgetSuicide());
        drone.progWidgets.addAll(builder.build());

        world.addEntity(drone);
        return drone;
    }
}
