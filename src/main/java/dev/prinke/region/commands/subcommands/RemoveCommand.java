package dev.prinke.region.commands.subcommands;

import dev.prinke.region.Region;
import dev.prinke.region.commands.SubCommand;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class RemoveCommand extends SubCommand {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Remove a player from the whitelist";
    }

    @Override
    public String getSyntax() {
        return "/region remove <region> <player>";
    }

    Region plugin;

    public RemoveCommand(Region plugin) {
        this.plugin = plugin;
    }

    @Override
    public void perform(Player p, String[] args) {
        if (p.hasPermission("region.remove")) {
            if (args.length == 3) {

                ArrayList<Document> regions = plugin.getRegions();
                Boolean regionFound = false;

                for (Document region : regions) {
                    if (region.getString("name").equalsIgnoreCase(args[1])) {
                        regionFound = true;

                        // check if the player is the owner
                        if (!region.getString("owner").equalsIgnoreCase(p.getUniqueId().toString())) {
                            p.sendMessage("§cYou are not the owner of this region!");
                            return;
                        }

                        ArrayList<String> whitelistStrings = (ArrayList<String>) region.get("whitelist");
                        ArrayList<UUID> whitelistUUIDs = new ArrayList<>();
                        for (String uuidStr : whitelistStrings) {
                            whitelistUUIDs.add(UUID.fromString(uuidStr));
                        }
                        Player target = plugin.getServer().getPlayer(args[2]);

                        // check if the player exists
                        if (target == null) {
                            p.sendMessage("§cCould not find the player " + args[2] + "!");
                            return;
                        }

                        Bukkit.getLogger().info("DEBUG WHITELIST ARRAY:" + whitelistUUIDs.toString());
                        // check if the player is already in the whitelist
                        if (!(whitelistUUIDs.contains(target.getUniqueId()))) {
                            p.sendMessage("§c" + target.getDisplayName() + " is not in the region's whitelist!");
                            return;
                        }

                        whitelistUUIDs.remove(target.getUniqueId());
                        region.put("whitelist", whitelistUUIDs);

                        // get locations
                        Location firstPoint = new Location(p.getWorld(),
                                region.get("firstPoint", Document.class).get("x", Number.class).doubleValue(),
                                region.get("firstPoint", Document.class).get("y", Number.class).doubleValue(),
                                region.get("firstPoint", Document.class).get("z", Number.class).doubleValue());
                        Location secondPoint = new Location(p.getWorld(),
                                region.get("secondPoint", Document.class).get("x", Number.class).doubleValue(),
                                region.get("secondPoint", Document.class).get("y", Number.class).doubleValue(),
                                region.get("secondPoint", Document.class).get("z", Number.class).doubleValue());;

                        plugin.modifyRegion(region.getString("name"), region.getString("name"), UUID.fromString(region.getString("owner")), firstPoint, secondPoint, whitelistUUIDs);
                        p.sendMessage("§a" + target.getName() + " has been removed from the region's whitelist!");
                        return;
                    }
                }

                if (!regionFound) {
                    p.sendMessage("§cCould not find a region with the name " + args[1] + "!");
                }

            } else {
                p.sendMessage("§cInvalid syntax! Usage: " + getSyntax());
            }
        } else {
            p.sendMessage("§cYou don't have permission to use this command!");
        }
    }
}
