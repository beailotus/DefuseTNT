package beailotus_io.github.defusetnt;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {
    private Material defuseItem;
    private final List<UUID> defusing = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        reloadDefuseItem();
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("defuse"))
                .setExecutor((sender, command, label, args) -> {
                    if(!sender.hasPermission("defuse.admin")){
                        sender.sendMessage("§cYou don't have permission to use this command!");
                        return false;
                    }

                    if (args.length == 0) {
                        sender.sendMessage("§cUsage: /defuse <reload/set-item>");
                        return false;
                    }

                    if(args[0].equalsIgnoreCase("reload")) {
                        reloadDefuseItem();
                        sender.sendMessage("§aReloaded defuse item.");
                        return true;
                    }

                    if(args[0].equalsIgnoreCase("set-item")) {
                        if(!(sender instanceof Player)){
                            sender.sendMessage("§cOnly players can use this command.");
                            return false;
                        }
                        Player player = (Player) sender;
                        getConfig().set("item", player.getInventory().getItemInMainHand().getType().name());
                        saveConfig();
                        reloadDefuseItem();
                        sender.sendMessage("§aSet defuse item to " + player.getInventory().getItemInMainHand().getType().name());
                    }

                    return true;
                });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reloadDefuseItem(){
        this.defuseItem = Material.getMaterial(Objects.requireNonNull(
                getConfig().getString("item")
        ));
    }

    @EventHandler
    public void setDefusing(PlayerInteractAtEntityEvent event){
        if(event.getRightClicked().getType() != EntityType.PRIMED_TNT){
            return;
        }

        if(event.getPlayer().getInventory().getItemInMainHand().getType() != defuseItem &&
                event.getPlayer().getInventory().getItemInOffHand().getType() != defuseItem){
            return;
        }

        UUID tnt = event.getRightClicked().getUniqueId();

        if(defusing.contains(tnt)){
            return;
        }

        defusing.add(tnt);
        event.getPlayer().sendMessage("§aYou have successfully defused the TNT.");
    }

    @EventHandler
    public void defuse(EntityExplodeEvent event){
        if(event.getEntityType() != EntityType.PRIMED_TNT){
            return;
        }

        if(!defusing.contains(event.getEntity().getUniqueId())){
            return;
        }

        defusing.remove(event.getEntity().getUniqueId());

        event.setCancelled(true);
    }
}
