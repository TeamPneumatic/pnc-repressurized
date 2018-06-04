package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.entity.living.EntityBasicDrone;
import me.desht.pneumaticcraft.common.inventory.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public class ItemBasicDrone extends ItemDrone {

    private final BiFunction<World, EntityPlayer, EntityBasicDrone> droneCreator;
    
    public ItemBasicDrone(String name, BiFunction<World, EntityPlayer, EntityBasicDrone> droneCreator) {
        super(name);
        this.droneCreator = droneCreator;
        setMaxStackSize(64);
    }

    @Override
    public void spawnDrone(EntityPlayer player, World world, BlockPos clickPos, EnumFacing facing, BlockPos placePos, ItemStack iStack){
        EntityBasicDrone drone = droneCreator.apply(world, player);

        drone.setPosition(placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5);
        world.spawnEntity(drone);

        NBTTagCompound stackTag = iStack.getTagCompound();
        NBTTagCompound entityTag = new NBTTagCompound();
        drone.writeEntityToNBT(entityTag);
        if (stackTag != null) {
            entityTag.setFloat("currentAir", stackTag.getFloat("currentAir"));
            entityTag.setInteger("color", stackTag.getInteger("color"));
            entityTag.setTag(ChargeableItemHandler.NBT_UPGRADE_TAG, stackTag.getCompoundTag(ChargeableItemHandler.NBT_UPGRADE_TAG));
        }
        drone.readEntityFromNBT(entityTag);
        drone.addProgram(clickPos, facing, placePos, drone.progWidgets);
        TileEntityProgrammer.updatePuzzleConnections(drone.progWidgets);
        if (iStack.hasDisplayName()) drone.setCustomNameTag(iStack.getDisplayName());

        drone.naturallySpawned = false;
        drone.onInitialSpawn(world.getDifficultyForLocation(placePos), null);
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
