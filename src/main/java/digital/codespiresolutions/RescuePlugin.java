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

public class RescuePlugin extends JavaPlugin implements CommandExecutor {

    private File rescueFile;
    private FileConfiguration rescueConfig;

    @Override
    public void onEnable() {
        setupRescueFile();
        getCommand("rescue").setExecutor(this);
        getLogger().info("RescuePlugin enabled!");
    }

    private void setupRescueFile() {
        rescueFile = new File(getDataFolder(), "rescue_location.yml");

        if (!rescueFile.exists()) {
            rescueFile.getParentFile().mkdirs();
            try {
                rescueFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create rescue_location.yml!");
            }
        }

        rescueConfig = YamlConfiguration.loadConfiguration(rescueFile);
    }

    private void saveRescueConfig() {
        try {
            rescueConfig.save(rescueFile);
        } catch (IOException e) {
            getLogger().severe("Could not save rescue_location.yml!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // /rescue setup
        if (args.length == 1 && args[0].equalsIgnoreCase("setup")) {

            Location loc = player.getLocation();

            rescueConfig.set("world", loc.getWorld().getName());
            rescueConfig.set("x", loc.getX());
            rescueConfig.set("y", loc.getY());
            rescueConfig.set("z", loc.getZ());
            saveRescueConfig();

            player.sendMessage(ChatColor.GREEN + "Rescue point saved!");
            return true;
        }

        // /rescue
        if (args.length == 0) {

            if (!rescueConfig.contains("x")) {
                player.sendMessage(ChatColor.RED + "No rescue point set! Use /rescue setup first.");
                return true;
            }

            if (!player.getInventory().contains(Material.DIAMOND, 32)) {
                player.sendMessage(ChatColor.RED + "You need 32 diamonds to use rescue!");
                return true;
            }

            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 32));

            World world = Bukkit.getWorld(rescueConfig.getString("world"));
            double x = rescueConfig.getDouble("x");
            double y = rescueConfig.getDouble("y");
            double z = rescueConfig.getDouble("z");

            Location rescueLoc = new Location(world, x, y, z);

            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            player.setFallDistance(0);
            player.setFireTicks(0);
            player.setHealth(player.getMaxHealth());

            player.teleport(rescueLoc);

            player.sendMessage(ChatColor.AQUA + "You have been rescued!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

            return true;
        }

        player.sendMessage(ChatColor.RED + "Usage: /rescue [setup]");
        return true;
    }
}