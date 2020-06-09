package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerKeroseneLamp;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.block.BlockKeroseneLamp.LIT;

public class TileEntityKeroseneLamp extends TileEntityTickableBase implements IRedstoneControlled, ISerializableTanks, INamedContainerProvider {

    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "pneumaticcraft.gui.tab.redstoneBehaviour.button.anySignal",
            "pneumaticcraft.gui.tab.redstoneBehaviour.button.highSignal",
            "pneumaticcraft.gui.tab.redstoneBehaviour.button.lowSignal",
            "pneumaticcraft.gui.tab.redstoneBehaviour.keroseneLamp.button.interpolate"
    );

    public static final int INVENTORY_SIZE = 2;

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int LIGHT_SPACING = 3;
    public static final int MAX_RANGE = 30;

    private final Set<BlockPos> managingLights = new HashSet<>();
    private boolean isOn;
    @GuiSynced
    private int range;
    @GuiSynced
    private int targetRange = 10;
    @GuiSynced
    private int redstoneMode;
    @GuiSynced
    private int fuel;
    private int checkingX, checkingY, checkingZ;

    @DescSynced
    @GuiSynced
    private final SmartSyncTank tank = new SmartSyncTank(this, 2000) {
        private FluidStack prevFluid = FluidStack.EMPTY;
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            if (prevFluid.getFluid() != fluid.getFluid()) {
                recalculateFuelQuality();
            }
            prevFluid = fluid;
        }
    };
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> tank);

    @DescSynced
    private float fuelQuality = -1f; // the quality of the liquid currently in the tank; basically, its burn time

    private final ItemStackHandler inventory = new BaseItemStackHandler(this, INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || FluidUtil.getFluidHandler(itemStack).isPresent();
        }
    };
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> inventory);


    public TileEntityKeroseneLamp() {
        super(ModTileEntities.KEROSENE_LAMP.get());
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return inventoryCap;
    }

    @Override
    public void tick() {
        super.tick();

        tank.tick();

        if (!getWorld().isRemote) {
            if (fuelQuality < 0) recalculateFuelQuality();
            processFluidItem(INPUT_SLOT, OUTPUT_SLOT);
            if (getWorld().getGameTime() % 5 == 0) {
                int realTargetRange = redstoneAllows() && fuel > 0 ? targetRange : 0;
                if (redstoneMode == 3) realTargetRange = (int) (poweredRedstone / 15D * targetRange);
                updateRange(Math.min(realTargetRange, tank.getFluidAmount())); //Fade out the lamp when almost empty.
                updateLights();
                useFuel();
            }
        } else {
            if (getBlockState().get(LIT) && getWorld().rand.nextInt(10) == 0) {
                getWorld().addParticle(ParticleTypes.FLAME, getPos().getX() + 0.4 + 0.2 * getWorld().rand.nextDouble(), getPos().getY() + 0.2 + tank.getFluidAmount() / 1000D * 3 / 16D, getPos().getZ() + 0.4 + 0.2 * getWorld().rand.nextDouble(), 0, 0, 0);
            }
        }
    }

    private void recalculateFuelQuality() {
        if (!tank.isEmpty()) {
            if (PNCConfig.Common.Machines.keroseneLampCanUseAnyFuel) {
                // 110 comes from kerosene's fuel value of 1,100,000 divided by the old FUEL_PER_MB value (10000)
                fuelQuality = PneumaticRegistry.getInstance().getFuelRegistry().getFuelValue(tank.getFluid().getFluid()) / 110f;
            } else {
                fuelQuality = tank.getFluid().getFluid() == ModFluids.KEROSENE.get() ? 10000f : 0f;
            }
            fuelQuality *= PNCConfig.Common.Machines.keroseneLampFuelEfficiency;
        }
    }

    private void useFuel() {
        if (fuelQuality == 0) return; // tank is empty or a non-burnable liquid in the tank
        fuel -= range * range * 3;// * range;
        while (fuel <= 0 && !tank.drain(1, IFluidHandler.FluidAction.EXECUTE).isEmpty()) {
            fuel += fuelQuality;
        }
        if (fuel < 0) fuel = 0;
    }

    @Override
    public void validate() {
        super.validate();
        checkingX = getPos().getX();
        checkingY = getPos().getY();
        checkingZ = getPos().getZ();
    }

    @Override
    public void remove() {
        super.remove();
        for (BlockPos pos : managingLights) {
            if (isLampLight(pos)) {
                getWorld().removeBlock(pos, false);
            }
        }
    }

    private boolean isLampLight(BlockPos pos) {
        return getWorld().getBlockState(pos).getBlock() == ModBlocks.KEROSENE_LAMP_LIGHT.get();
    }

    private void updateLights() {
        int roundedRange = range / LIGHT_SPACING * LIGHT_SPACING;
        checkingX += LIGHT_SPACING;
        if (checkingX > getPos().getX() + roundedRange) {
            checkingX = getPos().getX() - roundedRange;
            checkingY += LIGHT_SPACING;
            if (checkingY > getPos().getY() + roundedRange) {
                checkingY = getPos().getY() - roundedRange;
                checkingZ += LIGHT_SPACING;
                if (checkingZ > getPos().getZ() + roundedRange) checkingZ = getPos().getZ() - roundedRange;
            }
        }
        BlockPos pos = new BlockPos(checkingX, checkingY, checkingZ);
        BlockPos lampPos = new BlockPos(getPos().getX(), getPos().getY(), getPos().getZ());
        if (managingLights.contains(pos)) {
            if (isLampLight(pos)) {
                if (!passesRaytraceTest(pos, lampPos)) {
                    getWorld().removeBlock(pos, false);
                    managingLights.remove(pos);
                }
            } else {
                managingLights.remove(pos);
            }
        } else {
            tryAddLight(pos, lampPos);
        }
    }

    private void updateRange(int targetRange) {
        if (targetRange > range) {
            range++;
            BlockPos lampPos = new BlockPos(getPos().getX(), getPos().getY(), getPos().getZ());
            int roundedRange = range / LIGHT_SPACING * LIGHT_SPACING;
            for (int x = -roundedRange; x <= roundedRange; x += LIGHT_SPACING) {
                for (int y = -roundedRange; y <= roundedRange; y += LIGHT_SPACING) {
                    for (int z = -roundedRange; z <= roundedRange; z += LIGHT_SPACING) {
                        BlockPos pos = new BlockPos(x + getPos().getX(), y + getPos().getY(), z + getPos().getZ());
                        if (!managingLights.contains(pos)) {
                            tryAddLight(pos, lampPos);
                        }
                    }
                }
            }
        } else if (targetRange < range) {
            range--;
            Iterator<BlockPos> iterator = managingLights.iterator();
            BlockPos lampPos = new BlockPos(getPos().getX(), getPos().getY(), getPos().getZ());
            while (iterator.hasNext()) {
                BlockPos pos = iterator.next();
                if (!isLampLight(pos)) {
                    iterator.remove();
                } else if (PneumaticCraftUtils.distBetween(pos, lampPos) > range) {
                    getWorld().removeBlock(pos, false);
                    iterator.remove();
                }
            }
        }
        boolean wasOn = isOn;
        isOn = range > 0;
        if (isOn != wasOn) {
            world.getChunkProvider().getLightManager().checkBlock(pos);
            world.setBlockState(pos, getBlockState().with(LIT, isOn));
        }
    }

    public boolean isOn() {
        return isOn;
    }

    private boolean passesRaytraceTest(BlockPos pos, BlockPos lampPos) {
        // must be run on server!
        RayTraceContext ctx = new RayTraceContext(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), new Vec3d(lampPos.getX() + 0.5, lampPos.getY() + 0.5, lampPos.getZ() + 0.5), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, FakePlayerFactory.getMinecraft((ServerWorld) getWorld()));
        BlockRayTraceResult rtr = getWorld().rayTraceBlocks(ctx);
        return rtr.getType() == RayTraceResult.Type.BLOCK && rtr.getPos().equals(lampPos);
    }

    private void tryAddLight(BlockPos pos, BlockPos lampPos) {
        if (!PNCConfig.Common.Advanced.disableKeroseneLampFakeAirBlock && PneumaticCraftUtils.distBetween(pos, lampPos) <= range) {
            if (getWorld().isAirBlock(pos) && !isLampLight(pos)) {
                if (passesRaytraceTest(pos, lampPos)) {
                    getWorld().setBlockState(pos, ModBlocks.KEROSENE_LAMP_LIGHT.get().getDefaultState());
                    managingLights.add(pos);
                }
            }
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public void onDescUpdate() {
        getWorld().getChunkProvider().getLightManager().checkBlock(getPos());
        super.onDescUpdate();
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.put("lights", managingLights.stream().map(NBTUtil::writeBlockPos).collect(Collectors.toCollection(ListNBT::new)));
        tag.putByte("redstoneMode", (byte) redstoneMode);
        tag.putByte("targetRange", (byte) targetRange);
        tag.putByte("range", (byte) range);
        tag.put("Items", inventory.serializeNBT());
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        managingLights.clear();
        ListNBT lights = tag.getList("lights", 10);
        for (int i = 0; i < lights.size(); i++) {
            managingLights.add(NBTUtil.readBlockPos(lights.getCompound(i)));
        }
        recalculateFuelQuality();
        redstoneMode = tag.getByte("redstoneMode");
        targetRange = tag.getByte("targetRange");
        range = tag.getByte("range");
        inventory.deserializeNBT(tag.getCompound("Items"));
    }

    @Override
    public boolean redstoneAllows() {
        return redstoneMode == 3 || super.redstoneAllows();
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 3) redstoneMode = 0;
        } else {
            try {
                targetRange = MathHelper.clamp(Integer.parseInt(tag), 1, MAX_RANGE);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public SmartSyncTank getTank() {
        return tank;
    }

    public int getRange() {
        return range;
    }

    public int getTargetRange() {
        return targetRange;
    }

    public int getFuel() {
        return fuel;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction facing) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidCap.cast();
        } else {
            return super.getCapability(cap, facing);
        }
    }

    public float getFuelQuality() {
        return fuelQuality;
    }

    @Override
    protected List<String> getRedstoneButtonLabels() {
        return REDSTONE_LABELS;
    }

    @Nonnull
    @Override
    public Map<String, FluidTank> getSerializableTanks() {
        return ImmutableMap.of("Tank", tank);
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerKeroseneLamp(i, playerInventory, getPos());
    }
}
