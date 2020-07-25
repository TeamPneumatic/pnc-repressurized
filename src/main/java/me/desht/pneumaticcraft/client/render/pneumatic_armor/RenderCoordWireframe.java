package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.List;

public class RenderCoordWireframe {
    public final BlockPos pos;
    public final World world;
    public int ticksExisted;

    public RenderCoordWireframe(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public static void addInfo(List<ITextComponent> tooltip, World world, BlockPos pos) {
        RenderCoordWireframe coordHandler = new RenderCoordWireframe(world, pos);

        // FIXME bleh
//        for (int i = 0; i < tooltip.size(); i++) {
//            if (tooltip.get(i).getFormattedText().contains("Coordinate Tracker")) {
//                tooltip.set(i, tooltip.get(i).appendText(" (tracking " + coordHandler.pos.getX() + ", " + coordHandler.pos.getY() + ", " + coordHandler.pos.getZ() + " in " + DimensionType.getKey(coordHandler.world.getDimension().getType()) + ")"));
//                break;
//            }
//        }
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        double minX = 0;
        double minY = 0;
        double minZ = 0;
        double maxX = 1;
        double maxY = 1;
        double maxZ = 1;
        float progress = (ticksExisted % 20 + partialTicks) / 20;
        matrixStack.push();
        matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
        float g = progress < 0.5F ? progress + 0.5F : 1.5F - progress;
        IVertexBuilder builder = buffer.getBuffer(ModRenderTypes.BLOCK_TRACKER);
        builder.pos(minX, minY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(minX, maxY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(minX, minY, maxZ).color(0, g, 1, 1).endVertex();
        builder.pos(minX, maxY, maxZ).color(0, g, 1, 1).endVertex();

        builder.pos(maxX, minY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(maxX, maxY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(maxX, minY, maxZ).color(0, g, 1, 1).endVertex();
        builder.pos(maxX, maxY, maxZ).color(0, g, 1, 1).endVertex();

        builder.pos(minX, minY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(maxX, minY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(minX, minY, maxZ).color(0, g, 1, 1).endVertex();
        builder.pos(maxX, minY, maxZ).color(0, g, 1, 1).endVertex();

        builder.pos(minX, maxY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(maxX, maxY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(minX, maxY, maxZ).color(0, g, 1, 1).endVertex();
        builder.pos(maxX, maxY, maxZ).color(0, g, 1, 1).endVertex();

        builder.pos(minX, minY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(minX, minY, maxZ).color(0, g, 1, 1).endVertex();
        builder.pos(maxX, minY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(maxX, minY, maxZ).color(0, g, 1, 1).endVertex();

        builder.pos(minX, maxY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(minX, maxY, maxZ).color(0, g, 1, 1).endVertex();
        builder.pos(maxX, maxY, minZ).color(0, g, 1, 1).endVertex();
        builder.pos(maxX, maxY, maxZ).color(0, g, 1, 1).endVertex();

        matrixStack.pop();
    }
}
