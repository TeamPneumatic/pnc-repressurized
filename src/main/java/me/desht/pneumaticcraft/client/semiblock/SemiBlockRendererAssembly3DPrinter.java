package me.desht.pneumaticcraft.client.semiblock;

import me.desht.pneumaticcraft.client.model.block.ModelAssembly3DPrinter;
import me.desht.pneumaticcraft.client.model.semiblocks.ModelHeatFrame;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockAssembly3DPrinter;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockSpawnerAgitator;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.opengl.GL11;

public class SemiBlockRendererAssembly3DPrinter implements ISemiBlockRenderer<SemiBlockAssembly3DPrinter> {
    private final ModelAssembly3DPrinter model = new ModelAssembly3DPrinter();

    @Override
    public void render(SemiBlockAssembly3DPrinter semiBlock, float partialTick) {
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(Textures.MODEL_ASSEMBLY_3D_PRINTER);

        //double brightness = 0.2;
       // GL11.glColor4d(brightness, brightness, brightness, 1);

        GL11.glTranslated(0.5, 1.5, 0.5);
        GL11.glRotated(180, 1, 0, 0);
        
        model.railY1.offsetY = semiBlock.getCurY(partialTick);
        model.railX.offsetX = semiBlock.getCurX(partialTick);
        model.printerHead.offsetZ = semiBlock.getCurZ(partialTick);

        model.render(null, 0, 0, 0, 0, 0, 1 / 16F);
        GL11.glPopMatrix();
        GL11.glColor4d(1, 1, 1, 1);
    }
}
