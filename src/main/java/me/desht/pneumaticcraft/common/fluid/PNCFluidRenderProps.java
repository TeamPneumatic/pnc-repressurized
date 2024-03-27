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

package me.desht.pneumaticcraft.common.fluid;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PNCFluidRenderProps implements IClientFluidTypeExtensions {
    private final ResourceLocation still;
    private final ResourceLocation flowing;
    private final int colorTint;

    public PNCFluidRenderProps(String still, String flowing) {
        this(still, flowing, 0xFFFFFFFF);
    }

    public PNCFluidRenderProps(String still, String flowing, int colorTint) {
        this.still = still.indexOf(':') > 0 ? new ResourceLocation(still) : RL("block/fluid/" + still);
        this.flowing = flowing.indexOf(':') > 0 ? new ResourceLocation(flowing) : RL("block/fluid/" + flowing);
        this.colorTint = colorTint;
    }

    public static PNCFluidRenderProps genericFuel(int colorTint) {
        return new PNCFluidRenderProps("generic_fuel_still", "generic_fuel_flow", colorTint);
    }

    @Override
    public ResourceLocation getStillTexture() {
        return still;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return flowing;
    }

    @Override
    public int getTintColor() {
        return colorTint;
    }
}
