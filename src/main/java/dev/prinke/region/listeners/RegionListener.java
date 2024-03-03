package dev.prinke.region.listeners;

import dev.prinke.region.Region;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

public class RegionListener implements Listener {

    Region plugin;
    ArrayList<Location> regionLocations = new ArrayList<>();

    public RegionListener(Region plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getPlayer().hasPermission("region.bypass")) return;
        if (e.getClickedBlock() != null) {
            for (Document region : plugin.getRegions()) {
                Location first = new Location(e.getPlayer().getWorld(),
                        region.get("firstPoint", Document.class).get("x", Number.class).doubleValue(),
                        region.get("firstPoint", Document.class).get("y", Number.class).doubleValue(),
                        region.get("firstPoint", Document.class).get("z", Number.class).doubleValue());
                Location second = new Location(e.getPlayer().getWorld(),
                        region.get("secondPoint", Document.class).get("x", Number.class).doubleValue(),
                        region.get("secondPoint", Document.class).get("y", Number.class).doubleValue(),
                        region.get("secondPoint", Document.class).get("z", Number.class).doubleValue());

                if (isInsideRegion(e.getClickedBlock().getLocation(), first, second)) {
                    if (!plugin.getRegions().isEmpty()) {
                        if (region.get("whitelist") != null) {
                            ArrayList<String> whitelist = (ArrayList<String>) region.get("whitelist");
                            if (whitelist.contains(e.getPlayer().getUniqueId().toString())) {
                                return;
                            } else {
                                e.setCancelled(true);
                                e.getPlayer().sendMessage("§cYou don't have permission to interact with blocks in this region!");
                            }
                        } else {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage("§cYou don't have permission to interact with blocks in this region!");
                        }
                    }
                }
            }
        }
    }

    /*@EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getPlayer().hasPermission("region.bypass")) return;
        for (Document region : plugin.getRegions()) {
            Location first = new Location(e.getPlayer().getWorld(),
                    region.get("firstPoint", Document.class).get("x", Number.class).doubleValue(),
                    region.get("firstPoint", Document.class).get("y", Number.class).doubleValue(),
                    region.get("firstPoint", Document.class).get("z", Number.class).doubleValue());
            Location second = new Location(e.getPlayer().getWorld(),
                    region.get("secondPoint", Document.class).get("x", Number.class).doubleValue(),
                    region.get("secondPoint", Document.class).get("y", Number.class).doubleValue(),
                    region.get("secondPoint", Document.class).get("z", Number.class).doubleValue());

            if (isInsideRegion(e.getBlock().getLocation(), first, second)) {
                if (!plugin.getRegions().isEmpty()) {
                    if (region.get("whitelist") != null) {
                        ArrayList<String> whitelist = (ArrayList<String>) region.get("whitelist");
                        if (whitelist.contains(e.getPlayer().getUniqueId().toString())) {
                            return;
                        } else {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage("§cYou don't have permission to break blocks in this region!");
                        }
                    } else {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage("§cYou don't have permission to break blocks in this region!");
                    }
                }
            }
        }
    }
     */

    /*@EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getPlayer().hasPermission("region.bypass")) return;
        for (Document region : plugin.getRegions()) {
            Location first = new Location(e.getPlayer().getWorld(),
                    region.get("firstPoint", Document.class).get("x", Number.class).doubleValue(),
                    region.get("firstPoint", Document.class).get("y", Number.class).doubleValue(),
                    region.get("firstPoint", Document.class).get("z", Number.class).doubleValue());
            Location second = new Location(e.getPlayer().getWorld(),
                    region.get("secondPoint", Document.class).get("x", Number.class).doubleValue(),
                    region.get("secondPoint", Document.class).get("y", Number.class).doubleValue(),
                    region.get("secondPoint", Document.class).get("z", Number.class).doubleValue());

            if (isInsideRegion(e.getBlock().getLocation(), first, second)) {
                if (!plugin.getRegions().isEmpty()) {
                    if (region.get("whitelist") != null) {
                        ArrayList<String> whitelist = (ArrayList<String>) region.get("whitelist");
                        if (whitelist.contains(e.getPlayer().getUniqueId().toString())) {
                            return;
                        } else {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage("§cYou don't have permission to place blocks in this region!");
                        }
                    } else {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage("§cYou don't have permission to place blocks in this region!");
                    }
                }
            }
        }
    }
     */

    public static boolean isInsideRegion(Location location, Location firstPoint, Location secondPoint) {
        return location.getBlockX() >= Math.min(firstPoint.getBlockX(), secondPoint.getBlockX()) && location.getBlockX() <= Math.max(firstPoint.getBlockX(), secondPoint.getBlockX()) &&
                location.getBlockY() >= Math.min(firstPoint.getBlockY(), secondPoint.getBlockY()) && location.getBlockY() <= Math.max(firstPoint.getBlockY(), secondPoint.getBlockY()) &&
                location.getBlockZ() >= Math.min(firstPoint.getBlockZ(), secondPoint.getBlockZ()) && location.getBlockZ() <= Math.max(firstPoint.getBlockZ(), secondPoint.getBlockZ());
    }
}
