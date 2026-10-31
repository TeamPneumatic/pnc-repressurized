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

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PNCFluidRenderProps implements IClientFluidTypeExtensions {
    private final Identifier still;
    private final Identifier flowing;
    private final int colorTint;

    public PNCFluidRenderProps(String still, String flowing) {
        this(still, flowing, 0xFFFFFFFF);
    }

    public PNCFluidRenderProps(String still, String flowing, int colorTint) {
        this.still = still.indexOf(':') > 0 ? Identifier.parse(still) : RL("block/fluid/" + still);
        this.flowing = flowing.indexOf(':') > 0 ? Identifier.parse(flowing) : RL("block/fluid/" + flowing);
        this.colorTint = colorTint;
    }

    public static PNCFluidRenderProps thickFluid(int colorTint) {
        return new PNCFluidRenderProps("thick_fluid_still", "thick_fluid_flow", colorTint);
    }

    public static PNCFluidRenderProps waterLike(int colorTint) {
        return new PNCFluidRenderProps("minecraft:block/water_still", "minecraft:block/water_flow", colorTint);
    }

    public static PNCFluidRenderProps texture(String textureBase) {
        return texture(textureBase, 0xFFFFFFFF);
    }

    public static PNCFluidRenderProps texture(String textureBase, int colorTint) {
        return new PNCFluidRenderProps(textureBase + "_still", textureBase + "_flow", colorTint);
    }

    @Override
    public Identifier getStillTexture() {
        return still;
    }

    @Override
    public Identifier getFlowingTexture() {
        return flowing;
    }

    @Override
    public int getTintColor() {
        return colorTint;
    }
}
