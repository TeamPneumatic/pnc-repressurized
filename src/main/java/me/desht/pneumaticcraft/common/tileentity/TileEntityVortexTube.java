package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.network.DescSynced;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class TileEntityVortexTube extends TileEntityPneumaticBase implements IHeatTinted {
    private final IHeatExchangerLogic coldHeatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private LazyOptional<IHeatExchangerLogic> coldHeatCap = LazyOptional.of(() -> coldHeatExchanger);
    private final IHeatExchangerLogic hotHeatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private LazyOptional<IHeatExchangerLogic> hotHeatCap = LazyOptional.of(() -> hotHeatExchanger);
    private final IHeatExchangerLogic connectingExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private int visualizationTimer = 30;

    @DescSynced
    private boolean visualize;
    @DescSynced
    private int coldHeatLevel = 10, hotHeatLevel = 10;

    public TileEntityVortexTube() {
        super(ModTileEntities.VORTEX_TUBE.get(), 20, 25, 2000, 0);
        coldHeatExchanger.setThermalResistance(0.01);
        hotHeatExchanger.setThermalResistance(0.01);
        connectingExchanger.setThermalResistance(100);
        connectingExchanger.addConnectedExchanger(coldHeatExchanger);
        connectingExchanger.addConnectedExchanger(hotHeatExchanger);
    }

    @Override
    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        if (side == null || side == getRotation().getOpposite()) {
            return hotHeatCap;
        } else if (side == getRotation()) {
            return coldHeatCap;
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    protected Direction[] getConnectedHeatExchangerSides() {
        return new Direction[]{getRotation().getOpposite()};
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Override
    protected void initializeIfHeatExchanger() {
        super.initializeIfHeatExchanger();
        initializeHeatExchanger(coldHeatExchanger, getRotation());
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != getRotation() && side != getRotation().getOpposite();
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.put("coldHeat", coldHeatExchanger.serializeNBT());
        tag.put("connector", connectingExchanger.serializeNBT());
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        coldHeatExchanger.deserializeNBT(tag.getCompound("coldHeat"));
        connectingExchanger.deserializeNBT(tag.getCompound("connector"));
    }

    public int getColdHeatLevel() {
        return visualize ? 0 : coldHeatLevel;
    }

    public int getHotHeatLevel() {
        return visualize ? 20 : hotHeatLevel;
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isRemote) {
            // Only update the cold and connecting side; the hot side is handled in the superclass
            connectingExchanger.tick();
            coldHeatExchanger.tick();
            int usedAir = (int) (getPressure() * 10);
            if (usedAir > 0) {
                addAir(-usedAir);
                double generatedHeat = usedAir / 10D;
                coldHeatExchanger.addHeat(-generatedHeat);
                hotHeatExchanger.addHeat(generatedHeat);
            }
            visualize = visualizationTimer > 0;
            if (visualize) visualizationTimer--;
            coldHeatLevel = HeatUtil.getHeatLevelForTemperature(coldHeatExchanger.getTemperature());
            hotHeatLevel = HeatUtil.getHeatLevelForTemperature(hotHeatExchanger.getTemperature());
        }
    }

    @Override
    public void onBlockRotated() {
        visualizationTimer = 60;
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public int getHeatLevelForTintIndex(int tintIndex) {
        switch (tintIndex) {
            case 1: return visualize ? 20 : hotHeatLevel;
            case 2: return visualize ? 0 : coldHeatLevel;
            default: return 11;
        }
    }
}
