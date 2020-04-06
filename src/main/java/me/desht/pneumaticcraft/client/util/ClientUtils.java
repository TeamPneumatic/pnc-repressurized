package me.desht.pneumaticcraft.client.util;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Miscellaneous client-side utilities
 */
public class ClientUtils {
    /**
     * Emit particles from just above the given blockpos, which is generally a machine or similar.
     * Only call this clientside.
     *
     * @param world the world
     * @param pos the block pos
     * @param particle the particle type
     */
    public static void emitParticles(World world, BlockPos pos, EnumParticleTypes particle) {
        float xOff = world.rand.nextFloat() * 0.6F + 0.2F;
        float zOff = world.rand.nextFloat() * 0.6F + 0.2F;
        PneumaticCraftRepressurized.proxy.getClientWorld().spawnParticle(particle,
                pos.getX() + xOff, pos.getY() + 1.2, pos.getZ() + zOff,
                0, 0, 0);
    }

    @Nonnull
    public static ItemStack getWornArmor(EntityEquipmentSlot slot) {
        return Minecraft.getMinecraft().player.getItemStackFromSlot(slot);
    }

    public static double[] getTextureUV(IBlockState state, EnumFacing face) {
        if (state == null) return null;
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
        List<BakedQuad> quads = model.getQuads(state, face, 0L);
        if (!quads.isEmpty()) {
            TextureAtlasSprite sprite = quads.get(0).getSprite();
            return new double[] { sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV() };
        } else {
            return null;
        }
    }
}
