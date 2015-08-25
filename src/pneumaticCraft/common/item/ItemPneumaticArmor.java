package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.client.render.item.RenderItemPneumaticHelmet;
import pneumaticCraft.client.render.pneumaticArmor.RenderCoordWireframe;
import pneumaticCraft.common.DateEventHandler;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;
import thaumcraft.api.IGoggles;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.IRevealer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Optional.InterfaceList({@Interface(iface = "thaumcraft.api.IRepairable", modid = ModIds.THAUMCRAFT), @Interface(iface = "thaumcraft.api.IGoggles", modid = ModIds.THAUMCRAFT), @Interface(iface = "thaumcraft.api.IVisDiscountGear", modid = ModIds.THAUMCRAFT), @Interface(iface = "thaumcraft.api.nodes.IRevealer", modid = ModIds.THAUMCRAFT)})
public class ItemPneumaticArmor extends ItemArmor implements IPressurizable, IChargingStationGUIHolderItem,
        IRepairable, IRevealer, IGoggles, IVisDiscountGear{
    private final String textureLocation;
    private final int volume;

    public ItemPneumaticArmor(String textureLocation, ItemArmor.ArmorMaterial par2EnumArmorMaterial, int par3,
            int par4, int volume, int maxAir){
        super(par2EnumArmorMaterial, par3, par4);
        this.textureLocation = textureLocation;
        this.volume = volume;
        setMaxDamage(maxAir);
        setCreativeTab(PneumaticCraft.tabPneumaticCraft);
    }

    @Override
    public void registerIcons(IIconRegister register){
        itemIcon = register.registerIcon(Textures.ICON_LOCATION + textureLocation);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type){
        return Textures.ARMOR_PNEUMATIC + "_1.png";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item par1, CreativeTabs tab, List subItems){
        subItems.add(new ItemStack(this));
        ItemStack chargedStack = new ItemStack(this);
        addAir(chargedStack, PneumaticValues.PNEUMATIC_HELMET_VOLUME * 10);
        subItems.add(chargedStack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack iStack, EntityPlayer player, List textList, boolean par4){
        float pressure = getPressure(iStack);
        textList.add((pressure < 0.5F ? EnumChatFormatting.RED : EnumChatFormatting.DARK_GREEN) + "Pressure: " + Math.round(pressure * 10D) / 10D + " bar");
        ItemStack[] inventoryStacks = getUpgradeStacks(iStack);
        boolean isArmorEmpty = true;
        for(ItemStack stack : inventoryStacks) {
            if(stack != null) {
                isArmorEmpty = false;
                break;
            }
        }
        if(isArmorEmpty) {
            textList.add("Insert in Charging Station to install upgrades");
        } else {
            textList.add("Upgrades installed:");
            PneumaticCraftUtils.sortCombineItemStacksAndToString(textList, inventoryStacks);
            ItemStack searchedStack = getSearchedStack(iStack);
            if(searchedStack != null) {
                for(int i = 0; i < textList.size(); i++) {
                    if(((String)textList.get(i)).contains("Item Search")) {
                        textList.set(i, textList.get(i) + " (searching " + searchedStack.getDisplayName() + ")");
                        break;
                    }
                }
            }
            RenderCoordWireframe coordHandler = getCoordTrackLocation(iStack);
            if(coordHandler != null) {
                for(int i = 0; i < textList.size(); i++) {
                    if(((String)textList.get(i)).contains("Coordinate Tracker")) {
                        textList.set(i, textList.get(i) + " (tracking " + coordHandler.x + ", " + coordHandler.y + ", " + coordHandler.z + " in " + coordHandler.worldObj.provider.getDimensionName() + ")");
                        break;
                    }
                }
            }
        }
        ItemPneumatic.addTooltip(iStack, player, textList);
    }

    /**
     * Retrieves the upgrades currently installed on the given armor stack.
     */
    public static ItemStack[] getUpgradeStacks(ItemStack iStack){
        NBTTagCompound tag = NBTUtil.getCompoundTag(iStack, "UpgradeInventory");
        ItemStack[] inventoryStacks = new ItemStack[9];
        if(tag != null) {
            NBTTagList itemList = tag.getTagList("Items", 10);
            if(itemList != null) {
                for(int i = 0; i < itemList.tagCount(); i++) {
                    NBTTagCompound slotEntry = itemList.getCompoundTagAt(i);
                    int j = slotEntry.getByte("Slot");
                    if(j >= 0 && j < 9) {
                        inventoryStacks[j] = ItemStack.loadItemStackFromNBT(slotEntry);
                    }
                }
            }
        }
        return inventoryStacks;
    }

    public static int getUpgrades(int upgradeDamage, ItemStack iStack){
        int upgrades = 0;
        ItemStack[] stacks = getUpgradeStacks(iStack);
        for(ItemStack stack : stacks) {
            if(stack != null && stack.getItem() == Itemss.machineUpgrade && stack.getItemDamage() == upgradeDamage) {
                upgrades += stack.stackSize;
            }
        }
        return upgrades;
    }

    @SideOnly(Side.CLIENT)
    public static ItemStack getSearchedStack(){
        return getSearchedStack(PneumaticCraft.proxy.getPlayer().getCurrentArmor(3));
    }

    public static ItemStack getSearchedStack(ItemStack helmetStack){
        if(helmetStack == null || !NBTUtil.hasTag(helmetStack, "SearchStack")) return null;
        NBTTagCompound tag = NBTUtil.getCompoundTag(helmetStack, "SearchStack");
        if(tag.getInteger("itemID") == -1) return null;
        return new ItemStack(Item.getItemById(tag.getInteger("itemID")), 1, tag.getInteger("itemDamage"));
    }

    @SideOnly(Side.CLIENT)
    public static RenderCoordWireframe getCoordTrackLocation(ItemStack helmetStack){
        if(helmetStack == null || !NBTUtil.hasTag(helmetStack, "CoordTracker")) return null;
        NBTTagCompound tag = NBTUtil.getCompoundTag(helmetStack, "CoordTracker");
        if(tag.getInteger("y") == -1 || FMLClientHandler.instance().getClient().theWorld.provider.dimensionId != tag.getInteger("dimID")) return null;
        return new RenderCoordWireframe(FMLClientHandler.instance().getClient().theWorld, tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
    }

    @SideOnly(Side.CLIENT)
    public static String getEntityFilter(ItemStack helmetStack){
        if(helmetStack == null || !NBTUtil.hasTag(helmetStack, "entityFilter")) return "";
        return NBTUtil.getString(helmetStack, "entityFilter");
    }

    public static void setEntityFilter(ItemStack helmetStack, String filter){
        if(helmetStack != null) {
            NBTUtil.setString(helmetStack, "entityFilter", filter);
        }
    }

    @Override
    public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack){
        return false;
    }

    @Override
    public float getPressure(ItemStack iStack){
        int volume = ItemPneumaticArmor.getUpgrades(ItemMachineUpgrade.UPGRADE_VOLUME_DAMAGE, iStack) * PneumaticValues.VOLUME_VOLUME_UPGRADE + PneumaticValues.PNEUMATIC_HELMET_VOLUME;
        int oldVolume = NBTUtil.getInteger(iStack, "volume");
        if(volume < oldVolume) {
            int currentAir = NBTUtil.getInteger(iStack, "air");
            currentAir = currentAir * volume / oldVolume;
            NBTUtil.setInteger(iStack, "air", currentAir);
        }
        NBTUtil.setInteger(iStack, "volume", volume);
        return (float)NBTUtil.getInteger(iStack, "air") / volume;
    }

    public boolean hasSufficientPressure(ItemStack iStack){
        return getPressure(iStack) > 0F;
    }

    @Override
    public float maxPressure(ItemStack iStack){
        return 10F;
    }

    @Override
    public void addAir(ItemStack iStack, int amount){
        int oldAir = NBTUtil.getInteger(iStack, "air");
        NBTUtil.setInteger(iStack, "air", Math.max(oldAir + amount, 0));
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.PNEUMATIC_HELMET;
    }

    /**
     * Override this method to have an item handle its own armor rendering.
     * 
     * @param  entityLiving  The entity wearing the armor 
     * @param  itemStack  The itemStack to render the model of 
     * @param  armorSlot  0=head, 1=torso, 2=legs, 3=feet
     * 
     * @return  A ModelBiped to render instead of the default
     */
    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot){
        if(armorSlot == 0 && (Config.useHelmetModel || DateEventHandler.isIronManEvent())) {
            RenderItemPneumaticHelmet.INSTANCE.render(entityLiving);

            RenderPlayer render = (RenderPlayer)RenderManager.instance.entityRenderMap.get(EntityPlayer.class);
            ModelBiped model = armorSlot == 2 ? render.modelArmor : render.modelArmorChestplate;
            model.bipedHead.showModel = false;
            return model;
        }
        return null;
    }

    private boolean hasThaumcraftUpgradeAndPressure(ItemStack stack){
        return hasSufficientPressure(stack) && getUpgrades(ItemMachineUpgrade.UPGRADE_THAUMCRAFT, stack) > 0;
    }

    @Override
    @Optional.Method(modid = ModIds.THAUMCRAFT)
    public int getVisDiscount(ItemStack stack, EntityPlayer player, Aspect aspect){
        return hasThaumcraftUpgradeAndPressure(stack) ? 5 : 0;
    }

    @Override
    public boolean showIngamePopups(ItemStack itemstack, EntityLivingBase player){
        return hasThaumcraftUpgradeAndPressure(itemstack);
    }

    @Override
    public boolean showNodes(ItemStack itemstack, EntityLivingBase player){
        return hasThaumcraftUpgradeAndPressure(itemstack);
    }

    /**
     * Called to tick armor in the armor slot. Override to do something
     *
     * @param world
     * @param player
     * @param itemStack
     */
    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack iStack){
        super.onArmorTick(world, player, iStack);
        if(!world.isRemote && NBTUtil.hasTag(iStack, "Inventory") && iStack.getTagCompound().getTag("Inventory") instanceof NBTTagCompound) {
            Log.info("Converting 'Inventory' tag to 'UpgradeInventory' in Pneumatic items");
            iStack.getTagCompound().setTag("UpgradeInventory", iStack.getTagCompound().getTag("Inventory"));
            iStack.getTagCompound().removeTag("Inventory");
        }
    }

}
