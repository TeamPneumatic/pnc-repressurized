package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.entity.living.EntityBasicDrone;
import me.desht.pneumaticcraft.common.inventory.handler.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public class ItemBasicDrone extends ItemDrone {

    private final BiFunction<World, PlayerEntity, EntityBasicDrone> droneCreator;
    
    public ItemBasicDrone(String name, BiFunction<World, PlayerEntity, EntityBasicDrone> droneCreator) {
        super(name);
        this.droneCreator = droneCreator;
    }

    @Override
    public void spawnDrone(PlayerEntity player, World world, BlockPos clickPos, Direction facing, BlockPos placePos, ItemStack iStack){
        EntityBasicDrone drone = droneCreator.apply(world, player);

        drone.setPosition(placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5);
        world.addEntity(drone);

        CompoundNBT stackTag = iStack.getTag();
        CompoundNBT entityTag = new CompoundNBT();
        drone.writeAdditional(entityTag);
        if (stackTag != null) {
            entityTag.putFloat("currentAir", stackTag.getFloat("currentAir"));
            entityTag.putInt("color", stackTag.getInt("color"));
            entityTag.put(ChargeableItemHandler.NBT_UPGRADE_TAG, stackTag.getCompound(ChargeableItemHandler.NBT_UPGRADE_TAG));
        }
        drone.readAdditional(entityTag);
        drone.addProgram(clickPos, facing, placePos, drone.progWidgets);
        TileEntityProgrammer.updatePuzzleConnections(drone.progWidgets);
        if (iStack.hasDisplayName()) drone.setCustomName(iStack.getDisplayName());

        drone.naturallySpawned = false;
        drone.onInitialSpawn(world, world.getDifficultyForLocation(placePos), SpawnReason.MOB_SUMMONED,  null, null);
    }

    @Override
    public boolean canProgram(ItemStack stack) {
        return false;
    }

    @Override
    public boolean upgradeApplies(IItemRegistry.EnumUpgrade upgrade) {
        // Currently the only "basic" drones are Logistics & Harvesting, neither of which can take
        // advantage of a larger inventory.  If future basic drones are added which can do so, we can
        // subclass ItemBasicDrone and override this to allow dispenser upgrades.
        switch (upgrade) {
            case VOLUME:
            case ITEM_LIFE:
            case SECURITY:
            case SPEED:
            case MAGNET:
                return true;
        }
        return false;
    }
}
