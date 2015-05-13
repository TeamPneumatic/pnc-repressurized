package pneumaticCraft.api.drone;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidTank;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.common.progwidgets.IProgWidget;

public interface IDrone extends IPressurizable{
    /**
     * 
     * @param upgradeIndex metadata value of the upgrade item
     * @return amount of inserted upgrades in the drone
     */
    public int getUpgrades(int upgradeIndex);

    public World getWorld();

    public IFluidTank getTank();

    public IInventory getInventory();

    public Vec3 getPosition();

    public PathNavigate getNavigator();

    public void sendWireframeToClient(int x, int y, int z);

    public EntityPlayerMP getFakePlayer();

    public boolean isBlockValidPathfindBlock(int x, int y, int z);

    public void dropItem(ItemStack stack);

    public void setDugBlock(int x, int y, int z);

    public List<IProgWidget> getProgWidgets();

    public void setActiveProgram(IProgWidget widget);

    public boolean isProgramApplicable(IProgWidget widget);

    public EntityAITasks getTargetAI();

    public IExtendedEntityProperties getProperty(String key);

    public void setProperty(String key, IExtendedEntityProperties property);

    public void setEmittingRedstone(ForgeDirection orientation, int emittingRedstone);

    public double getSpeed();

    public void setName(String string);

    public void setCarryingEntity(Entity entity);

    public Entity getCarryingEntity();

    public boolean isAIOverriden();

    public void onItemPickupEvent(EntityItem curPickingUpEntity, int stackSize);
}
