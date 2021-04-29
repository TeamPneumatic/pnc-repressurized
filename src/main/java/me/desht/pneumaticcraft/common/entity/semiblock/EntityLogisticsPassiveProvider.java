package me.desht.pneumaticcraft.common.entity.semiblock;

import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityLogisticsPassiveProvider extends EntityLogisticsActiveProvider {
    public EntityLogisticsPassiveProvider(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public int getColor() {
        return 0xFFFF0000;
    }

    @Override
    public boolean shouldProvideTo(int level) {
        return level > 2;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.MODEL_LOGISTICS_FRAME_PASSIVE_PROVIDER;  // TODO ridanisaurus
    }

}
