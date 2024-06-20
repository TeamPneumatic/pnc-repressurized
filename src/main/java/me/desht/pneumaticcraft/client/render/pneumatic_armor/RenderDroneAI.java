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

package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.client.render.ProgWidgetRenderer;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.entity.drone.ProgrammableControllerEntity;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Renders the Drone's currently executing widget in-world, above the drone itself. Used when drone debugging is active.
 */
public class RenderDroneAI {
    private final AbstractDroneEntity drone;
    private final List<Pair<RenderCoordWireframe, Integer>> blackListWireframes = new ArrayList<>();
    private float progress = 0;
    private BlockPos oldPos, pos;

    public RenderDroneAI(AbstractDroneEntity drone) {
        this.drone = drone;
        tick();
    }

    public void tick() {
        BlockPos lastPos = pos;
        pos = drone.getTargetedBlock();
        if (pos != null) {
            if (lastPos == null) {
                oldPos = pos;
            } else if (!pos.equals(lastPos)) {
                progress = 0;
                oldPos = lastPos;
            }
        } else {
            oldPos = null;
        }
        progress = Math.min((float) Math.PI, progress + 0.1F);

        Iterator<Pair<RenderCoordWireframe, Integer>> iterator = blackListWireframes.iterator();
        while (iterator.hasNext()) {
            Pair<RenderCoordWireframe, Integer> wireframe = iterator.next();
            wireframe.getKey().ticksExisted++;
            wireframe.setValue(wireframe.getValue() - 1);
            if (wireframe.getValue() <= 0) {
                iterator.remove();
            }
        }
    }

    public void render(PoseStack matrixStack, MultiBufferSource buffer, float partialTicks) {
        for (Pair<RenderCoordWireframe, Integer> wireframe : blackListWireframes) {
            wireframe.getKey().render(matrixStack, buffer, partialTicks);
        }

        if (PneumaticArmorItem.isPlayerDebuggingDrone(ClientUtils.getClientPlayer(), drone)) {
            IProgWidget activeWidget = getActiveWidget(drone);
            if (activeWidget != null) {
                double x, y, z;
                if (pos != null) {
                    x = getInterpolated(pos.getX(), oldPos.getX(), partialTicks);
                    y = getInterpolated(pos.getY(), oldPos.getY(), partialTicks);
                    z = getInterpolated(pos.getZ(), oldPos.getZ(), partialTicks);
                } else {
                    x = Mth.lerp(partialTicks, drone.xo, drone.getX());
                    y = Mth.lerp(partialTicks, drone.yo, drone.getY()) + 0.5;
                    z = Mth.lerp(partialTicks, drone.zo, drone.getZ());
                }
                matrixStack.pushPose();
                matrixStack.translate(x, y + 0.5, z);
                matrixStack.scale(0.01f, 0.01f, 0.01f);
                matrixStack.mulPose(Axis.XP.rotationDegrees(180));
                matrixStack.mulPose(Axis.YP.rotationDegrees(Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));
                ProgWidgetRenderer.renderProgWidget3d(matrixStack, buffer, activeWidget);
                matrixStack.popPose();
            }
        }
    }

    private IProgWidget getActiveWidget(AbstractDroneEntity droneBase) {
        if (droneBase instanceof DroneEntity de) {
            return de.getActiveWidget();
        } else if (droneBase instanceof ProgrammableControllerEntity pc) {
            return pc.getController().getActiveWidget();
        } else {
            return null;
        }
    }

    private double getInterpolated(double newPos, double oldPos, float partialTicks) {
        double cosProgress = 0.5 - 0.5 * Mth.cos((float) Math.min(Math.PI, progress + partialTicks * 0.1F));
        return oldPos + (newPos - oldPos) * cosProgress;
    }

    public void addBlackListEntry(Level world, BlockPos pos) {
        blackListWireframes.add(new MutablePair<>(new RenderCoordWireframe(world, pos), 60));
    }
}
