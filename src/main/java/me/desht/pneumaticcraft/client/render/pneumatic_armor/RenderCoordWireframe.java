package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;

public class RenderCoordWireframe {
    public final BlockPos pos;
    public final RegistryKey<World> worldKey;
    public int ticksExisted;

    public RenderCoordWireframe(World world, BlockPos pos) {
        this.worldKey = world.getDimensionKey();
        this.pos = pos;
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        float minX = 0;
        float minY = 0;
        float minZ = 0;
        float maxX = 1;
        float maxY = 1;
        float maxZ = 1;
        float progress = (ticksExisted % 20 + partialTicks) / 20;
        matrixStack.push();
        matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
        float g = progress < 0.5F ? progress + 0.5F : 1.5F - progress;
        Matrix4f posMat = matrixStack.getLast().getMatrix();
        IVertexBuilder builder = buffer.getBuffer(ModRenderTypes.BLOCK_TRACKER);
        builder.pos(posMat, minX, minY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, minX, maxY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, minX, minY, maxZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, minX, maxY, maxZ).color(0, g, 1, 1).endVertex();

        builder.pos(posMat, maxX, minY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, maxX, maxY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, maxX, minY, maxZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, maxX, maxY, maxZ).color(0, g, 1, 1).endVertex();

        builder.pos(posMat, minX, minY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, maxX, minY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, minX, minY, maxZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, maxX, minY, maxZ).color(0, g, 1, 1).endVertex();

        builder.pos(posMat, minX, maxY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, maxX, maxY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, minX, maxY, maxZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, maxX, maxY, maxZ).color(0, g, 1, 1).endVertex();

        builder.pos(posMat, minX, minY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, minX, minY, maxZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, maxX, minY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, maxX, minY, maxZ).color(0, g, 1, 1).endVertex();

        builder.pos(posMat, minX, maxY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, minX, maxY, maxZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, maxX, maxY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(posMat, maxX, maxY, maxZ).color(0, g, 1, 1).endVertex();

        matrixStack.pop();
    }
}
