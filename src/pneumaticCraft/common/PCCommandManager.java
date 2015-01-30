package pneumaticCraft.common;

import net.minecraft.command.ServerCommandManager;

public class PCCommandManager{
    public void init(ServerCommandManager commandManager){
        commandManager.registerCommand(new CommandAmazonDelivery());
    }

}
