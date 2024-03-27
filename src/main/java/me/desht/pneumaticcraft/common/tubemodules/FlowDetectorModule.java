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

import me.desht.pneumaticcraft.common.block.PressureTubeBlock;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;

public class FlowDetectorModule extends AbstractRedstoneEmittingModule implements IInfluenceDispersing {
    public float rotation, oldRotation;
    private int flow;
    private int oldFlow;

    public FlowDetectorModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        super(dir, pressureTube);
    }

    @Override
    public void tickCommon() {
        super.tickCommon();

        oldRotation = rotation;
        rotation += getRedstoneLevel() / 100F;
    }

    @Override
    public Item getItem() {
        return ModItems.FLOW_DETECTOR_MODULE.get();
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (setRedstone(flow / 5)) {
            pressureTube.sendDescriptionPacket();
        }
        oldFlow = flow;
        flow = 0;
    }

    @Override
    public int getMaxDispersion() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onAirDispersion(int amount) {
        flow += amount;
    }

    @Override
    public void addInfo(List<Component> curInfo) {
        super.addInfo(curInfo);
        curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.flowModule.level", oldFlow));
    }

    @Override
    public boolean isInline() {
        return true;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        rotation = tag.getFloat("rotation");
        oldFlow = tag.getInt("flow");//taggin it for waila purposes.
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putFloat("rotation", rotation);
        tag.putInt("flow", oldFlow);
        return tag;
    }

    @Override
    public boolean canUpgrade() {
        return false;
    }

    @Override
    public boolean isInlineAndFocused(PressureTubeBlock.TubeHitInfo hitInfo) {
        // flow detector is small and covers only the tube end it's installed on
        return hitInfo.dir() == getDirection();
    }
}
