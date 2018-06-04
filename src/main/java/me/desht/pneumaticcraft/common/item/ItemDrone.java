package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.inventory.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemDrone extends ItemPneumatic implements IPressurizable, IChargingStationGUIHolderItem, IProgrammable, IUpgradeAcceptor {

    ItemDrone(String registryName) {
        super(registryName);
        setMaxDamage(1);
    }

    ItemDrone() {
        this("drone");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND) return EnumActionResult.PASS;

        if (!world.isRemote) {
            ItemStack iStack = player.getHeldItemMainhand();
            BlockPos placePos = pos.offset(facing);
            spawnDrone(player, world, pos, facing, placePos, iStack);
            iStack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }

    public void spawnDrone(EntityPlayer player, World world, BlockPos clickPos, EnumFacing facing, BlockPos placePos, ItemStack iStack){
        EntityDrone drone = new EntityDrone(world, player);

        drone.setPosition(placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5);
        world.spawnEntity(drone);

        NBTTagCompound stackTag = iStack.getTagCompound();
        NBTTagCompound entityTag = new NBTTagCompound();
        drone.writeEntityToNBT(entityTag);
        if (stackTag != null) {
            entityTag.setTag("widgets", stackTag.getTagList("widgets", 10).copy());
            entityTag.setFloat("currentAir", stackTag.getFloat("currentAir"));
            entityTag.setInteger("color", stackTag.getInteger("color"));
            entityTag.setTag(ChargeableItemHandler.NBT_UPGRADE_TAG, stackTag.getCompoundTag(ChargeableItemHandler.NBT_UPGRADE_TAG));
        }
        drone.readEntityFromNBT(entityTag);
        if (iStack.hasDisplayName()) drone.setCustomNameTag(iStack.getDisplayName());

        drone.naturallySpawned = false;
        drone.onInitialSpawn(world.getDifficultyForLocation(placePos), null);
    }

    public static void setProgWidgets(List<IProgWidget> widgets, ItemStack iStack) {
        NBTUtil.initNBTTagCompound(iStack);
        TileEntityProgrammer.setWidgetsToNBT(widgets, iStack.getTagCompound());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (isInCreativeTab(tab)) {
            subItems.add(new ItemStack(this));
            ItemStack chargedStack = new ItemStack(this);
            addAir(chargedStack, (int) (PneumaticValues.DRONE_VOLUME * PneumaticValues.DRONE_MAX_PRESSURE));
            subItems.add(chargedStack);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
        list.add("Pressure: " + PneumaticCraftUtils.roundNumberTo(getPressure(stack), 1) + " bar");
        UpgradableItemUtils.addUpgradeInformation(stack, world, list, flag);
        super.addInformation(stack, world, list, flag);
    }

    @Override
    public float getPressure(ItemStack iStack) {
        float volume = UpgradableItemUtils.getUpgrades(EnumUpgrade.VOLUME, iStack) * PneumaticValues.VOLUME_VOLUME_UPGRADE + PneumaticValues.DRONE_VOLUME;
        float oldVolume = NBTUtil.getFloat(iStack, "volume");
        if (volume < oldVolume) {
            float currentAir = NBTUtil.getFloat(iStack, "currentAir");
            currentAir *= volume / oldVolume;
            NBTUtil.setFloat(iStack, "currentAir", currentAir);
        }
        NBTUtil.setFloat(iStack, "volume", volume);
        return NBTUtil.getFloat(iStack, "currentAir") / volume;
    }

    @Override
    public void addAir(ItemStack iStack, int amount) {
        NBTUtil.setFloat(iStack, "currentAir", NBTUtil.getFloat(iStack, "currentAir") + amount);
    }

    @Override
    public float maxPressure(ItemStack iStack) {
        return PneumaticValues.DRONE_MAX_PRESSURE;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.DRONE;
    }

    @Override
    public boolean canProgram(ItemStack stack) {
        return true;
    }

    @Override
    public boolean usesPieces(ItemStack stack) {
        return true;
    }

    @Override
    public boolean showProgramTooltip() {
        return true;
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        Set<Item> set = new HashSet<>();
        for (EnumUpgrade upgrade : EnumUpgrade.values()) {
            if (upgradeApplies(upgrade)) {
                set.add(Itemss.upgrades.get(upgrade));
            }
        }
        return set;
    }

    public boolean upgradeApplies(EnumUpgrade upgrade) {
        switch (upgrade) {
            case VOLUME:
            case DISPENSER:
            case ITEM_LIFE:
            case SECURITY:
            case SPEED:
            case ENTITY_TRACKER:
            case MAGNET:
            case RANGE:
                return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return getUnlocalizedName() + ".name";
    }

}
