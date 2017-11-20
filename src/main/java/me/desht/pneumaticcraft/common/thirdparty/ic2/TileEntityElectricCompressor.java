package me.desht.pneumaticcraft.common.thirdparty.ic2;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.tile.IWrenchable;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.tileentity.IRedstoneControlled;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAdvancedAirCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collections;
import java.util.List;

public class TileEntityElectricCompressor extends TileEntityPneumaticBase implements IEnergySink, IWrenchable, IRedstoneControlled, IHeatExchanger {
    public int outputTimer;//set to 20 when receiving energy, and decreased to 0 when not. Acts as a buffer before sending packets to update the client's rotation logic.

    private boolean redstoneAllows;
    @GuiSynced
    public int redstoneMode = 0;
    private int curEnergyProduction;
    @GuiSynced
    public int lastEnergyProduction;
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();

    public float turbineRotation;
    public float oldTurbineRotation;
    public float turbineSpeed;

    public TileEntityElectricCompressor() {
        super(PneumaticValues.DANGER_PRESSURE_ELECTRIC_COMPRESSOR, PneumaticValues.MAX_PRESSURE_ELECTRIC_COMPRESSOR, PneumaticValues.VOLUME_ELECTRIC_COMPRESSOR, 4);
        addApplicableUpgrade(IItemRegistry.EnumUpgrade.SPEED);
        addApplicableCustomUpgrade(IC2.overclockerUpgrade, IC2.energyStorageUpgrade, IC2.transformerUpgrade);
        heatExchanger.setThermalCapacity(100);
    }

    public int getEfficiency() {
        return TileEntityAdvancedAirCompressor.getEfficiency(heatExchanger.getTemperature());
    }

    @Override
    public void update() {
        redstoneAllows = redstoneAllows();

        oldTurbineRotation = turbineRotation;
        if (outputTimer > 0) {
            turbineSpeed = Math.min(turbineSpeed + 0.2F, 10);
        } else {
            turbineSpeed = Math.max(turbineSpeed - 0.2F, 0);
        }
        turbineRotation += turbineSpeed;

        if (!getWorld().isRemote) {
            lastEnergyProduction = curEnergyProduction;
            curEnergyProduction = 0;
        }

        super.update();

        if (!getWorld().isRemote) {
            outputTimer--;
            if (outputTimer == 0) {
                sendDescriptionPacket();
            }

        }

    }

    @Override
    protected void onFirstServerUpdate() {
        MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
    }

    @Override
    public void invalidate() {
        if (getWorld() != null && !getWorld().isRemote) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        if (getWorld() != null && !getWorld().isRemote) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
        }
        super.onChunkUnload();
    }

    @Override
    public boolean redstoneAllows() {
        switch (redstoneMode) {
            case 0:
                return true;
            case 1:
                return getWorld().isBlockIndirectlyGettingPowered(getPos()) > 0;
            case 2:
                return getWorld().isBlockIndirectlyGettingPowered(getPos()) == 0;
        }
        return false;
    }

    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return side == getRotation() || side == getRotation().getOpposite();
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public String getName() {
        return IC2.ELECTRIC_COMPRESSOR.getUnlocalizedName();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);

        redstoneMode = nbtTagCompound.getInteger("redstoneMode");
        outputTimer = nbtTagCompound.getBoolean("outputTimer") ? 20 : 0;
        turbineSpeed = nbtTagCompound.getFloat("turbineSpeed");
        lastEnergyProduction = nbtTagCompound.getInteger("energyProduction");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);

        nbtTagCompound.setInteger("redstoneMode", redstoneMode);
        nbtTagCompound.setBoolean("outputTimer", outputTimer > 0);
        nbtTagCompound.setFloat("turbineSpeed", turbineSpeed);
        nbtTagCompound.setInteger("energyProduction", lastEnergyProduction);

        return nbtTagCompound;
    }

    @Override
    public double getDemandedEnergy() {
        return redstoneAllows ? Double.MAX_VALUE : 0;
    }

    @Override
    public int getSinkTier() {
        int upgradesInserted = getCustomUpgrades(IC2.transformerUpgrade);
        return 1 + upgradesInserted;
    }

    int getMaxSafeInput() {
        int upgradesInserted = getCustomUpgrades(IC2.transformerUpgrade);
        return 32 * (int) Math.pow(4, upgradesInserted);
    }

    @Override
    public double injectEnergy(EnumFacing enumFacing, double amount, double voltage) {
        int efficiency = ConfigHandler.machineProperties.electricCompressorEfficiency;
        int airProduction = (int) (amount / 0.25F * efficiency / 100F * getEfficiency() / 100);
        heatExchanger.addHeat(amount / 16);
        addAir(airProduction);
        curEnergyProduction += airProduction;
        boolean clientNeedsUpdate = outputTimer <= 0;
        outputTimer = 20;
        if (clientNeedsUpdate) sendDescriptionPacket();
        return 0;
    }

    @Override
    public boolean acceptsEnergyFrom(IEnergyEmitter iEnergyEmitter, EnumFacing enumFacing) {
        return enumFacing == EnumFacing.UP;
    }

    @Override
    public EnumFacing getFacing(World world, BlockPos blockPos) {
        return getRotation();
    }

    @Override
    public boolean setFacing(World world, BlockPos blockPos, EnumFacing enumFacing, EntityPlayer entityPlayer) {
        Block b = getBlockType();
        if (b instanceof BlockElectricCompressor) {
            ((BlockElectricCompressor) b).rotateBlock(world, entityPlayer, blockPos, enumFacing);
            return true;
        }
        return false;
    }

    @Override
    public boolean wrenchCanRemove(World world, BlockPos blockPos, EntityPlayer entityPlayer) {
        return true;
    }

    @Override
    public List<ItemStack> getWrenchDrops(World world, BlockPos blockPos, IBlockState iBlockState, TileEntity tileEntity, EntityPlayer entityPlayer, int i) {
        return Collections.singletonList(new ItemStack(IC2.ELECTRIC_COMPRESSOR));
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        return heatExchanger;
    }
}
