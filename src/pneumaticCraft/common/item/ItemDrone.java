package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.api.item.IProgrammable;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.debug.DebugUtils;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.proxy.CommonProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemDrone extends ItemPneumatic implements IPressurizable, IChargingStationGUIHolderItem, IProgrammable{

    public ItemDrone(){
        setMaxStackSize(1);
    }

    @Override
    public void registerIcons(IIconRegister par1IconRegister){}

    @Override
    public boolean onItemUse(ItemStack iStack, EntityPlayer player, World world, int x, int y, int z, int side, float vecX, float vecY, float vecZ){
        if(!world.isRemote) {
            DebugUtils.printNBT(iStack.getTagCompound());
            EntityDrone drone = new EntityDrone(world, player);
            ForgeDirection dir = ForgeDirection.getOrientation(side);
            drone.setPosition(x + 0.5 + dir.offsetX, y + 0.5 + dir.offsetY, z + 0.5 + dir.offsetZ);
            world.spawnEntityInWorld(drone);

            NBTTagCompound stackTag = iStack.getTagCompound();
            NBTTagCompound entityTag = new NBTTagCompound();
            drone.writeEntityToNBT(entityTag);
            if(stackTag != null) {
                entityTag.setTag("widgets", stackTag.getTagList("widgets", 10).copy());
                entityTag.setFloat("currentAir", stackTag.getFloat("currentAir"));
                NBTTagCompound invTag = stackTag.getCompoundTag("Inventory");
                if(invTag != null) entityTag.setTag("Inventory", invTag.copy());
            }
            drone.readEntityFromNBT(entityTag);
            if(iStack.hasDisplayName()) drone.setCustomNameTag(iStack.getDisplayName());

            drone.naturallySpawned = false;
            drone.onSpawnWithEgg(null);
            iStack.stackSize--;
        }
        return true;
    }

    public static void setProgWidgets(List<IProgWidget> widgets, ItemStack iStack){
        NBTUtil.initNBTTagCompound(iStack);
        TileEntityProgrammer.setWidgetsToNBT(widgets, iStack.getTagCompound());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item par1, CreativeTabs tab, List subItems){
        subItems.add(new ItemStack(this));
        ItemStack chargedStack = new ItemStack(this);
        addAir(chargedStack, (int)(PneumaticValues.DRONE_VOLUME * PneumaticValues.DRONE_MAX_PRESSURE));
        subItems.add(chargedStack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    /**
     * allows items to add custom lines of information to the mouseover description
     */
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4){
        list.add("Pressure: " + PneumaticCraftUtils.roundNumberTo(getPressure(stack), 1) + " bar");
    }

    @Override
    public float getPressure(ItemStack iStack){
        float volume = ItemPneumaticArmor.getUpgrades(ItemMachineUpgrade.UPGRADE_VOLUME_DAMAGE, iStack) * PneumaticValues.VOLUME_VOLUME_UPGRADE + PneumaticValues.DRONE_VOLUME;
        float oldVolume = NBTUtil.getFloat(iStack, "volume");
        if(volume < oldVolume) {
            float currentAir = NBTUtil.getFloat(iStack, "currentAir");
            currentAir *= volume / oldVolume;
            NBTUtil.setFloat(iStack, "currentAir", currentAir);
        }
        NBTUtil.setFloat(iStack, "volume", volume);
        return NBTUtil.getFloat(iStack, "currentAir") / volume;
    }

    @Override
    public void addAir(ItemStack iStack, int amount){
        NBTUtil.setFloat(iStack, "currentAir", NBTUtil.getFloat(iStack, "currentAir") + amount);
    }

    @Override
    public float maxPressure(ItemStack iStack){
        return PneumaticValues.DRONE_MAX_PRESSURE;
    }

    @Override
    public int getGuiID(){
        return CommonProxy.GUI_ID_DRONE;
    }

    @Override
    public boolean canProgram(ItemStack stack){
        return true;
    }

    @Override
    public boolean usesPieces(ItemStack stack){
        return true;
    }

    @Override
    public boolean showProgramTooltip(){
        return true;
    }
}
