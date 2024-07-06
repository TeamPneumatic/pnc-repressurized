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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureBlock;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PressureGaugeModule extends AbstractRedstoneEmittingModule {
    private boolean hideGauge = false;

    public PressureGaugeModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        super(dir, pressureTube);

        lowerBound = 0;
        higherBound = 7.5F;
    }

    @Override
    public Item getItem() {
        return ModItems.PRESSURE_GAUGE_MODULE.get();
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (pressureTube.nonNullLevel().getGameTime() % 20 == 0) {
            PNCCapabilities.getAirHandler(pressureTube).ifPresent(handler ->
                    NetworkHandler.sendToAllTracking(PacketUpdatePressureBlock.create(pressureTube.getBlockPos(),
                            null, handler.getSideLeaking(), handler.getAir()), pressureTube)
            );
        }
        if (setRedstone(getRedstone(pressureTube.getPressure()))) {
            // force a recalc on next tick
            pressureTube.tubeModules()
                    .filter(tm -> tm instanceof RedstoneModule)
                    .forEach(tm -> ((RedstoneModule) tm).setInputLevel(-1));
        }
    }

    @Override
    public boolean onActivated(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (ModdedWrenchUtils.getInstance().isWrench(heldStack)) {
            hideGauge = !hideGauge;
            setChanged();
            getTube().sendDescriptionPacket();
            return true;
        }
        return super.onActivated(player, hand);
    }

    public boolean shouldShowGauge() {
        return !hideGauge;
    }

    private int getRedstone(float pressure) {
        return (int) ((pressure - lowerBound) / (higherBound - lowerBound) * 15);
    }

    @Override
    public double getWidth() {
        return 8D;
    }

    @Override
    protected double getHeight() {
        return 4D;
    }

    @Override
    public boolean hasGui() {
        return upgraded;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (hideGauge) tag.putBoolean("hideGauge", true);
        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        hideGauge = tag.getBoolean("hideGauge");
    }
}
