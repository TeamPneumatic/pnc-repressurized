package me.desht.pneumaticcraft.common.entity.semiblock;

import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityLogisticsDefaultStorage extends EntityLogisticsStorage {
    public EntityLogisticsDefaultStorage(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public int getColor() {
        return 0xFF008800;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.MODEL_LOGISTICS_FRAME_DEFAULT_STORAGE;  // TODO ridanisaurus
    }

}
