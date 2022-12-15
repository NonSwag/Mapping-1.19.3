package net.nonswag.tnl.mappings.v1_19_R3.api.helper;

import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;
import net.nonswag.tnl.listener.api.plugin.PluginHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;

@MethodsReturnNonnullByDefault
public class NMSPluginHelper extends PluginHelper {

    @Override
    public SimpleCommandMap getCommandMap() {
        return ((CraftServer) Bukkit.getServer()).getServer().server.getCommandMap();
    }
}
