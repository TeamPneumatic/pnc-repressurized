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

package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class ModuleFlowDetector extends TubeModuleRedstoneEmitting implements IInfluenceDispersing {
    public float rotation, oldRotation;
    private int flow;
    private int oldFlow;

    public ModuleFlowDetector(ItemTubeModule item) {
        super(item);
    }

    @Override
    public void update() {
        super.update();

        oldRotation = rotation;
        rotation += getRedstoneLevel() / 100F;

        if (!pressureTube.getLevel().isClientSide) {
            if (setRedstone(flow / 5)) {
                sendDescriptionPacket();
            }
            oldFlow = flow;
            flow = 0;
        }
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
    public void addInfo(List<ITextComponent> curInfo) {
        super.addInfo(curInfo);
        curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.flowModule.level", oldFlow));
    }

    @Override
    public boolean isInline() {
        return true;
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        rotation = tag.getFloat("rotation");
        oldFlow = tag.getInt("flow");//taggin it for waila purposes.
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putFloat("rotation", rotation);
        tag.putInt("flow", oldFlow);
        return tag;
    }

    @Override
    public boolean canUpgrade() {
        return false;
    }
}
