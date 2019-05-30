package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.ai.ChunkPositionSorter;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TileEntityGasLift extends TileEntityPneumaticBase
        implements IMinWorkingPressure, IRedstoneControlled, ISerializableTanks, IAutoFluidEjecting {
    private static final int INVENTORY_SIZE = 1;

    public enum Status {
        IDLE("idling"), PUMPING("pumping"), DIGGING("diggingDown"), RETRACTING("retracting"), STUCK("stuck");
        public final String desc;
        Status(String desc) {
            this.desc = desc;
        }
    }

    @GuiSynced
    private final FluidTank tank = new FluidTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    private final ItemStackHandler inventory = new BaseItemStackHandler(this, INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty()
                    || itemStack.getItem() instanceof ItemBlock && ((ItemBlock) itemStack.getItem()).getBlock() instanceof BlockPressureTube;
        }
    };
    @GuiSynced
    public int currentDepth;
    @GuiSynced
    public int redstoneMode, mode;
    @GuiSynced
    public Status status = Status.IDLE;
    @DescSynced
    public final boolean[] sidesConnected = new boolean[6];
    private int workTimer;
    private int ticker;
    private List<BlockPos> pumpingLake;
    private static final int MAX_PUMP_RANGE_SQUARED = 15 * 15;

    public TileEntityGasLift() {
        super(5, 7, 3000, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED, EnumUpgrade.DISPENSER);
    }

    @Override
    public boolean isConnectedTo(EnumFacing d) {
        return d != EnumFacing.DOWN;
    }

    private void updateConnections() {
        List<Pair<EnumFacing, IAirHandler>> connections = getAirHandler(null).getConnectedPneumatics();
        Arrays.fill(sidesConnected, false);
        for (Pair<EnumFacing, IAirHandler> entry : connections) {
            sidesConnected[entry.getKey().ordinal()] = true;
        }
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        updateConnections();
    }

    @Override
    public void onNeighborTileUpdate() {
        super.onNeighborTileUpdate();
        updateConnections();
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            ticker++;
            if (currentDepth > 0) {
                int curCheckingPipe = ticker % currentDepth;
                if (curCheckingPipe > 0 && !isPipe(world, getPos().offset(EnumFacing.DOWN, curCheckingPipe))) {
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
                    if (mode == 2) {
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
                BlockPos pos1 = getPos().offset(EnumFacing.DOWN, currentDepth);
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
        if (isUnbreakable(getPos().offset(EnumFacing.DOWN, currentDepth + 1))) {
            status = Status.STUCK;
        } else if (getPos().getY() - currentDepth >= 0) {
            status = Status.DIGGING;
            currentDepth++;
            BlockPos pos1 = getPos().offset(EnumFacing.DOWN, currentDepth);
            if (!isPipe(world, pos1)) {
                ItemStack extracted = inventory.extractItem(0, 1, true);
                if (extracted.getItem() instanceof ItemBlock) {
                    IBlockState currentState = world.getBlockState(pos1);
                    IBlockState newState = ((ItemBlock) extracted.getItem()).getBlock().getDefaultState();

                    int airRequired = Math.round(66.66f * currentState.getBlockHardness(world, pos1));
                    if (getPipeTier(newState) > 1) airRequired /= 2;

                    if (getAirHandler(null).getAir() > airRequired) {
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
        return getPipeTier(world.getBlockState(pos)) >= 1;
    }

    private int getPipeTier(IBlockState state) {
        Block b = state.getBlock();
        return b instanceof BlockPressureTube ? ((BlockPressureTube) b).getTier() : 0;
    }

    private boolean isUnbreakable(BlockPos pos) {
        return world().getBlockState(pos).getBlockHardness(world, pos) < 0;
    }


    private boolean suckLiquid() {
        BlockPos pos = getPos().offset(EnumFacing.DOWN, currentDepth + 1);

        FluidStack fluidStack = FluidUtils.getFluidAt(world, pos, false);
        if (fluidStack == null || fluidStack.amount < Fluid.BUCKET_VOLUME) {
            pumpingLake = null;
            return false;
        }

        if (tank.fill(fluidStack, false) == Fluid.BUCKET_VOLUME) {
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
                FluidStack fluidStack1 = FluidUtils.getFluidAt(world, curPos, true);
                if (fluidStack1 != null && fluidStack1.amount == Fluid.BUCKET_VOLUME) {
                    tank.fill(fluidStack1, true);
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
        BlockPos thisPos = getPos().offset(EnumFacing.DOWN, currentDepth + 1);
        pendingPositions.add(thisPos);
        pumpingLake.add(thisPos);
        while (!pendingPositions.empty()) {
            BlockPos checkingPos = pendingPositions.pop();
            for (EnumFacing d : EnumFacing.VALUES) {
                if (d == EnumFacing.DOWN) continue;
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
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        } else if (buttonID > 0 && buttonID < 4) {
            mode = buttonID - 1;
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
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Items", inventory.serializeNBT());
        tag.setByte("redstoneMode", (byte) redstoneMode);
        tag.setByte("mode", (byte) mode);

        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        tag.setTag("tank", tankTag);
        tag.setInteger("currentDepth", currentDepth);

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        inventory.deserializeNBT(tag.getCompoundTag("Items"));
        redstoneMode = tag.getByte("redstoneMode");
        mode = tag.getByte("mode");
        tank.readFromNBT(tag.getCompoundTag("tank"));
        currentDepth = tag.getInteger("currentDepth");
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank);
        } else {
            return super.getCapability(capability, facing);
        }
    }

    public FluidTank getTank() {
        return tank;
    }

    /**
     * Returns the name of the inventory.
     */
    @Override
    public String getName() {
        return Blockss.GAS_LIFT.getTranslationKey();
    }

    @Nonnull
    @Override
    public Map<String, FluidTank> getSerializableTanks() {
        return ImmutableMap.of("Tank", tank);
    }
}
