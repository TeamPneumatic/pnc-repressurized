package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

class CamoTECache {
    private static Set<BlockPos> teSet;
    private static BlockPos lastPlayerPos;

    static Set<BlockPos> getCamouflageableBlockPos(World worldIn, EntityPlayer entityIn) {
        if (worldIn == null) return Collections.emptySet();

        if (lastPlayerPos == null || teSet == null || entityIn.getDistanceSq(lastPlayerPos) > 9) {
            lastPlayerPos = entityIn.getPosition();
            teSet = worldIn.loadedTileEntityList.stream()
                    .filter(te -> te instanceof ICamouflageableTE && te.getPos().distanceSq(entityIn.posX, entityIn.posY, entityIn.posZ) < 100
                    )
                    .map(TileEntity::getPos)
                    .collect(Collectors.toSet());
        }
        return teSet;
    }

    static void clearCache() {
        teSet = null;
    }
}
