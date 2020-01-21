package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityTransferGadget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static me.desht.pneumaticcraft.common.entity.semiblock.EntityTransferGadget.EnumInputOutput;

public class RenderTransferGadget extends RenderSemiblockBase<EntityTransferGadget> {
    public static final IRenderFactory<EntityTransferGadget> FACTORY = RenderTransferGadget::new;

    private final Map<AxisAlignedBB, Integer> renderListCache = new HashMap<>();

    private RenderTransferGadget(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    @Override
    public void doRender(EntityTransferGadget entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.isAir() || entity.renderingOffset == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.setProfile(GlStateManager.Profile.TRANSPARENT_MODEL);

        AxisAlignedBB aabb = entity.getBoundingBox();
        setupColour(entity);

        Vec3d offset = entity.renderingOffset;
        if (entity.getTimeSinceHit() > 0) wobble(entity, partialTicks);
        GlStateManager.translated(x - entity.posX + offset.getX(), y - entity.posY + offset.getY(), z - entity.posZ + offset.getZ());
        int renderList = renderListCache.computeIfAbsent(aabb, this::compileRenderList);
        GlStateManager.callList(renderList);
        GlStateManager.disableBlend();
        GlStateManager.unsetProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
        GlStateManager.enableTexture();
        GlStateManager.popMatrix();
        GlStateManager.color4f(1, 1, 1, 1);
    }

    private void setupColour(EntityTransferGadget entity) {
        EnumInputOutput io = entity.getIOMode();
        if (io == EnumInputOutput.INPUT) {
            GlStateManager.color4f(0, 0, 1, 0.75F);
        } else {
            GlStateManager.color4f(1, 0.3F, 0, 0.75F);
        }
    }

    private int compileRenderList(AxisAlignedBB aabb) {
        int renderList = GlStateManager.genLists(1);
        GlStateManager.newList(renderList, GL11.GL_COMPILE);

        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        wr.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();

        wr.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        wr.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();

        wr.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        wr.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();

        wr.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();

        wr.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        wr.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();

        wr.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();

        Tessellator.getInstance().draw();

        GlStateManager.endList();
        return renderList;
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityTransferGadget entityTransferGadget) {
        return null;
    }
}
