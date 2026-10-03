package net.ch4rlesexe.morph;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class UnmorphCommand implements CommandExecutor, TabCompleter {
    private final MorphPlugin plugin;

    public UnmorphCommand(MorphPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rmorph.morph")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage("§cUsage: /unmorph <player>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }
        if (!plugin.getMorphManager().isDisguised(target)) {
            sender.sendMessage("§c" + target.getName() + " is not morphed.");
            return true;
        }
        plugin.getMorphManager().undisguise(target);
        sender.sendMessage("§aUnmorphed " + target.getName() + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("rmorph.morph")) return List.of();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> plugin.getMorphManager().isDisguised(p))
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
