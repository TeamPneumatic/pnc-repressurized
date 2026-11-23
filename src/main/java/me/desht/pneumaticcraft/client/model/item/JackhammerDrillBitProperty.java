package me.desht.pneumaticcraft.client.model.item;

import com.mojang.serialization.MapCodec;
import me.desht.pneumaticcraft.client.sound.MovingSoundJackhammer;
import me.desht.pneumaticcraft.common.item.DrillBitItem;
import me.desht.pneumaticcraft.common.item.JackHammerItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Replaces the pre-26.1 {@code pneumaticcraft:drill_bit} item property; used by a {@code minecraft:range_dispatch}
 * item model for the jackhammer.
 */
public record JackhammerDrillBitProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<JackhammerDrillBitProperty> MAP_CODEC = MapCodec.unit(new JackhammerDrillBitProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        DrillBitItem.DrillBitType type = JackHammerItem.getDrillBit(stack);
        if (type == DrillBitItem.DrillBitType.NONE) return 0f;
        if (level == null || owner == null || !(owner.asLivingEntity() instanceof Player player)) return 0.99f;
        long l = MovingSoundJackhammer.lastJackHammerTime(player);
        if (l <= 20) return Mth.sin((level.getGameTime() % 4 / 4f) * 3.141529f);
        else return 0.99f;
    }

    @Override
    public MapCodec<JackhammerDrillBitProperty> type() {
        return MAP_CODEC;
    }
}
