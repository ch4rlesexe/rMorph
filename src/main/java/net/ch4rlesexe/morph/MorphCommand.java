package net.ch4rlesexe.morph;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MorphCommand implements CommandExecutor, TabCompleter {
    private final MorphPlugin plugin;


    private static final List<String> MOB_TYPES = Arrays.stream(EntityType.values())
            .filter(EntityType::isAlive)
            .filter(t -> t != EntityType.PLAYER)
            .map(EntityType::name)
            .sorted()
            .collect(Collectors.toList());

    public MorphCommand(MorphPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rmorph.morph")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage("§cUsage: /morph <player> <mob>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid mob type. Examples: ELDER_GUARDIAN, ZOMBIE, CREEPER");
            return true;
        }
        if (!entityType.isAlive() || entityType == EntityType.PLAYER) {
            sender.sendMessage("§cThat is not a valid mob type.");
            return true;
        }

        plugin.getMorphManager().disguise(target, entityType);
        sender.sendMessage("§aMorphed §f" + target.getName() + " §ainto §f" + entityType.name());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("rmorph.morph")) return List.of();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            String prefix = args[1].toUpperCase();
            return MOB_TYPES.stream()
                    .filter(n -> n.startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
