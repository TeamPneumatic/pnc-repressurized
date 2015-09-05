package pneumaticCraft.common.commands;

import net.minecraft.command.ServerCommandManager;

public class PCCommandManager{
    public void init(ServerCommandManager commandManager){
        commandManager.registerCommand(new CommandAmazonDelivery());
        commandManager.registerCommand(new CommandGetGlobalVariable());
        commandManager.registerCommand(new CommandSetGlobalVariable());
    }

}
