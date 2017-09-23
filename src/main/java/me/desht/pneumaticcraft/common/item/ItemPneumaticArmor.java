package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.RenderCoordWireframe;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.DateEventHandler;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

//TODO 1.8 Thaumcraft dep @Optional.InterfaceList({@Interface(iface = "thaumcraft.api.IRepairable", modid = ModIds.THAUMCRAFT), @Interface(iface = "thaumcraft.api.IGoggles", modid = ModIds.THAUMCRAFT), @Interface(iface = "thaumcraft.api.IVisDiscountGear", modid = ModIds.THAUMCRAFT), @Interface(iface = "thaumcraft.api.nodes.IRevealer", modid = ModIds.THAUMCRAFT)})
public class ItemPneumaticArmor extends ItemArmor implements IPressurizable, IChargingStationGUIHolderItem,
        IUpgradeAcceptor/*,
IRepairable, IRevealer, IGoggles, IVisDiscountGear*/ {

    public ItemPneumaticArmor(ItemArmor.ArmorMaterial material, int renderIndex, EntityEquipmentSlot armorType, int maxAir) {
        super(material, renderIndex, armorType);
        // TODO other armor types?
        setRegistryName("pneumatic_helmet");
        setUnlocalizedName("pneumatic_helmet");
        setMaxDamage(maxAir);
        setCreativeTab(PneumaticCraftRepressurized.tabPneumaticCraft);
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return Textures.ARMOR_PNEUMATIC + "_1.png";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (isInCreativeTab(tab)) {
            subItems.add(new ItemStack(this));
            ItemStack chargedStack = new ItemStack(this);
            addAir(chargedStack, PneumaticValues.PNEUMATIC_HELMET_VOLUME * 10);
            subItems.add(chargedStack);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack iStack, World world, List<String> textList, ITooltipFlag flag) {
        float pressure = getPressure(iStack);
        textList.add((pressure < 0.5F ? TextFormatting.RED : TextFormatting.DARK_GREEN) + "Pressure: " + Math.round(pressure * 10D) / 10D + " bar");
        ItemStack[] inventoryStacks = getUpgradeStacks(iStack);
        boolean isArmorEmpty = true;
        for (ItemStack stack : inventoryStacks) {
            if (!stack.isEmpty()) {
                isArmorEmpty = false;
                break;
            }
        }
        if (isArmorEmpty) {
            textList.add("Insert in Charging Station to install upgrades");
        } else {
            textList.add("Upgrades installed:");
            PneumaticCraftUtils.sortCombineItemStacksAndToString(textList, inventoryStacks);
            ItemStack searchedStack = getSearchedStack(iStack);
            if (!searchedStack.isEmpty()) {
                for (int i = 0; i < textList.size(); i++) {
                    if (textList.get(i).contains("Item Search")) {
                        textList.set(i, textList.get(i) + " (searching " + searchedStack.getDisplayName() + ")");
                        break;
                    }
                }
            }
            RenderCoordWireframe coordHandler = getCoordTrackLocation(iStack);
            if (coordHandler != null) {
                for (int i = 0; i < textList.size(); i++) {
                    if (textList.get(i).contains("Coordinate Tracker")) {
                        textList.set(i, textList.get(i) + " (tracking " + coordHandler.pos.getX() + ", " + coordHandler.pos.getY() + ", " + coordHandler.pos.getZ() + " in " + coordHandler.world.provider.getDimensionType() + ")");
                        break;
                    }
                }
            }
        }
        ItemPneumatic.addTooltip(iStack, world, textList);
    }

    /**
     * Retrieves the upgrades currently installed on the given armor stack.
     */
    public static ItemStack[] getUpgradeStacks(ItemStack iStack) {
        NBTTagCompound tag = NBTUtil.getCompoundTag(iStack, "UpgradeInventory");
        ItemStack[] inventoryStacks = new ItemStack[9];
        Arrays.fill(inventoryStacks, ItemStack.EMPTY);
        NBTTagList itemList = tag.getTagList("Items", 10);
        for (int i = 0; i < itemList.tagCount(); i++) {
            NBTTagCompound slotEntry = itemList.getCompoundTagAt(i);
            int j = slotEntry.getByte("Slot");
            if (j >= 0 && j < 9) {
                inventoryStacks[j] = new ItemStack(slotEntry);
            }
        }
        return inventoryStacks;
    }

    public static int getUpgrades(EnumUpgrade upgrade, ItemStack stack) {
        return getUpgrades(Itemss.upgrades.get(upgrade), stack);
    }

    public static int getUpgrades(Item upgrade, ItemStack iStack) {
        int upgrades = 0;
        ItemStack[] stacks = getUpgradeStacks(iStack);
        for (ItemStack stack : stacks) {
            if (stack.getItem() == upgrade) {
                upgrades += stack.getCount();
            }
        }
        return upgrades;
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    public static ItemStack getSearchedStack() {
        return getSearchedStack(PneumaticCraftRepressurized.proxy.getPlayer().getItemStackFromSlot(EntityEquipmentSlot.HEAD));
    }

    @Nonnull
    public static ItemStack getSearchedStack(ItemStack helmetStack) {
        if (helmetStack.isEmpty() || !NBTUtil.hasTag(helmetStack, "SearchStack")) return ItemStack.EMPTY;
        NBTTagCompound tag = NBTUtil.getCompoundTag(helmetStack, "SearchStack");
        if (tag.getInteger("itemID") == -1) return ItemStack.EMPTY;
        return new ItemStack(Item.getItemById(tag.getInteger("itemID")), 1, tag.getInteger("itemDamage"));
    }

    @SideOnly(Side.CLIENT)
    public static RenderCoordWireframe getCoordTrackLocation(ItemStack helmetStack) {
        if (helmetStack.isEmpty() || !NBTUtil.hasTag(helmetStack, "CoordTracker")) return null;
        NBTTagCompound tag = NBTUtil.getCompoundTag(helmetStack, "CoordTracker");
        if (tag.getInteger("y") == -1 || FMLClientHandler.instance().getClient().world.provider.getDimension() != tag.getInteger("dimID"))
            return null;
        return new RenderCoordWireframe(FMLClientHandler.instance().getClient().world, NBTUtil.getPos(tag));
    }

    @SideOnly(Side.CLIENT)
    public static String getEntityFilter(ItemStack helmetStack) {
        if (helmetStack.isEmpty() || !NBTUtil.hasTag(helmetStack, "entityFilter")) return "";
        return NBTUtil.getString(helmetStack, "entityFilter");
    }

    public static void setEntityFilter(ItemStack helmetStack, String filter) {
        if (!helmetStack.isEmpty()) {
            NBTUtil.setString(helmetStack, "entityFilter", filter);
        }
    }

    @Override
    public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
        return false;
    }

    @Override
    public float getPressure(ItemStack iStack) {
        int volume = ItemPneumaticArmor.getUpgrades(EnumUpgrade.VOLUME, iStack) * PneumaticValues.VOLUME_VOLUME_UPGRADE + PneumaticValues.PNEUMATIC_HELMET_VOLUME;
        int oldVolume = NBTUtil.getInteger(iStack, "volume");
        if (volume < oldVolume) {
            int currentAir = NBTUtil.getInteger(iStack, "air");
            currentAir = currentAir * volume / oldVolume;
            NBTUtil.setInteger(iStack, "air", currentAir);
        }
        NBTUtil.setInteger(iStack, "volume", volume);
        return (float) NBTUtil.getInteger(iStack, "air") / volume;
    }

    public boolean hasSufficientPressure(ItemStack iStack) {
        return getPressure(iStack) > 0F;
    }

    @Override
    public float maxPressure(ItemStack iStack) {
        return 10F;
    }

    @Override
    public void addAir(ItemStack iStack, int amount) {
        int oldAir = NBTUtil.getInteger(iStack, "air");
        NBTUtil.setInteger(iStack, "air", Math.max(oldAir + amount, 0));
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.PNEUMATIC_HELMET;
    }

    /**
     * Override this method to have an item handle its own armor rendering.
     *
     * @param entityLiving The entity wearing the armor
     * @param itemStack    The itemStack to render the model of
     * @param armorSlot    0=head, 1=torso, 2=legs, 3=feet
     * @return A ModelBiped to render instead of the default
     */

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        if (armorSlot == EntityEquipmentSlot.HEAD && (ConfigHandler.general.useHelmetModel || DateEventHandler.isIronManEvent())) {
            /*RenderItemPneumaticHelmet.INSTANCE.render(entityLiving);

            RenderPlayer render = (RenderPlayer)Minecraft.getMinecraft().getRenderManager().entityRenderMap.get(EntityPlayer.class);
            ModelBiped model = armorSlot == 2 ? render.modelArmor : render.modelArmorChestplate;
            model.bipedHead.showModel = false;
            return model;*///TODO 1.8 fix
        }
        return null;
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        Set<Item> items = new HashSet<>();
        for (IUpgradeRenderHandler handler : UpgradeRenderHandlerList.instance().upgradeRenderers) {
            Collections.addAll(items, handler.getRequiredUpgrades());
        }
        return items;
    }

    @Override
    public String getName() {
        return getUnlocalizedName() + ".name";
    }

    /*private boolean hasThaumcraftUpgradeAndPressure(ItemStack stack){
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
      }*/

}
