package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.RenderCoordWireframe;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.DateEventHandler;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.recipes.factories.OneProbeRecipeFactory;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.items.IGoggles;
import thaumcraft.api.items.IRevealer;
import thaumcraft.api.items.IVisDiscountGear;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Optional.InterfaceList({
        @Optional.Interface(iface = "thaumcraft.api.items.IGoggles", modid = ModIds.THAUMCRAFT),
        @Optional.Interface(iface = "thaumcraft.api.items.IVisDiscountGear", modid = ModIds.THAUMCRAFT),
        @Optional.Interface(iface = "thaumcraft.api.items.IRevealer", modid = ModIds.THAUMCRAFT)
})
public class ItemPneumaticHelmet extends ItemPneumaticArmorBase implements IPressurizable, IChargingStationGUIHolderItem, IUpgradeAcceptor,
        IRevealer, IGoggles, IVisDiscountGear {

    public ItemPneumaticHelmet() {
        super("pneumatic_helmet", EntityEquipmentSlot.HEAD);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack iStack, World world, List<String> textList, ITooltipFlag flag) {
        super.addInformation(iStack, world, textList, flag);

        if (iStack.hasTagCompound() && iStack.getTagCompound().getInteger(OneProbeRecipeFactory.ONE_PROBE_TAG) == 1) {
            textList.add(TextFormatting.BLUE + "The One Probe installed");
        }

        if (UpgradableItemUtils.addUpgradeInformation(iStack, world, textList, flag) > 0) {
            // supplementary search & tracker information
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
        if (armorSlot == EntityEquipmentSlot.HEAD && (ConfigHandler.client.useHelmetModel || DateEventHandler.isIronManEvent())) {
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
        items.add(CraftingRegistrator.getUpgrade(EnumUpgrade.SPEED).getItem());
        items.add(CraftingRegistrator.getUpgrade(EnumUpgrade.VOLUME).getItem());
        items.add(CraftingRegistrator.getUpgrade(EnumUpgrade.RANGE).getItem());
        items.add(CraftingRegistrator.getUpgrade(EnumUpgrade.SECURITY).getItem());
        if (Loader.isModLoaded(ModIds.THAUMCRAFT)) {
            items.add(CraftingRegistrator.getUpgrade(EnumUpgrade.THAUMCRAFT).getItem());
        }
        return items;
    }

    @Override
    @Optional.Method(modid = ModIds.THAUMCRAFT)
    public int getVisDiscount(ItemStack stack, EntityPlayer player) {
        return hasThaumcraftUpgradeAndPressure(stack) ? 5 : 0;
    }

    @Override
    @Optional.Method(modid = ModIds.THAUMCRAFT)
    public boolean showIngamePopups(ItemStack itemstack, EntityLivingBase player) {
        return hasThaumcraftUpgradeAndPressure(itemstack);
    }

    @Override
    @Optional.Method(modid = ModIds.THAUMCRAFT)
    public boolean showNodes(ItemStack itemstack, EntityLivingBase player) {
        return hasThaumcraftUpgradeAndPressure(itemstack);
    }

    @Override
    public int getVolume() {
        return PneumaticValues.PNEUMATIC_HELMET_VOLUME;
    }

    @Override
    public int getMaxAir() {
        return PneumaticValues.PNEUMATIC_HELMET_MAX_AIR;
    }
}
