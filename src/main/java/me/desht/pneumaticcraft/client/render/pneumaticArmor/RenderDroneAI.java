package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

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
        Reflections.setItemAge(entityItem, Reflections.getItemAge(entityItem) + 4);
//        entityItem.age += 4;
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

        ItemStack activeProgram = drone.getActiveProgram();
        if (!activeProgram.isEmpty() && pos != null) {
            entityItem.setItem(activeProgram);
            GL11.glColor4d(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            renderItem.doRender(entityItem, getInterpolated(pos.getX(), oldPos.getX(), partialTicks) + 0.5, getInterpolated(pos.getY(), oldPos.getY(), partialTicks) + 0.5, getInterpolated(pos.getZ(), oldPos.getZ(), partialTicks) + 0.5, 0, partialTicks * 4);
            GL11.glDisable(GL11.GL_LIGHTING);
        }
    }

    private double getInterpolated(double newPos, double oldPos, float partialTicks) {
        double cosProgress = 0.5 - 0.5 * Math.cos(Math.min(Math.PI, progress + partialTicks * 0.1F));
        return oldPos + (newPos - oldPos) * cosProgress;
    }

    public void addBlackListEntry(World world, BlockPos pos) {
        blackListWireframes.add(new MutablePair(new RenderCoordWireframe(world, pos), 60));
    }
}
