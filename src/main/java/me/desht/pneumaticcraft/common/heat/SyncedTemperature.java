package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketTemperatureSync;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;

import static net.minecraftforge.fml.network.PacketDistributor.TargetPoint;

public class SyncedTemperature {
    private final WeakReference<World> world;
    private final BlockPos pos;
    private final Direction dir;
    private final int id;

    private int currentTemp = 300;
    private int lastSyncedTemp = -1;

    public SyncedTemperature(TileEntity te, Direction dir) {
        this.world = new WeakReference<>(te.getWorld());
        this.pos = te.getPos();
        this.dir = dir;
        this.id = -1;
    }

    public SyncedTemperature(ISemiBlock semiBlock) {
        this.world = new WeakReference<>(semiBlock.getWorld());
        this.pos = semiBlock.getBlockPos();
        this.id = semiBlock.getTrackingId();
        this.dir = null;
    }

    public void setCurrentTemp(double currentTemp) {
        this.currentTemp = (int) currentTemp;

        if (shouldSync() && world.get() != null) {
            TargetPoint tp = new TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 256D, world.get().getDimension().getType());
            if (id < 0) {
                NetworkHandler.sendToAllAround(new PacketTemperatureSync(pos, dir, (int) currentTemp), tp);
            } else {
                NetworkHandler.sendToAllAround(new PacketTemperatureSync(id, (int) currentTemp), tp);
            }
            this.lastSyncedTemp = (int) currentTemp;
        }
    }

    private boolean shouldSync() {
        if (lastSyncedTemp < 0) return true; // initial sync

        int delta = Math.abs(lastSyncedTemp - currentTemp);

        if (currentTemp < 73) {
            return false;
        } else if (currentTemp < 473) {
            return delta >= 20;
        } else if (currentTemp < 873) {
            return delta >= 30;
        } else if (currentTemp < 1473) {
            return delta >= 80;
        } else {
            return false;
        }
    }
}
