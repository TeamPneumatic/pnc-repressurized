package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.ai.ChunkPositionSorter;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerGasLift;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TileEntityGasLift extends TileEntityPneumaticBase
        implements IMinWorkingPressure, IRedstoneControlled, ISerializableTanks, IAutoFluidEjecting, INamedContainerProvider {
    private static final int INVENTORY_SIZE = 1;

    public enum Status {
        IDLE("idling"), PUMPING("pumping"), DIGGING("diggingDown"), RETRACTING("retracting"), STUCK("stuck");

        public final String desc;
        Status(String desc) {
            this.desc = desc;
        }
    }

    public enum PumpMode {
        PUMP_EMPTY, PUMP_LEAVE_FLUID, RETRACT
    }

    @GuiSynced
    private final FluidTank tank = new GasLiftFluidTank();
    private LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> tank);

    private final ItemStackHandler inventory = new BaseItemStackHandler(this, INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() == ModBlocks.DRILL_PIPE.asItem();
        }
    };
    private final LazyOptional<IItemHandlerModifiable> inventoryCap = LazyOptional.of(() -> inventory);
    @GuiSynced
    public int currentDepth;
    @GuiSynced
    public int redstoneMode;
    @GuiSynced
    public PumpMode pumpMode = PumpMode.PUMP_EMPTY;
    @GuiSynced
    public Status status = Status.IDLE;
    private int workTimer;
    private int ticker;
    private List<BlockPos> pumpingLake;
    private static final int MAX_PUMP_RANGE_SQUARED = 15 * 15;

    public TileEntityGasLift() {
        super(ModTileEntityTypes.GAS_LIFT, 5, 7, 3000, 4);

        addApplicableUpgrade(IItemRegistry.EnumUpgrade.SPEED, IItemRegistry.EnumUpgrade.DISPENSER);
    }

    @Override
    public boolean canConnectPneumatic(Direction d) {
        return d != Direction.DOWN;
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isRemote) {
            ticker++;
            if (currentDepth > 0) {
                int curCheckingPipe = ticker % currentDepth;
                if (curCheckingPipe > 0 && !isPipe(world, getPos().offset(Direction.DOWN, curCheckingPipe))) {
                    currentDepth = curCheckingPipe - 1;
                }
            }
            if (ticker == 400) {
                pumpingLake = null;
                ticker = 0;
            }

            if (redstoneAllows() && getPressure() >= getMinWorkingPressure()) {
                workTimer += this.getSpeedMultiplierFromUpgrades();
                while (workTimer > 20) {
                    workTimer -= 20;
                    status = Status.IDLE;
                    if (pumpMode == PumpMode.RETRACT) {
                        retractPipes();
                    } else {
                        if (!suckLiquid() && !tryDigDown()) {
                            break;
                        }
                    }
                }
            } else {
                status = Status.IDLE;
            }
        }
    }

    private void retractPipes() {
        if (currentDepth > 0) {
            status = Status.RETRACTING;
            if (isPipe(world, getPos().add(0, -currentDepth, 0))) {
                BlockPos pos1 = getPos().offset(Direction.DOWN, currentDepth);
                ItemStack toInsert = new ItemStack(world.getBlockState(pos1).getBlock());
                if (inventory.insertItem(0, toInsert, true).isEmpty()) {
                    inventory.insertItem(0, toInsert, false);
                    world.destroyBlock(pos1, false);
                    addAir(-100);
                    currentDepth--;
                } else {
                    status = Status.IDLE;
                }
            } else {
                currentDepth--;
            }
        }
    }

    private boolean tryDigDown() {
        if (isUnbreakable(getPos().offset(Direction.DOWN, currentDepth + 1))) {
            status = Status.STUCK;
        } else if (getPos().getY() - currentDepth >= 0) {
            status = Status.DIGGING;
            currentDepth++;
            BlockPos pos1 = getPos().offset(Direction.DOWN, currentDepth);
            if (!isPipe(world, pos1)) {
                ItemStack extracted = inventory.extractItem(0, 1, true);
                if (extracted.getItem() == ModBlocks.DRILL_PIPE.asItem()) {
                    BlockState currentState = world.getBlockState(pos1);
                    BlockState newState = ((BlockItem) extracted.getItem()).getBlock().getDefaultState();

                    int airRequired = Math.round(66.66f * currentState.getBlockHardness(world, pos1));
                    if (airHandler.getAir() > airRequired) {
                        inventory.extractItem(0, 1, false);
                        world.destroyBlock(pos1, false);
                        world.setBlockState(pos1, newState);
                        // kludge: don't permit placing more than one tube per tick
                        // causes TE cache problems - root cause to be determined
                        workTimer = 19;
                        addAir(-airRequired);
                    } else {
                        status = Status.IDLE;
                        currentDepth--;
                    }
                } else {
                    status = Status.IDLE;
                    currentDepth--;
                }
            }
        } else {
            status = Status.IDLE;
        }
        return status == Status.DIGGING;
    }

    private boolean isPipe(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == ModBlocks.DRILL_PIPE;
    }

    private boolean isUnbreakable(BlockPos pos) {
        return getWorld().getBlockState(pos).getBlockHardness(world, pos) < 0;
    }

    private boolean suckLiquid() {
        BlockPos pos = getPos().offset(Direction.DOWN, currentDepth + 1);

        IFluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid() == Fluids.EMPTY) {
            pumpingLake = null;
            return false;
        }
        FluidStack fluidStack = new FluidStack(fluidState.getFluid(), FluidAttributes.BUCKET_VOLUME);

        if (tank.fill(fluidStack, FluidAction.SIMULATE) == fluidStack.getAmount()) {
            if (pumpingLake == null) {
                findLake(fluidStack.getFluid());
            }
            boolean foundSource = false;
            BlockPos curPos = null;
            while (pumpingLake.size() > 0) {
                curPos = pumpingLake.get(0);
                if (FluidUtils.isSourceBlock(getWorld(), curPos, fluidStack.getFluid())) {
                    foundSource = true;
                    break;
                }
                pumpingLake.remove(0);
            }
            if (pumpingLake.isEmpty()) {
                pumpingLake = null;
            } else if (foundSource) {
                FluidStack taken = FluidUtils.tryPickupFluid(fluidCap, world, curPos, false, FluidAction.EXECUTE);
                if (taken.getAmount() == FluidAttributes.BUCKET_VOLUME) {
                    addAir(-100);
                    status = Status.PUMPING;
                }
            }
        }
        return true;
    }

    private void findLake(Fluid fluid) {
        pumpingLake = new ArrayList<>();
        Stack<BlockPos> pendingPositions = new Stack<>();
        BlockPos thisPos = getPos().offset(Direction.DOWN, currentDepth + 1);
        pendingPositions.add(thisPos);
        pumpingLake.add(thisPos);
        while (!pendingPositions.empty()) {
            BlockPos checkingPos = pendingPositions.pop();
            for (Direction d : Direction.VALUES) {
                if (d == Direction.DOWN) continue;
                BlockPos newPos = checkingPos.offset(d);
                if (PneumaticCraftUtils.distBetweenSq(newPos, thisPos) <= MAX_PUMP_RANGE_SQUARED
                        && FluidUtils.isSourceBlock(getWorld(), newPos, fluid)
                        && !pumpingLake.contains(newPos)) {
                    pendingPositions.add(newPos);
                    pumpingLake.add(newPos);
                }
            }
        }
        pumpingLake.sort(new ChunkPositionSorter(getPos().getX() + 0.5, getPos().getY() - currentDepth - 1, getPos().getZ() + 0.5));
        Collections.reverse(pumpingLake);
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        } else {
            try {
                pumpMode = PumpMode.valueOf(tag);
            } catch (IllegalArgumentException ignored) {

            }
        }
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public float getMinWorkingPressure() {
        return 0.5F + currentDepth * 0.05F;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.put("Items", inventory.serializeNBT());
        tag.putByte("redstoneMode", (byte) redstoneMode);
        tag.putString("mode", pumpMode.toString());
        tag.putInt("currentDepth", currentDepth);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
        redstoneMode = tag.getByte("redstoneMode");
        if (tag.contains("mode")) pumpMode = PumpMode.valueOf(tag.getString("mode"));
        currentDepth = tag.getInt("currentDepth");
    }

    @Override
    protected LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return inventoryCap;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction facing) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, fluidCap);
        } else {
            return super.getCapability(cap, facing);
        }
    }

    public FluidTank getTank() {
        return tank;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerGasLift(i, playerInventory, getPos());
    }

    @Nonnull
    @Override
    public Map<String, FluidTank> getSerializableTanks() {
        return ImmutableMap.of("Tank", tank);
    }

    private class GasLiftFluidTank extends FluidTank {
        GasLiftFluidTank() {
            super(PneumaticValues.NORMAL_TANK_CAPACITY);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            int inTank = fluid.getAmount();
            int amount = pumpMode == PumpMode.PUMP_LEAVE_FLUID ? Math.max(0, inTank - 1) : inTank;
            return super.drain(Math.min(maxDrain, amount), action);
        }
    }
}
