package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.inventory.SolarCompressorMenu;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.util.BoundingBlockEntityData;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static me.desht.pneumaticcraft.api.lib.NBTKeys.NBT_BROKEN;

public class SolarCompressorBlockEntity extends AbstractAirHandlingBlockEntity implements IHeatExchangingTE, MenuProvider, IHasBoundingBlocks {
    public static final double MAX_TEMPERATURE = 698.15; // 425C
    public static final double WARNING_TEMPERATURE = MAX_TEMPERATURE - 15; // 410C
    private static final Vec3i[] SURROUNDING_BLOCK_OFFSETS = {
            new Vec3i(1, 0, 0),
            new Vec3i(-1, 0, 0),
            new Vec3i(0, 0, 1),
            new Vec3i(0, 0, -1),
            new Vec3i(1, 0, -1),
            new Vec3i(-1, 0, 1),
            new Vec3i(1, 0, 1),
            new Vec3i(-1, 0, -1)};
    @GuiSynced
    private float airPerTick;
    private float airBuffer;
    @GuiSynced
    private boolean isBroken = false;
    @GuiSynced
    private boolean canSeeSunlight = false;
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private final BoundingBlockEntityData boundingBlockEntityData = new BoundingBlockEntityData();

    public SolarCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.SOLAR_COMPRESSOR.get(), pos, state, PressureTier.TIER_TWO, PneumaticValues.VOLUME_SOLAR_COMPRESSOR, 4);

        // Nullifies the heat component of the bounding blocks
        // Might not be necessary but doesn't hurt anything
        if(isBounding(this)) {
            heatExchanger.setThermalCapacity(0);
            heatExchanger.setThermalResistance(1000000000);
        }

        else {
            heatExchanger.setThermalCapacity(100);
        }
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    @NotNull
    @Override
    public BoundingBlockEntityData getBoundingBlockEntityData() {
        return boundingBlockEntityData;
    }

    /**
     * Returns if the compressor is broken
     * @return if the compressor is broken
     */
    public boolean isBroken() {
        return isBroken;
    }

    /**
     * Fixes the solar compressor (sets isBroken to false)
     */
    public void fixBroken() {
        isBroken = false;
    }

    /**
     * Breaks solar compressor and dangerously vents all heat
     */
    public void breakCompressor() {
        if(!isBounding(this)) {
            // Breaks compressor
            isBroken = true;

            // Removes excess heat
            heatExchanger.setTemperature(heatExchanger.getAmbientTemperature());

            // Creates fires around base of compressor
            final Level level = nonNullLevel();
            BlockPos mainPos = getBlockPos();

            for (Vec3i offset : SURROUNDING_BLOCK_OFFSETS) {
                BlockPos offsetPos = mainPos.offset(offset);

                // Only replaces air blocks with fire
                if (level.getBlockState(offsetPos).getBlock() == Blocks.AIR) {
                    level.setBlockAndUpdate(offsetPos, Blocks.FIRE.defaultBlockState());
                }
            }

            // Spawns fire particles on the base of the compressor
            if (!level.isClientSide()) {
                ((ServerLevel) level).sendParticles(
                        ParticleTypes.FLAME,
                        mainPos.getX() + 0.5,
                        mainPos.getY() + 0.75,
                        mainPos.getZ()+ 0.5,
                        30,
                        0,
                        0,
                        0,
                        0.15);
            }

            // Plays sounds for the compressor breaking
            level.playSound(null, mainPos, SoundEvents.GENERIC_BURN, SoundSource.BLOCKS, 1 ,1);
            level.playSound(null, mainPos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1 ,1);
        }
    }

    /**
     * Returns the bonus heat efficiency for the compressor
     * Base ambient temperature (39C or 312.15K) or less is efficiency of 1
     * Higher temperature scales efficiency up to 8 at 351.15C or 624.3K
     * @return the bonus heat efficiency for the compressor
     */
    public float getHeatEfficiency(){
        double temperature = heatExchanger.getTemperatureAsInt(); // AsInt required to sync with GUI
        return (float) Mth.clamp(Math.pow((temperature / 312.15), 3), 0, 8);
    }

    /**
     * Returns the percent for the heat efficiency of the solar compressor
     * @return the percent for the heat efficiency of the solar compressor
     */
    public int getPercentHeatEfficiency() {
        return (int) Mth.clamp((getHeatEfficiency() / 8) * 100, 1, 100);
    }

    /**
     * Returns the heat of the solar compressor
     * @return the heat of the solar compressor
     */
    public int getTemperature() {
        return heatExchanger.getTemperatureAsInt();
    }

    /**
     * Returns if the solar compressor block can generate air
     * @return if the solar compressor block can generate air
     */
    public boolean canGenerateAir() {
        return getCanSeeSunlight() && !isBroken;
    }

    /**
     * Sets if the solar compressor block can see the sun
     */
    public void canSeeSunlight() {
        final Level level = nonNullLevel();

        // Sets false for bounding blocks, during the night, and during rain
        if (isBounding(this) || level.isNight() || level.isRaining())
        {
            canSeeSunlight = false;
        }

        // Sets true only if all bounding blocks can see the sky
        else {
            canSeeSunlight = (level.canSeeSky(getBlockPos().offset(0, 1, 0)) &&
                    level.canSeeSky(getBlockPos().offset(0, 1, 1)) &&
                    level.canSeeSky(getBlockPos().offset(0, 1, -1)));
        }
    }

    /**
     * Returns if the solar compressor block can see the sun
     * @return if the solar compressor block can see the sun
     */
    public boolean getCanSeeSunlight() {
        return canSeeSunlight;
    }

    @Override
    public void tickServer() {
        super.tickServer();

        // Only does generation ticks for the main block
        if(!isBounding(this)) {
            final Level level = nonNullLevel();

            // Updates air generation amount every 5 ticks
            if (level.getGameTime() % 5 == 0) {
                float baseModifier = PneumaticValues.PRODUCTION_SOLAR_COMPRESSOR *
                        this.getSpeedUsageMultiplierFromUpgrades();
                float heatModifier = getHeatEfficiency();
                float configModifier = ConfigHelper.common().machines.solarCompressorMultiplier.get().floatValue();

                airPerTick = baseModifier * heatModifier * configModifier;
            }

            // Sets if the compressor can see the sun
            canSeeSunlight();

            // Generates air only when sky is visible and during day
            if (canGenerateAir()) {
                // Resets thermal resistance
                heatExchanger.setThermalResistance(1);

                airBuffer += airPerTick;

                if (airBuffer >= 1f) {
                    int toAdd = (int) airBuffer;
                    this.addAir(toAdd);
                    airBuffer -= toAdd;

                    // Heats up compressor proportionate to how much air is being generated
                    double heatToAdd = Math.round(toAdd / 20d) + 2;
                    heatExchanger.addHeat(heatToAdd);
                }
            }

            // Prevents heat loss when the compressor is inactive to make managing a stable heat easier
            else {
                heatExchanger.setThermalResistance(1000000000);
            }

            // Emits smoke particles if overheating
            if (heatExchanger.getTemperature() < MAX_TEMPERATURE
                    && heatExchanger.getTemperature() > WARNING_TEMPERATURE) {

                // Only emits smoke particles once every second
                if (level.getGameTime() % 20 == 0) {
                    BlockPos mainPos = getBlockPos();
                    if (!level.isClientSide()) {
                        ((ServerLevel) level).sendParticles(
                                ParticleTypes.SMOKE,
                                mainPos.getX() + 0.5,
                                mainPos.getY() + 1.2,
                                mainPos.getZ() + 0.5,
                                10,
                                0,
                                0,
                                0,
                                0.05);
                    }
                }
            }

            // Breaks compressor if overheated
            if (!isBroken && (heatExchanger.getTemperature() > MAX_TEMPERATURE)) {
                breakCompressor();
            }
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return !isBounding(this) && (side == getRotation() || side == getRotation().getOpposite());
    }

    public float getAirRate() {
        // Returns 0 for the air rate if the compressor is broken
        if (isBroken) {
            return 0f;
        }

        else {
            return airPerTick;
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        // Returns null for the bounding blocks
        if (isBounding(this)) {
            return null;
        }

        else {
            return new SolarCompressorMenu(i, playerInventory, getBlockPos());
        }
    }

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        // Only the main block has a heat exchanger
        return isBounding(this) ? null : heatExchanger;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        isBroken = tag.getBoolean(NBTKeys.NBT_BROKEN);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (isBroken) {
            tag.putBoolean(NBTKeys.NBT_BROKEN, true);
        }
    }

    @Override
    public void serializeExtraItemData(CompoundTag blockEntityTag, boolean preserveState) {
        super.serializeExtraItemData(blockEntityTag, preserveState);

        if (isBroken) {
            blockEntityTag.putBoolean(NBT_BROKEN, true);
        }
    }
}
