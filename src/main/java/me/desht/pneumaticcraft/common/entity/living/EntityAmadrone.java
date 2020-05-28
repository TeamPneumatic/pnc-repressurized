package me.desht.pneumaticcraft.common.entity.living;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class EntityAmadrone extends EntityDrone {
    public enum AmadronAction { TAKING_PAYMENT, RESTOCKING }

    private ResourceLocation handlingOffer; // ID of an Amadron offer
    private int offerTimes;
    private ItemStack usedTablet = ItemStack.EMPTY;  // Tablet used to place the order.
    private String buyingPlayer;
    private AmadronAction amadronAction;

    public EntityAmadrone(EntityType<? extends EntityDrone> type, World world) {
        super(type, world, null);

        getAirHandler().addAir(100000);
        setCustomName(new TranslationTextComponent("drone.amadronDeliveryDrone"));
    }

    public void setHandlingOffer(ResourceLocation offerId, int times, @Nonnull ItemStack usedTablet, String buyingPlayer, AmadronAction amadronAction) {
        this.handlingOffer = offerId;
        this.offerTimes = times;
        this.usedTablet = usedTablet.copy();
        this.buyingPlayer = buyingPlayer;
        this.amadronAction = amadronAction;
    }

    public ResourceLocation getHandlingOffer() {
        return handlingOffer;
    }

    public AmadronAction getAmadronAction() {
        return amadronAction;
    }

    public int getOfferTimes() {
        return offerTimes;
    }

    public ItemStack getUsedTablet() {
        return usedTablet;
    }

    public String getBuyingPlayer() {
        return buyingPlayer;
    }

    @Override
    public boolean shouldDropAsItem() {
        return false;
    }

    @Override
    protected boolean canDropLoot() {
        return false;
    }

    @Override
    public int getUpgrades(EnumUpgrade upgrade) {
        switch (upgrade) {
            case SECURITY:
                return 1;
            case ITEM_LIFE:
            case SPEED:
                return 10;
            case INVENTORY:
                return 35;
            default:
                return 0;
        }
    }

    @Override
    public void writeAdditional(CompoundNBT tag) {
        super.writeAdditional(tag);

        if (handlingOffer != null) {
            CompoundNBT subTag = new CompoundNBT();
            subTag.putString("offerId", handlingOffer.toString());
            subTag.putInt("offerTimes", offerTimes);
            subTag.putString("buyingPlayer", buyingPlayer);
            if (!usedTablet.isEmpty()) subTag.put("usedTablet", usedTablet.write(new CompoundNBT()));
            subTag.putString("amadronAction", amadronAction.toString());
            tag.put("amadron", subTag);
        }
    }

    @Override
    public void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);

        if (tag.contains("amadron")) {
            CompoundNBT subTag = tag.getCompound("amadron");
            handlingOffer = new ResourceLocation(subTag.getString("offerId"));
            usedTablet = ItemStack.read(subTag.getCompound("usedTablet"));
            offerTimes = subTag.getInt("offerTimes");
            buyingPlayer = subTag.getString("buyingPlayer");
            amadronAction = AmadronAction.valueOf(subTag.getString("amadronAction"));
        }
    }
}
