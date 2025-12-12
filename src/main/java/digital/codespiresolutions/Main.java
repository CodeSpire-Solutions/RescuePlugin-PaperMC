package digital.codespiresolutions;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin implements CommandExecutor {

    private File rescueFile;
    private FileConfiguration rescueConfig;

    @Override
    public void onEnable() {
        createRescueFile();
        getCommand("rescue").setExecutor(this);
        getLogger().info("RescuePlugin is enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RescuePlugin is disabled!");
    }

    private void createRescueFile() {
        rescueFile = new File(getDataFolder(), "rescue_location.yml");

        if (!rescueFile.exists()) {
            rescueFile.getParentFile().mkdirs();

            try {
                rescueFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Failed to create rescue_location.yml!");
            }
        }

        rescueConfig = YamlConfiguration.loadConfiguration(rescueFile);
    }

    private void saveRescueConfig() {
        try {
            rescueConfig.save(rescueFile);
        } catch (IOException e) {
            getLogger().severe("Failed to save rescue_location.yml!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        Player p = (Player) sender;

        // ----- /rescue setup -----
        if (args.length == 1 && args[0].equalsIgnoreCase("setup")) {

            Location loc = p.getLocation();

            rescueConfig.set("world", loc.getWorld().getName());
            rescueConfig.set("x", loc.getX());
            rescueConfig.set("y", loc.getY());
            rescueConfig.set("z", loc.getZ());
            saveRescueConfig();

            p.sendMessage(ChatColor.GREEN + "Rescue point has been saved!");
            return true;
        }

        // ----- /rescue -----
        if (args.length == 0) {

            if (!rescueConfig.contains("world")) {
                p.sendMessage(ChatColor.RED + "No rescue point is set! Use /rescue setup first.");
                return true;
            }

            // Check if player has 32 diamonds
            if (!p.getInventory().contains(Material.DIAMOND, 32)) {
                p.sendMessage(ChatColor.RED + "You need 32 diamonds to use /rescue.");
                return true;
            }

            // Remove 32 diamonds
            p.getInventory().removeItem(new ItemStack(Material.DIAMOND, 32));

            String worldName = rescueConfig.getString("world");
            World world = Bukkit.getWorld(worldName);

            if (world == null) {
                p.sendMessage(ChatColor.RED + "Saved rescue world does not exist anymore!");
                return true;
            }

            double x = rescueConfig.getDouble("x");
            double y = rescueConfig.getDouble("y");
            double z = rescueConfig.getDouble("z");

            Location rescueLoc = new Location(world, x, y, z);

            // Clear effects
            p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
            p.setFireTicks(0);
            p.setFallDistance(0);
            p.setHealth(p.getMaxHealth());

            // Teleport
            p.teleport(rescueLoc);

            p.sendMessage(ChatColor.AQUA + "You have been rescued!");
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

            return true;
        }

        p.sendMessage(ChatColor.RED + "Usage: /rescue [setup]");
        return true;
    }
}