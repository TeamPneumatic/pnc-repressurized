package pneumaticCraft.client.render.pneumaticArmor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.entity.living.EntityDrone;

public class RenderDroneAI{
    private final EntityDrone drone;
    private final RenderItem renderItem;
    private final EntityItem entityItem;
    private final List<Pair<RenderCoordWireframe, Integer>> blackListWireframes = new ArrayList<Pair<RenderCoordWireframe, Integer>>();
    private float progress = 0;
    private ChunkPosition oldPos, pos;

    public RenderDroneAI(EntityDrone drone){
        this.drone = drone;
        renderItem = new RenderItem();
        renderItem.setRenderManager(RenderManager.instance);
        entityItem = new EntityItem(drone.worldObj);
        update();
    }

    public void update(){
        entityItem.age += 4;
        ChunkPosition lastPos = pos;
        pos = drone.getTargetedBlock();
        if(pos != null) {
            if(lastPos == null) {
                oldPos = pos;
            } else if(!pos.equals(lastPos)) {
                progress = 0;
                oldPos = lastPos;
            }
        } else {
            oldPos = null;
        }
        progress = Math.min((float)Math.PI, progress + 0.1F);

        Iterator<Pair<RenderCoordWireframe, Integer>> iterator = blackListWireframes.iterator();
        while(iterator.hasNext()) {
            Pair<RenderCoordWireframe, Integer> wireframe = iterator.next();
            wireframe.getKey().ticksExisted++;
            wireframe.setValue(wireframe.getValue() - 1);
            if(wireframe.getValue() <= 0) {
                iterator.remove();
            }
        }
    }

    public void render(float partialTicks){
        for(Pair<RenderCoordWireframe, Integer> wireframe : blackListWireframes) {
            wireframe.getKey().render(partialTicks);
        }

        ItemStack activeProgram = drone.getActiveProgram();
        if(activeProgram != null && pos != null) {
            entityItem.setEntityItemStack(activeProgram);
            GL11.glColor4d(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            renderItem.doRender(entityItem, getInterpolated(pos.chunkPosX, oldPos.chunkPosX, partialTicks) + 0.5, getInterpolated(pos.chunkPosY, oldPos.chunkPosY, partialTicks) + 0.5, getInterpolated(pos.chunkPosZ, oldPos.chunkPosZ, partialTicks) + 0.5, 0, partialTicks * 4);
            GL11.glDisable(GL11.GL_LIGHTING);
        }
    }

    private double getInterpolated(double newPos, double oldPos, float partialTicks){
        double cosProgress = 0.5 - 0.5 * Math.cos(Math.min(Math.PI, progress + partialTicks * 0.1F));
        return oldPos + (newPos - oldPos) * cosProgress;
    }

    public void addBlackListEntry(World world, int x, int y, int z){
        blackListWireframes.add(new MutablePair(new RenderCoordWireframe(world, x, y, z), 60));
    }
}
