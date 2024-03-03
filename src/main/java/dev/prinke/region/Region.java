package dev.prinke.region;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.prinke.region.commands.CommandManager;
import dev.prinke.region.listeners.RegionGuiListener;
import dev.prinke.region.listeners.RegionListener;
import dev.prinke.region.listeners.WandListener;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Region extends JavaPlugin {

    FileConfiguration config = getConfig();
    MongoClient client;
    MongoDatabase database;
    public HashMap<Player, Location> selectedFirstPoints = new HashMap<>();
    public HashMap<Player, Location> selectedSecondPoints = new HashMap<>();

    @Override
    public void onEnable() {
        // setup config
        config.addDefault("mongoIP", "localhost");
        config.addDefault("mongoPort", 27017);
        config.options().copyDefaults(true);
        saveConfig();

        // attempt to connect to mongodb
        try {
            client = MongoClients.create("mongodb://" + config.getString("mongoIP") + ":" + config.getInt("mongoPort"));
            database = client.getDatabase("region");
            MongoCollection<Document> collection = database.getCollection("regions");
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to connect to MongoDB! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // register commands
        getCommand("region").setExecutor(new CommandManager(this));

        // register listeners
        getServer().getPluginManager().registerEvents(new WandListener(this), this);
        getServer().getPluginManager().registerEvents(new RegionListener(this), this);
        getServer().getPluginManager().registerEvents(new RegionGuiListener(this), this);

        Bukkit.getLogger().info("Region has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        // disconnect from mongodb
        client.close();
        Bukkit.getLogger().info("Region has been successfully disabled!");
    }

    // method to store a region to the db
    public void storeRegion(String name, UUID playerUUID, Location firstPoint, Location secondPoint, ArrayList<UUID> whitelist) {
        // Convert UUIDs in the whitelist to String format
        List<String> whitelistStrings = whitelist.stream().map(UUID::toString).collect(Collectors.toList());

        // Create the document to insert into MongoDB
        Document document = new Document("name", name)
                .append("owner", playerUUID.toString())
                .append("firstPoint", new Document("world", firstPoint.getWorld().getName()).append("x", firstPoint.getBlockX()).append("y", firstPoint.getBlockY()).append("z", firstPoint.getBlockZ()))
                .append("secondPoint", new Document("world", secondPoint.getWorld().getName()).append("x", secondPoint.getBlockX()).append("y", secondPoint.getBlockY()).append("z", secondPoint.getBlockZ()))
                .append("whitelist", whitelistStrings);

        database.getCollection("regions").insertOne(document);
    }


    // method to get all regions
    public ArrayList<Document> getRegions() {
        ArrayList<Document> regions = new ArrayList<>();
        for (Document document : database.getCollection("regions").find()) {
            regions.add(document);
        }
        return regions;
    }

    // method to modify a region
    public void modifyRegion(String name, String newName, UUID playerUUID, Location firstPoint, Location secondPoint, ArrayList<UUID> whitelist) {
        // delete the region
        deleteRegion(name);

        // store the region
        storeRegion(newName, playerUUID, firstPoint, secondPoint, whitelist);
    }

    // method to delete a region
    public void deleteRegion(String name) {
        database.getCollection("regions").deleteOne(new Document("name", name));
    }

}