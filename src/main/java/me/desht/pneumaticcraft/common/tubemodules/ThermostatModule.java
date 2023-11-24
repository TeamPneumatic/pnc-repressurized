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

import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.TemperatureData;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncThermostatModuleToClient;
import me.desht.pneumaticcraft.common.network.PacketSyncThermostatModuleToServer;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;

public class ThermostatModule extends AbstractTubeModule implements INetworkedModule {

    private int colorChannel;
    private double temperature = 0.0;
    private int level;
    private int threshold;
    private boolean update = true;

    public ThermostatModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        super(dir, pressureTube);
    }

    public double getTemperature() {
        return this.temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
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
    public void setColorChannel(int channel) {
        this.colorChannel = channel;
        setChanged();
    }

    @Override
    public double getWidth() {
        return 9D;
    }

    @Override
    protected double getHeight() {
        return 5D;
    }

    @Override
    public void addInfo(List<Component> curInfo) {
        super.addInfo(curInfo);
        curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.tubeModule.threshold_temp", PneumaticCraftUtils.roundNumberTo(getThreshold(), 1)));
        curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.thermostatModule.temperature", temperature));
        curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.thermostatModule.level", level));
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

    @Override
    public void onNeighborBlockUpdate() {
        updateInputLevel();
    }

    @Override
    public void onNeighborTileUpdate() {
        updateInputLevel();
    }

    @Override
    public void tickServer() {
        super.tickServer();

        // Forced recalc when client GUI updated
        if (this.update) {
            this.update = false;
            updateInputLevel();
        }
    }

    private void updateInputLevel() {
        Level world = Objects.requireNonNull(pressureTube.getLevel());

        BlockPos pos = pressureTube.getBlockPos().relative(getDirection());

        HeatExchangerManager.getInstance().getLogic(world, pos, null)
            .ifPresent(logic -> setTemperature(logic.getTemperature() - 273));

        int level = 0;
        if (temperature >= threshold) {
            level = 15;
        }

        if (this.level != level) {
            this.level = level;
            NetworkHandler.sendToAllTracking(new PacketSyncThermostatModuleToClient(this), getTube());
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);

        tag.putByte("channel", (byte) colorChannel);
        tag.putByte("level", (byte) level);
        tag.putDouble("temperature", temperature);
        tag.putInt("threshold", threshold);

        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);

        colorChannel = tag.getByte("channel");
        level = tag.getByte("level");
        temperature = tag.getDouble("temperature");
        threshold = tag.getInt("threshold");
    }

}
