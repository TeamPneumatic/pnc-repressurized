package pneumaticCraft.client.semiblock;

import pneumaticCraft.common.semiblock.ISemiBlock;

public interface ISemiBlockRenderer<SemiBlock extends ISemiBlock> {
    public void render(SemiBlock semiBlock, float partialTick);
}
