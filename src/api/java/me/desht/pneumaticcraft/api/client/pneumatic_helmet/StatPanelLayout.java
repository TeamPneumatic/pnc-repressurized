package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Represents a resolution-independent position for an armor HUD stat panel
 *
 * @param x X position, in range 0..1
 * @param y Y position, in range 0..1
 * @param expandsLeft true if panel expands to the left (thus anchored right), false otherwise
 */
public record StatPanelLayout(float x, float y, boolean expandsLeft) {
    public static final StatPanelLayout DEFAULT = new StatPanelLayout(0f, 0.5f, false);

    public static final Codec<StatPanelLayout> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("x").forGetter(StatPanelLayout::x),
            Codec.FLOAT.fieldOf("y").forGetter(StatPanelLayout::y),
            Codec.BOOL.fieldOf("expandsLeft").forGetter(StatPanelLayout::expandsLeft)
    ).apply(instance, StatPanelLayout::new));

}

