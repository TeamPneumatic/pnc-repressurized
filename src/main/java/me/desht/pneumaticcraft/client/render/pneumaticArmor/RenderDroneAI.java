package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.entity.item.EntityItem;
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
    private final RenderEntityItem renderItem;
    private final EntityItem entityItem;
    private final List<Pair<RenderCoordWireframe, Integer>> blackListWireframes = new ArrayList<Pair<RenderCoordWireframe, Integer>>();
    private float progress = 0;
    private BlockPos oldPos, pos;

    public RenderDroneAI(EntityDrone drone) {
        this.drone = drone;
        renderItem = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem()); //TODO 1.8 test
        entityItem = new EntityItem(drone.world);
        update();
    }

    public void update() {
        entityItem.age += 4;
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
            int color = ItemPlastic.getColour(drone.getActiveProgram());
            double x = getInterpolated(pos.getX(), oldPos.getX(), partialTicks);
            double y = getInterpolated(pos.getY(), oldPos.getY(), partialTicks);
            double z = getInterpolated(pos.getZ(), oldPos.getZ(), partialTicks);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.pushMatrix();
            float[] c = colorToRGBA(color);
            GlStateManager.color(c[1], c[2], c[3], 0.5f);
            GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
            GlStateManager.rotate(180, 1, 0, 0);
            RenderUtils.render3DArrow();
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
            GlStateManager.disableBlend();
        }
//        ItemStack activeProgram = drone.getActiveProgram();
//        if (!activeProgram.isEmpty() && pos != null) {
//            entityItem.setItem(activeProgram);
//            GL11.glColor4d(1, 1, 1, 1);
//            GL11.glEnable(GL11.GL_TEXTURE_2D);
//            renderItem.doRender(entityItem, getInterpolated(pos.getX(), oldPos.getX(), partialTicks) + 0.5, getInterpolated(pos.getY(), oldPos.getY(), partialTicks) + 0.5, getInterpolated(pos.getZ(), oldPos.getZ(), partialTicks) + 0.5, 0, partialTicks * 4);
//            GL11.glDisable(GL11.GL_LIGHTING);
//        }
    }

    private double getInterpolated(double newPos, double oldPos, float partialTicks) {
        double cosProgress = 0.5 - 0.5 * MathHelper.cos((float) Math.min(Math.PI, progress + partialTicks * 0.1F));
        return oldPos + (newPos - oldPos) * cosProgress;
    }

    public void addBlackListEntry(World world, BlockPos pos) {
        blackListWireframes.add(new MutablePair<>(new RenderCoordWireframe(world, pos), 60));
    }

    private static float[] colorToRGBA(int color) {
        float[] c = new float[4];
        c[0] = (color >> 24 & 255) / 255.0F;
        c[1] = (color >> 16 & 255) / 255.0F;
        c[2] = (color >> 8 & 255) / 255.0F;
        c[3] = (color & 255) / 255.0F;
        return c;
    }
}
