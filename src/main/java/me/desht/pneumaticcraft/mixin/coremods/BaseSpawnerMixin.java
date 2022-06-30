package me.desht.pneumaticcraft.mixin.coremods;

import me.desht.pneumaticcraft.common.entity.semiblock.SpawnerAgitatorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseSpawner.class)
public class BaseSpawnerMixin {
    @Inject(method="isNearPlayer", at = @At("RETURN"), cancellable = true)
    private void onIsNearPlayer(Level pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        //noinspection ConstantConditions
        if (SpawnerAgitatorEntity.isAgitated((BaseSpawner)(Object) this)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
