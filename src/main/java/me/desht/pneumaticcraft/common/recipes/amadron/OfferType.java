package me.desht.pneumaticcraft.common.recipes.amadron;

import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum OfferType {
    RECIPE(
            // For recipe-based offers, we only need to write the offer ID - everything else is already sync'd to the
            // client recipe manager via normal vanilla recipe sync
            // Update, nope: this doesn't work reliably for custom trades (datapack/KJS) for some reason
            // Will come back to this later!
//            (buf, offer) -> buf.writeResourceLocation(offer.getOfferId()),
//            buf -> fromHolder(ModRecipeTypes.AMADRON.get().getRecipe(ClientUtils.getClientLevel(), buf.readResourceLocation()))
            (buf, offer) -> offer.write(buf),
            buf -> Optional.of(AmadronOffer.offerFromBuf(buf))
    ),
    VILLAGER(
            (buf, offer) -> offer.write(buf),
            buf -> Optional.of(AmadronOffer.offerFromBuf(buf))
    ),
    PLAYER(
            (buf, offer) -> offer.write(buf),
            buf -> Optional.of(AmadronPlayerOffer.playerOfferFromBuf(buf))
    );

    private final BiConsumer<RegistryFriendlyByteBuf, AmadronOffer> writer;
    private final Function<RegistryFriendlyByteBuf, Optional<AmadronOffer>> reader;

    OfferType(BiConsumer<RegistryFriendlyByteBuf, AmadronOffer> writer, Function<RegistryFriendlyByteBuf, Optional<AmadronOffer>> reader) {
        this.writer = writer;
        this.reader = reader;
    }

    public void write(RegistryFriendlyByteBuf buf, AmadronOffer offer) {
        writer.accept(buf, offer);
    }

    public Optional<AmadronOffer> read(RegistryFriendlyByteBuf buf) {
        return reader.apply(buf);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Optional<AmadronOffer> fromHolder(Optional<RecipeHolder<AmadronRecipe>> holder) {
        return holder.filter(r -> r.value() instanceof AmadronOffer).map(r -> (AmadronOffer) r.value());
    }
}
