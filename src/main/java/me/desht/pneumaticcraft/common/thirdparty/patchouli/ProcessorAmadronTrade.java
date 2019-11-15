package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;

public class ProcessorAmadronTrade /*implements IComponentProcessor*/ {
    private AmadronOffer offer = null;
    private String text = null;

//    @Override
//    public void setup(IVariableProvider<String> iVariableProvider) {
//        // TODO: only item->item trades supported right now
//
//        ItemStack result = PatchouliAPI.instance.deserializeItemStack(iVariableProvider.get("item"));
//        for (AmadronOffer offer : AmadronOfferManager.getInstance().getAllOffers()) {
//            if (offer.getInput() instanceof ItemStack && offer.getOutput() instanceof ItemStack) {
//                ItemStack outStack = (ItemStack)offer.getOutput();
//                if (ItemStack.areItemsEqual(result, outStack)) {
//                    this.offer = offer;
//                    break;
//                }
//            }
//        }
//
//        text = iVariableProvider.has("text") ? iVariableProvider.get("text") : null;
//    }
//
//    @Override
//    public String process(String key) {
//        if (offer == null) return null;
//
//        switch (key) {
//            case "input":
//                return ItemStackUtil.serializeStack((ItemStack) offer.getInput());
//            case "output":
//                return ItemStackUtil.serializeStack((ItemStack) offer.getOutput());
//            case "name":
//                return ((ItemStack) offer.getOutput()).getDisplayName();
//            case "text":
//                return text == null ? null : I18n.format(text);
//        }
//
//        return null;
//    }
}
