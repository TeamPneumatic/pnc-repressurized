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

package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class HeatBehaviourFurnace extends HeatBehaviour<AbstractFurnaceTileEntity> {
    static final ResourceLocation ID = RL("furnace");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public boolean isApplicable() {
        return getBlockState().getBlock() instanceof AbstractFurnaceBlock;
    }

    @Override
    public void tick() {
        AbstractFurnaceTileEntity furnace = getTileEntity();
        if (getHeatExchanger().getTemperature() > 373) {
            if (furnace.litTime < 190 && !furnace.getItem(0).isEmpty()) {
                if (furnace.litTime == 0) {
                    getWorld().setBlockAndUpdate(getPos(), getBlockState().setValue(AbstractFurnaceBlock.LIT, true));
                }
                furnace.litDuration = 200; // oddly named? this is itemBurnTime
                furnace.litTime += 10;
                getHeatExchanger().addHeat(-1);
            }
            if (furnace.cookingProgress > 0) {
                // Easy performance saver, the Furnace won't be ticked unnecessarily when there's nothing to
                // cook (or when just started cooking).
                int progress = Math.max(0, ((int) getHeatExchanger().getTemperature() - 343) / 30);
                progress = Math.min(5, progress);
                for (int i = 0; i < progress; i++) {
                    furnace.tick();
                }
            }
        }
    }

}
