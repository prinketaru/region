package dev.prinke.region.commands;

import dev.prinke.region.Region;
import dev.prinke.region.commands.subcommands.*;
import dev.prinke.region.listeners.RegionGuiListener;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {

    Region plugin;
    private final ArrayList<SubCommand> subCommands = new ArrayList<>();

    public CommandManager(Region plugin) {
        this.plugin = plugin;
        subCommands.add(new CreateCommand(plugin));
        subCommands.add(new WandCommand());
        subCommands.add(new AddCommand(plugin));
        subCommands.add(new RemoveCommand(plugin));
        subCommands.add(new WhitelistCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players can use this command!");
            return true;
        }

        if (strings.length > 0) {
            for (int i = 0; i < getSubCommands().size(); i++) {
                if (strings[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
                    getSubCommands().get(i).perform((Player) commandSender, strings);
                } else {
                    // check if a region with the name exists
                    for (Document region : plugin.getRegions()) {
                        if (region.getString("name").equalsIgnoreCase(strings[0])) {
                            // show region menu
                            RegionGuiListener.openRegionMenu(region, (Player) commandSender, plugin);
                            return true;
                        }
                    }
                }
            }
        } else {
            // show regions menu
            Inventory regionsInv = plugin.getServer().createInventory(null, 27, "Regions");

            for (Document region : plugin.getRegions()) {
                ItemStack regionItem = new ItemStack(Material.GRASS_BLOCK);
                ItemMeta regionMeta = regionItem.getItemMeta();
                regionMeta.setDisplayName(region.getString("name"));
                regionItem.setItemMeta(regionMeta);

                regionsInv.addItem(regionItem);
            }
            ((Player) commandSender).openInventory(regionsInv);
        }

        return true;
    }

    // get subcommands
    public ArrayList<SubCommand> getSubCommands() {
        return subCommands;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        ArrayList<String> subcommands = new ArrayList<>();
        if (strings.length == 1) {
            // autocomplete subcommands
            for (int i = 0; i < getSubCommands().size(); i++) {
                subcommands.add(getSubCommands().get(i).getName());
            }
        } else if (strings[0].equalsIgnoreCase("add") || strings[0].equalsIgnoreCase("remove")) {
            // auto complete usernames
            if (strings.length == 2) {
                for (int i = 0; i < plugin.getRegions().size(); i++) {
                    subcommands.add(plugin.getRegions().get(i).getString("name"));
                }
            }
        } else if (strings.length == 2) {
            // auto complete region names
            for (int i = 0; i < plugin.getRegions().size(); i++) {
                if (plugin.getRegions().get(i).getString("owner").equalsIgnoreCase(commandSender.getName())) {
                    subcommands.add(plugin.getRegions().get(i).getString("name"));
                }
            }
        }
        return subcommands;
    }
}
