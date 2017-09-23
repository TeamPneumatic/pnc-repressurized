package me.desht.pneumaticcraft.client.semiblock;

import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;

public interface ISemiBlockRenderer<SemiBlock extends ISemiBlock> {
    void render(SemiBlock semiBlock, float partialTick);
}
