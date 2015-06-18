package pneumaticCraft.client.render.tileentity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.client.render.entity.RenderDrone;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.tileentity.TileEntityProgrammableController;

public class RenderProgrammableController extends TileEntitySpecialRenderer{
    private RenderDrone renderDrone;
    private EntityDrone drone;

    /*
     * TileEntitySpecialRenderer part
     */

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f){
        if(renderDrone == null) {
            renderDrone = new RenderDrone(false);
            renderDrone.setRenderManager(RenderManager.instance);
            drone = new EntityDrone(tileentity.getWorldObj());
        }
        TileEntityProgrammableController te = (TileEntityProgrammableController)tileentity;
        double droneX = te.oldCurX + (te.getPosition().xCoord - te.oldCurX) * f - te.xCoord + 0.5 + d0;
        double droneY = te.oldCurY + (te.getPosition().yCoord - te.oldCurY) * f - te.yCoord - 0.2 + d1;
        double droneZ = te.oldCurZ + (te.getPosition().zCoord - te.oldCurZ) * f - te.zCoord + 0.5 + d2;

        renderDrone.doRender((Entity)drone, droneX, droneY, droneZ, 0, f);
    }

}
