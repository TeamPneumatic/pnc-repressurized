package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
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

    public void render(float partialTicks) {
        for (Pair<RenderCoordWireframe, Integer> wireframe : blackListWireframes) {
            wireframe.getKey().render(partialTicks);
        }

        if (pos != null) {
            IProgWidget activeWidget = drone.getActiveWidget();
            if (activeWidget != null) {
                double x = getInterpolated(pos.getX(), oldPos.getX(), partialTicks);
                double y = getInterpolated(pos.getY(), oldPos.getY(), partialTicks);
                double z = getInterpolated(pos.getZ(), oldPos.getZ(), partialTicks);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.pushMatrix();
                GlStateManager.translated(x + 0.5, y + 1.5, z + 0.5);
                GlStateManager.rotated(180, 1, 0, 0);
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
