package dev.prinke.region.commands.subcommands;

import dev.prinke.region.Region;
import dev.prinke.region.commands.SubCommand;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WhitelistCommand extends SubCommand {
    @Override
    public String getName() {
        return "whitelist";
    }

    @Override
    public String getDescription() {
        return "Lists all players whitelisted in a region";
    }

    @Override
    public String getSyntax() {
        return "/region whitelist <region>";
    }

    Region plugin;

    public WhitelistCommand(Region plugin) {
        this.plugin = plugin;
    }

    @Override
    public void perform(Player p, String[] args) {
        if (p.hasPermission("region.whitelist")) {
            if (args.length == 2) {
                Boolean regionFound = false;
                for (Document region : plugin.getRegions()) {
                    if (region.getString("name").equalsIgnoreCase(args[1])) {
                        regionFound = true;

                        // check if the player is the owner
                        if (!region.getString("owner").equalsIgnoreCase(p.getUniqueId().toString())) {
                            p.sendMessage("§cYou are not the owner of this region!");
                            return;
                        }

                        p.sendMessage("§aWhitelisted players in region §6" + region.getString("name") + "§a:");
                        for (String uuid : (Iterable<String>) region.get("whitelist")) {
                            p.sendMessage("§7- §6" + plugin.getServer().getPlayer(UUID.fromString(uuid)).getName());
                        }
                        return;
                    }
                }
                if (!regionFound) {
                    p.sendMessage("§cCould not find a region with the name " + args[1] + "!");
                }
            }
        } else {
            p.sendMessage("§cYou do not have permission to use this command!");
        }
    }
}
