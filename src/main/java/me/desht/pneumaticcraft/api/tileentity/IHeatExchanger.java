package me.desht.pneumaticcraft.api.tileentity;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * Implemented by TileEntities or Blocks which transport heat. Keep in mind that when a Block is implementing it you
 * only can give off a constant resistance/temperature (like Lava and Ice).
 *
 * @author MineMaarten
 *         www.minemaarten.com
 */
public interface IHeatExchanger {

    /**
     * Your Tile Entity's constructor should create an instance of {@link IHeatExchangerLogic} using
     * {@link IHeatRegistry#getHeatExchangerLogic()} and keep a reference to it, and return it from this method.
     * You can return different exchanger logics for different sides (e.g. like the Vortex Tube). Keep in mind that
     * when you change a returned logic, you need to create a neighbor block change to notify the differences - you can
     * use {@link net.minecraft.world.World#notifyNeighborsOfStateChange(BlockPos, Block)} for this.
     *
     * @param side side of your block
     * @return a heat exchanger logic, or null if no heat should be exchanged on this side
     */
    IHeatExchangerLogic getHeatExchangerLogic(Direction side);

}
