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

package me.desht.pneumaticcraft.client.render.entity.drone;

import me.desht.pneumaticcraft.client.model.ModelMinigun;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DroneRenderState extends LivingEntityRenderState {
    public float propRotation;
    public int droneColor;

    public final ItemStackRenderState heldItem = new ItemStackRenderState();
    public boolean heldItemUpsideDown;
    public float heldItemScale;

    public @Nullable Vec3 digLaserStart;
    public @Nullable Vec3 digLaserEnd;
    public int laserColor;

    public float @Nullable [] targetLine;

    public boolean hasMinigun;
    public final ModelMinigun.MinigunRenderState minigun = new ModelMinigun.MinigunRenderState();
}
