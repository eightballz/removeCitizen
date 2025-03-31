package me.eightballcreates.citizensRemove;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class CitizensRemove extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("removecitizen").setExecutor(new RemoveCitizenCommand());
    }

    private class RemoveCitizenCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!sender.hasPermission("citizensremove.remove")) {
                sendMessage(sender, ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }

            // validation
            if (args.length != 2) {
                sendMessage(sender, ChatColor.RED + "Usage: /removecitizen [citizen type] [citizen owner]");
                return true;
            }

            final EntityType entityType;
            try {
                entityType = EntityType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                sendMessage(sender, ChatColor.RED + "Invalid entity type: " + args[0]);
                return true;
            }

            final String targetOwner = args[1];
            final NPCRegistry registry = CitizensAPI.getNPCRegistry();

            // null checks (i shoulda used kotlin bro)
            if (registry == null) {
                sendMessage(sender, ChatColor.RED + "Could not access Citizens NPC registry.");
                return true;
            }

            int removed = 0;
            final List<NPC> toRemove = new ArrayList<>();

            for (NPC npc : registry) {
                if (isValidNPC(npc, entityType, targetOwner)) {
                    toRemove.add(npc);
                }
            }

            for (NPC npc : toRemove) {
                npc.destroy();
                removed++;
            }

            sendMessage(sender, ChatColor.GREEN + String.format(
                    "Removed %d %s NPCs owned by %s",
                    removed,
                    entityType.name().toLowerCase(),
                    targetOwner
            ));
            return true;
        }

        private boolean isValidNPC(NPC npc, EntityType type, String owner) {
            return npc.getEntity() != null
                    && npc.getEntity().getType() == type
                    && npc.hasTrait(Owner.class)
                    && npc.getTrait(Owner.class).getOwner().equalsIgnoreCase(owner);
        }

        private void sendMessage(CommandSender sender, String message) {
            sender.sendMessage(message);
        }
    }
}