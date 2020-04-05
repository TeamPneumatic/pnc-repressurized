package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RenderDroneAI {
    private final EntityDrone drone;
    private final List<Pair<RenderCoordWireframe, Integer>> blackListWireframes = new ArrayList<>();
    private float progress = 0;
    private BlockPos oldPos, pos;

    public RenderDroneAI(EntityDrone drone) {
        this.drone = drone;
        update();
    }

    public void update() {
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

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        for (Pair<RenderCoordWireframe, Integer> wireframe : blackListWireframes) {
            wireframe.getKey().render(matrixStack, buffer, partialTicks);
        }

        if (ItemPneumaticArmor.isPlayerDebuggingEntity(ClientUtils.getClientPlayer(), drone)) {
            IProgWidget activeWidget = drone.getActiveWidget();
            if (activeWidget != null) {
                double x, y, z;
                if (pos != null) {
                    x = getInterpolated(pos.getX(), oldPos.getX(), partialTicks);
                    y = getInterpolated(pos.getY(), oldPos.getY(), partialTicks);
                    z = getInterpolated(pos.getZ(), oldPos.getZ(), partialTicks);
                } else {
                    x = MathHelper.lerp(partialTicks, drone.prevPosX, drone.getPosX());
                    y = MathHelper.lerp(partialTicks, drone.prevPosY, drone.getPosY()) + 0.5;
                    z = MathHelper.lerp(partialTicks, drone.prevPosZ, drone.getPosZ());
                }
                GlStateManager.enableTexture();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.pushMatrix();
                GlStateManager.translated(x, y + 0.5, z);
                GlStateManager.scaled(0.01, 0.01, 0.01);
                GlStateManager.rotated(180, 1, 0, 0);
                GlStateManager.rotated(180 + Minecraft.getInstance().getRenderManager().playerViewY, 0, 1, 0);
                activeWidget.render();
                GlStateManager.popMatrix();
                GlStateManager.disableBlend();
            }
        }
    }

    private double getInterpolated(double newPos, double oldPos, float partialTicks) {
        double cosProgress = 0.5 - 0.5 * MathHelper.cos((float) Math.min(Math.PI, progress + partialTicks * 0.1F));
        return oldPos + (newPos - oldPos) * cosProgress;
    }

    public void addBlackListEntry(World world, BlockPos pos) {
        blackListWireframes.add(new MutablePair<>(new RenderCoordWireframe(world, pos), 60));
    }
}
