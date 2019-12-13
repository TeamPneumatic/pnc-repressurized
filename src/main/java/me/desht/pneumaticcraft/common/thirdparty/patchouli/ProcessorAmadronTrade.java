package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer.TradeResource.Type;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariableProvider;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.common.util.ItemStackUtil;

public class ProcessorAmadronTrade implements IComponentProcessor {
    private AmadronOffer offer = null;
    private String text = null;

    @Override
    public void setup(IVariableProvider<String> iVariableProvider) {
        // TODO: only item->item trades supported right now

        ItemStack result = PatchouliAPI.instance.deserializeItemStack(iVariableProvider.get("item"));
        for (AmadronOffer offer : AmadronOfferManager.getInstance().getAllOffers()) {
            if (offer.getInput().getType() == Type.ITEM && offer.getOutput().getType() == Type.ITEM) {
                ItemStack outStack = offer.getOutput().getItem();
                if (ItemStack.areItemsEqual(result, outStack)) {
                    this.offer = offer;
                    break;
                }
            }
        }

        text = iVariableProvider.has("text") ? iVariableProvider.get("text") : null;
    }

    @Override
    public String process(String key) {
        if (offer == null) return null;

        switch (key) {
            case "input":
                return ItemStackUtil.serializeStack(offer.getInput().getItem());
            case "output":
                return ItemStackUtil.serializeStack(offer.getOutput().getItem());
            case "name":
                return offer.getOutput().getItem().getDisplayName().getFormattedText();
            case "text":
                return text == null ? null : I18n.format(text);
        }

        return null;
    }
}
