package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * @author Vexatos
 */
public class DriverPneumaticCraft extends DriverSidedTileEntity {

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos blockPos, EnumFacing enumFacing) {
        return new InternalManagedEnvironment((TileEntityBase) world.getTileEntity(blockPos));
    }

    @Override
    public Class<?> getTileEntityClass() {
        return TileEntityBase.class;
    }

    public static class InternalManagedEnvironment extends AbstractManagedEnvironment implements ManagedPeripheral, NamedBlock {
        protected final TileEntityBase tile;

        InternalManagedEnvironment(TileEntityBase tile) {
            this.tile = tile;
            setNode(Network.newNode(this, Visibility.Network).withComponent(this.tile.getType(), Visibility.Network).create());
        }

        @Override
        public String preferredName() {
            return tile.getType();
        }

        @Override
        public int priority() {
            return 20;
        }

        @Override
        public String[] methods() {
            return tile.getMethodNames();
        }

        @Override
        public Object[] invoke(String method, Context context, Arguments args) throws Exception {
            if ("greet".equals(method)) {
                return new Object[]{String.format("Hello, %s!", args.checkString(0))};
            }
            List<ILuaMethod> luaMethods = tile.getLuaMethods();
            for (ILuaMethod m : luaMethods) {
                if (m.getMethodName().equals(method)) {
                    return m.call(args.toArray());
                }
            }
            throw new IllegalArgumentException("Can't invoke method with name \"" + method + "\". not registered");
        }
    }

}
