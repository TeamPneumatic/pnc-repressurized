package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import net.minecraft.util.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Non-API way to get a heat exchanger for a tile entity
 */
@FunctionalInterface
public interface IHeatExchangingTE {
    /**
     * Get the heat exchanger on the given face. This may return null iff the direction is not null.
     * @param dir the side of the block to check, or null for the default/primary heat exchanger
     * @return a heat exchanger
     */
    @Nullable
    IHeatExchangerLogic getHeatExchanger(Direction dir);

    /**
     * Get the heat exchanger on the null or default face.  This must never return null.
     * @return the primary heat exchanger
     */
    @Nonnull
    default IHeatExchangerLogic getHeatExchanger() {
        return Objects.requireNonNull(getHeatExchanger(null));
    }
}
