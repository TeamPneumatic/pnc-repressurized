package me.desht.pneumaticcraft.client.util;

import com.google.common.base.Function;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

public class RenderUtils extends Render {
    public static RenderUtils INSTANCE = new RenderUtils();

    public static Function<ResourceLocation, TextureAtlasSprite> TEXTURE_GETTER = new Function<ResourceLocation, TextureAtlasSprite>() {
        @Override
        public TextureAtlasSprite apply(ResourceLocation location) {
            return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
        }
    };

    private RenderUtils() {
        super(Minecraft.getMinecraft().getRenderManager());
    }

    public void renderLiquid(FluidTankInfo info, RenderInfo renderInfo, World world) {
        if (info.fluid.getFluid().getBlock() != null) {
            renderInfo.baseBlock = info.fluid.getFluid().getBlock();
        } else {
            renderInfo.baseBlock = Blocks.WATER;
        }
        renderInfo.texture = info.fluid.getFluid().getStill(info.fluid); //TODO 1.8 still or flowing?
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        // Tessellator.instance.setColorOpaque_I();
        int color = info.fluid.getFluid().getColor(info.fluid);
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        GL11.glColor4ub((byte) red, (byte) green, (byte) blue, (byte) 255);
        RenderUtils.INSTANCE.renderBlock(renderInfo, world, 0, 0, 0, false, true);
    }

    public void renderBlock(RenderInfo info, IBlockAccess blockAccess, int x, int y, int z, boolean doLight, boolean doTessellating) {
        renderBlock(info, blockAccess, x, y, z, x, y, z, doLight, doTessellating);
    }

    /**
     * This method is derived from BuildCraft's RenderEntityBlock.java, found at https://github.com/BuildCraft/BuildCraft/blob/6.1.x/common/buildcraft/core/render/RenderEntityBlock.java
     * This is edited by @author MineMaarten.
     *
     * @param info
     * @param blockAccess
     * @param x
     * @param y
     * @param z
     * @param lightX
     * @param lightY
     * @param lightZ
     * @param doLight
     * @param doTessellating
     */
    public void renderBlock(RenderInfo info, IBlockAccess blockAccess, double x, double y, double z, int lightX, int lightY, int lightZ, boolean doLight, boolean doTessellating) {
        /*    float lightBottom = 0.5F;
            float lightTop = 1.0F;
            float lightEastWest = 0.8F;
            float lightNorthSouth = 0.6F;

            BufferBuilder wr = Tessellator.getInstance()getBuffer();

            boolean realDoLight = doLight;

            if(blockAccess == null) {
                realDoLight = false;
            }

            boolean ambientOcclusion = renderBlocks.enableAO;
            if(!realDoLight) {
                tessellator.setColorOpaque_F(1, 1, 1);
                renderBlocks.enableAO = false;
            }

            if(doTessellating) wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

            float light = 0;
            if(realDoLight) {
                if(info.light < 0) {
                    light = info.baseBlock.getMixedBrightnessForBlock(blockAccess, lightX, lightY, lightZ);
                    light = light + (1.0f - light) * 0.4f;
                } else {
                    light = info.light;
                }
                int brightness = 0;
                if(info.brightness < 0) {
                    brightness = info.baseBlock.getMixedBrightnessForBlock(blockAccess, lightX, lightY, lightZ);
                } else {
                    brightness = info.brightness;
                }
                tessellator.setBrightness(brightness);
                tessellator.setColorOpaque_F(lightBottom * light, lightBottom * light, lightBottom * light);
            } else {
                //          tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
                if(info.brightness >= 0) {
                    tessellator.setBrightness(info.brightness);
                }
            }

            renderBlocks.setRenderBounds(info.minX, info.minY, info.minZ, info.maxX, info.maxY, info.maxZ);

            if(info.renderSide[0]) {
                renderBlocks.renderFaceYNeg(info.baseBlock, x, y, z, info.getBlockTextureFromSide(0));
            }

            if(realDoLight) {
                tessellator.setColorOpaque_F(lightTop * light, lightTop * light, lightTop * light);
            }

            if(info.renderSide[1]) {
                renderBlocks.renderFaceYPos(info.baseBlock, x, y, z, info.getBlockTextureFromSide(1));
            }

            if(realDoLight) {
                tessellator.setColorOpaque_F(lightEastWest * light, lightEastWest * light, lightEastWest * light);
            }

            if(info.renderSide[2]) {
                renderBlocks.renderFaceZNeg(info.baseBlock, x, y, z, info.getBlockTextureFromSide(2));
            }

            if(realDoLight) {
                tessellator.setColorOpaque_F(lightEastWest * light, lightEastWest * light, lightEastWest * light);
            }

            if(info.renderSide[3]) {
                renderBlocks.renderFaceZPos(info.baseBlock, x, y, z, info.getBlockTextureFromSide(3));
            }

            if(realDoLight) {
                tessellator.setColorOpaque_F(lightNorthSouth * light, lightNorthSouth * light, lightNorthSouth * light);
            }

            if(info.renderSide[4]) {
                renderBlocks.renderFaceXNeg(info.baseBlock, x, y, z, info.getBlockTextureFromSide(4));
            }

            if(realDoLight) {
                tessellator.setColorOpaque_F(lightNorthSouth * light, lightNorthSouth * light, lightNorthSouth * light);
            }

            if(info.renderSide[5]) {
                renderBlocks.renderFaceXPos(info.baseBlock, x, y, z, info.getBlockTextureFromSide(5));
            }

            renderBlocks.enableAO = ambientOcclusion;

            if(doTessellating) Tessellator.getInstance().draw();*/
        //TODO 1.8 implement
    }

    @Override
    public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {

    }

    @Override
    protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
        return null;
    }

    /**
     * This class is derived from BuildCraft's RenderEntityBlock.java, found at https://github.com/BuildCraft/BuildCraft/blob/6.1.x/common/buildcraft/core/render/RenderEntityBlock.java
     * This is edited by @author MineMaarten.
     */
    public static class RenderInfo {

        public double minX;
        public double minY;
        public double minZ;
        public double maxX;
        public double maxY;
        public double maxZ;
        public Block baseBlock = Blocks.SAND;
        public ResourceLocation texture = null;
        public boolean[] renderSide = new boolean[6];
        public float light = -1f;
        public int brightness = -1;
        private int meta;

        public RenderInfo() {
            setRenderAllSides();
        }

        public RenderInfo(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
            this();
            setBounds(minX, minY, minZ, maxX, maxY, maxZ);
        }

        public RenderInfo setMeta(int meta) {
            this.meta = meta;
            return this;
        }

        public final void setBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public final void setRenderSingleSide(int side) {
            Arrays.fill(renderSide, false);
            renderSide[side] = true;
        }

        public final void setRenderAllSides() {
            Arrays.fill(renderSide, true);
        }

        public void rotate() {
            double temp = minX;
            minX = minZ;
            minZ = temp;

            temp = maxX;
            maxX = maxZ;
            maxZ = temp;
        }

        public void reverseX() {
            double temp = minX;
            minX = 1 - maxX;
            maxX = 1 - temp;
        }

        public void reverseZ() {
            double temp = minZ;
            minZ = 1 - maxZ;
            maxZ = 1 - temp;
        }
    }

    public static void glColorHex(int color) {
        float alpha = (color >> 24 & 255) / 255F;
        float red = (color >> 16 & 255) / 255F;
        float green = (color >> 8 & 255) / 255F;
        float blue = (color & 255) / 255F;
        GL11.glColor4d(red, green, blue, alpha);
        GlStateManager.color(red, green, blue, alpha);
    }

    public static void glColorHex(int color, int alpha) {
        glColorHex(color | alpha << 24);
    }

    public static void render3DArrow() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        double arrowTipLength = 0.2;
        double arrowTipRadius = 0.25;
        double baseLength = 0.3;
        double baseRadius = 0.15;

        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        for (int i = PneumaticCraftUtils.sin.length - 1; i >= 0; i--) {
            double sin = PneumaticCraftUtils.sin[i] * baseRadius;
            double cos = PneumaticCraftUtils.cos[i] * baseRadius;
            wr.pos(sin, 0, cos).endVertex();
        }
        Tessellator.getInstance().draw();
        wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        for (int i = PneumaticCraftUtils.sin.length - 1; i >= 0; i--) {
            double sin = PneumaticCraftUtils.sin[i] * arrowTipRadius;
            double cos = PneumaticCraftUtils.cos[i] * arrowTipRadius;
            wr.pos(sin, baseLength, cos).endVertex();
        }
        Tessellator.getInstance().draw();
        wr.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
        for (int i = PneumaticCraftUtils.sin.length - 1; i >= 0; i--) {
            double sin = PneumaticCraftUtils.sin[i] * baseRadius;
            double cos = PneumaticCraftUtils.cos[i] * baseRadius;
            wr.pos(sin, 0, cos).endVertex();
            wr.pos(sin, baseLength, cos).endVertex();
        }
        Tessellator.getInstance().draw();

        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        wr.pos(0, baseLength + arrowTipLength, 0).endVertex();
        for (int i = 0; i < PneumaticCraftUtils.sin.length; i++) {
            double sin = PneumaticCraftUtils.sin[i] * arrowTipRadius;
            double cos = PneumaticCraftUtils.cos[i] * arrowTipRadius;
            wr.pos(sin, baseLength, cos).endVertex();
        }
        wr.pos(0, baseLength, arrowTipRadius).endVertex();
        Tessellator.getInstance().draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
