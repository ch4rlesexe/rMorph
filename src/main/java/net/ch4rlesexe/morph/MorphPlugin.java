package net.ch4rlesexe.morph;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.PluginCommand;

public final class MorphPlugin extends JavaPlugin {

    private MorphManager morphManager;

    @Override
    public void onEnable() {
        this.morphManager = new MorphManager(this);

        MorphCommand morphCmd = new MorphCommand(this);
        PluginCommand morphPluginCmd = this.getCommand("morph");
        if (morphPluginCmd != null) {
            morphPluginCmd.setExecutor(morphCmd);
            morphPluginCmd.setTabCompleter(morphCmd);
        }

        UnmorphCommand unmorphCmd = new UnmorphCommand(this);
        PluginCommand unmorphPluginCmd = this.getCommand("unmorph");
        if (unmorphPluginCmd != null) {
            unmorphPluginCmd.setExecutor(unmorphCmd);
            unmorphPluginCmd.setTabCompleter(unmorphCmd);
        }

        getServer().getPluginManager().registerEvents(morphManager, this);
        getLogger().info("rMorph enabled (ProtocolLib-based disguises)");
    }

    @Override
    public void onDisable() {
        if (morphManager != null) {
            morphManager.undisguiseAll();
        }
        getLogger().info("rMorph disabled");
    }

    public MorphManager getMorphManager() {
        return morphManager;
    }
}
