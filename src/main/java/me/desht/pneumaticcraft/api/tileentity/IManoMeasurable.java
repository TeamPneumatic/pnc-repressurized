package me.desht.pneumaticcraft.api.tileentity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public interface IManoMeasurable {
    /**
     * This method is invoked by the Manometer when a player right-clicks a TE or Entity with this interface implemented.
     *
     * @param player  player who is right-clicking the measurable TE, and therefore needs to get the message
     * @param curInfo list you can append info to. If you don't append any info no air will be used.
     */
    void printManometerMessage(PlayerEntity player, List<ITextComponent> curInfo);
}
