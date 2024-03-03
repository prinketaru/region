package dev.prinke.region.commands.subcommands;

import dev.prinke.region.Region;
import dev.prinke.region.commands.SubCommand;
import dev.prinke.region.listeners.RegionListener;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CreateCommand extends SubCommand {
    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Creates a new region";
    }

    @Override
    public String getSyntax() {
        return "/region create <name>";
    }

    Region plugin;
    HashMap<Player, Location> selectedFirstPoints = new HashMap<>();
    HashMap<Player, Location> selectedSecondPoints = new HashMap<>();

    public CreateCommand(Region plugin) {
        this.plugin = plugin;
        this.selectedFirstPoints = plugin.selectedFirstPoints;
        this.selectedSecondPoints = plugin.selectedSecondPoints;
    }

    @Override
    public void perform(Player p, String[] args) {
        if (args.length == 2) {
            if (p.hasPermission("region.create")) {
                if (selectedFirstPoints.containsKey(p) && selectedSecondPoints.containsKey(p)) {

                    Location first = selectedFirstPoints.get(p);
                    Location second = selectedSecondPoints.get(p);

                    for (Document region : plugin.getRegions()) {
                        // check if a region overlaps with another
                        Location regionFirst = new Location(p.getWorld(),
                                region.get("firstPoint", Document.class).get("x", Number.class).doubleValue(),
                                region.get("firstPoint", Document.class).get("y", Number.class).doubleValue(),
                                region.get("firstPoint", Document.class).get("z", Number.class).doubleValue());
                        Location regionSecond = new Location(p.getWorld(),
                                region.get("secondPoint", Document.class).get("x", Number.class).doubleValue(),
                                region.get("secondPoint", Document.class).get("y", Number.class).doubleValue(),
                                region.get("secondPoint", Document.class).get("z", Number.class).doubleValue());

                        if (RegionListener.isInsideRegion(first, regionFirst, regionSecond) || RegionListener.isInsideRegion(second, regionFirst, regionSecond)) {
                            p.sendMessage("§cThis region overlaps with another region!");
                            return;
                        }

                        // check if another region has the same name as the one being created
                        if (region.getString("name").equalsIgnoreCase(args[1])) {
                            p.sendMessage("§cA region with the name " + args[1] + " already exists!");
                            return;
                        }
                    }

                    String name = args[1];
                    ArrayList<UUID> whitelist = new ArrayList<>();
                    whitelist.add(p.getUniqueId());
                    plugin.storeRegion(name, p.getUniqueId(), first, second, whitelist);
                    p.sendMessage("§aRegion " + name + " has been created!");

                } else {
                    p.sendMessage("§cYou need to select the first and second points of the region first!");
                }
            } else {
                p.sendMessage("§cYou don't have permission to use this command!");
            }
        } else {
            p.sendMessage("§cInvalid syntax! Usage: " + getSyntax());
        }
    }
}
