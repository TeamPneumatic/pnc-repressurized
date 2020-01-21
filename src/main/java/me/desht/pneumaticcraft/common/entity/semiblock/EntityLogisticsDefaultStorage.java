package me.desht.pneumaticcraft.common.entity.semiblock;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class EntityLogisticsDefaultStorage extends EntityLogisticsStorage {
    public static EntityLogisticsDefaultStorage createDefault(EntityType<EntityLogisticsDefaultStorage> type, World world) {
        return new EntityLogisticsDefaultStorage(type, world);
    }

    private EntityLogisticsDefaultStorage(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public int getColor() {
        return 0xFF008800;
    }

    @Override
    public int getPriority() {
        return 1;
    }}
