/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.tubemodules;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.heat.TemperatureListener;
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncThermostatModuleToClient;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ThermostatModule extends AbstractNetworkedRedstoneModule implements INetworkedModule, TemperatureListener {

    public static final int MIN_VALUE = -273;
    public static final int MAX_VALUE = 2000;

    private int colorChannel;
    private int temperature = 0;
    private int threshold;
    private boolean update = true;

    public ThermostatModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        super(dir, pressureTube);
        lowerBound = ThermostatModule.MIN_VALUE;
        higherBound = ThermostatModule.MAX_VALUE;
    }

    public int getTemperature() {
        return this.temperature;
    }

    public int getTemperatureForLevel(int level) {
        float temperatureRange = higherBound - lowerBound;
        float levelNormalized = (float)level / 15f;
        float temperature = levelNormalized * temperatureRange + lowerBound;
        return (int)temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    @Override
    public boolean hasGui() {
        return true;
    }

    @Override
    public Item getItem() {
        return ModItems.THERMOSTAT_MODULE.get();
    }

    @Override
    public int getColorChannel() {
        return colorChannel;
    }

    @Override
    protected int getInputChannel() {
        return colorChannel;
    }

    @Override
    public void setColorChannel(int channel) {
        this.colorChannel = channel;
        setChanged();
    }

    @Override
    public double getWidth() {
        return 10D;
    }

    @Override
    protected double getHeight() {
        return 5D;
    }

    @Override
    public void addInfo(List<Component> curInfo) {
        super.addInfo(curInfo);
        if (advancedConfig) {
            curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.tubeModule.threshold_temp_bounds",
                PneumaticCraftUtils.roundNumberTo(lowerBound, 0), PneumaticCraftUtils.roundNumberTo(higherBound, 0)));
        } else {
            curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.tubeModule.threshold_temp",
                PneumaticCraftUtils.roundNumberTo(getThreshold(), 1)));
        }
        curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.thermostatModule.temperature", temperature));
        curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.thermostatModule.level", getInputLevel()));
    }

    @Override
    public boolean onActivated(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        DyeColor dyeColor = DyeColor.getColor(heldStack);
        if (dyeColor != null) {
            int colorId = dyeColor.getId();
            setColorChannel(colorId);
            if (ConfigHelper.common().general.useUpDyesWhenColoring.get() && !player.isCreative()) {
                heldStack.shrink(1);
            }
            return true;
        } else {
            return super.onActivated(player, hand);
        }
    }

    @NotNull
    private Optional<IHeatExchangerLogic> getHeatExchangerLogic() {
        return HeatExchangerManager.getInstance().getLogic(pressureTube.getLevel(),
                pressureTube.getBlockPos().relative(getDirection()),
                getDirection().getOpposite());
    }

    @Override
    public void onPlaced() {
        getHeatExchangerLogic().ifPresent(logic -> logic.addTemperatureListener(this));
    }

    @Override
    public void onRemoved() {
        getHeatExchangerLogic().ifPresent(logic -> logic.removeTemperatureListener(this));
    }

    @Override
    public void onNeighborBlockUpdate() {
        getHeatExchangerLogic().ifPresent(logic -> {
            logic.removeTemperatureListener(this);
            logic.addTemperatureListener(this);
        });
        updateInputLevel();
    }

    @Override
    public void onNeighborTileUpdate() {
        updateInputLevel();
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (this.update) {
            this.update = false;
            updateInputLevel();
        }
    }

    @Override
    protected int calculateInputLevel() {
        getHeatExchangerLogic().ifPresent(logic -> setTemperature((int)logic.getTemperature() - 273));

        if (advancedConfig) {
            float temperatureRange = higherBound - lowerBound;
            float temperatureNormalized = (temperature - lowerBound) / temperatureRange;
            int level = (int)(15f * temperatureNormalized);
            level = Math.max(0, Math.min(15, level));
            return level;
        } else {
            return temperature >= threshold ? 15 : 0;
        }
    }

    @Override
    protected void onInputLevelChange(int level) {
        NetworkHandler.sendToAllTracking(PacketSyncThermostatModuleToClient.forModule(this), getTube());
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);

        tag.putByte("channel", (byte) colorChannel);
        tag.putInt("temperature", temperature);
        tag.putInt("threshold", threshold);

        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);

        colorChannel = tag.getByte("channel");
        temperature = tag.getInt("temperature");
        threshold = tag.getInt("threshold");
    }

    @Override
    public void onTemperatureChanged(double prevTemperature, double newTemperature) {
        setUpdate(true);
    }
}
