package net.ch4rlesexe.morph;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MorphManager implements Listener {

    private final MorphPlugin plugin;
    private static final String META_KEY = "rmorph_entity";


    private final Map<UUID, DisguiseInfo> disguises = new ConcurrentHashMap<>();

    public MorphManager(MorphPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isDisguised(Player player) {
        return disguises.containsKey(player.getUniqueId());
    }

    public EntityType getDisguiseType(Player player) {
        DisguiseInfo info = disguises.get(player.getUniqueId());
        return info != null ? info.entityType : null;
    }


    public void disguise(Player target, EntityType entityType) {

        if (isDisguised(target)) {
            undisguise(target);
        }


        Location loc = target.getLocation();
        Entity mob = target.getWorld().spawnEntity(loc, entityType);


        if (mob instanceof LivingEntity livingMob) {
            livingMob.setAI(false);
            livingMob.setInvulnerable(true);
            livingMob.setSilent(true);
            livingMob.setCollidable(false);
            livingMob.setGravity(false);

            if (livingMob instanceof Mob m) {
                m.setAware(false);
            }
            livingMob.setRemoveWhenFarAway(false);
        }
        mob.setPersistent(true);
        mob.setMetadata(META_KEY, new FixedMetadataValue(plugin, target.getUniqueId().toString()));

        DisguiseInfo info = new DisguiseInfo(entityType, mob.getUniqueId(), mob.getEntityId());
        disguises.put(target.getUniqueId(), info);


        for (Player observer : Bukkit.getOnlinePlayers()) {
            if (observer.equals(target)) continue;
            observer.hidePlayer(plugin, target);
        }


        info.taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!target.isOnline()) return;
            DisguiseInfo di = disguises.get(target.getUniqueId());
            if (di == null) return;
            Entity mobEntity = Bukkit.getEntity(di.mobUuid);
            if (mobEntity == null || mobEntity.isDead()) {

                plugin.getLogger().warning("Morph mob for " + target.getName() + " was removed, undisguising.");
                undisguise(target);
                return;
            }

            Location playerLoc = target.getLocation();
            mobEntity.teleport(playerLoc);
        }, 1L, 1L).getTaskId();
    }


    public void undisguise(Player target) {
        DisguiseInfo info = disguises.remove(target.getUniqueId());
        if (info == null) return;


        if (info.taskId != -1) {
            Bukkit.getScheduler().cancelTask(info.taskId);
        }


        Entity mob = Bukkit.getEntity(info.mobUuid);
        if (mob != null) {
            mob.remove();
        }


        for (Player observer : Bukkit.getOnlinePlayers()) {
            if (observer.equals(target)) continue;
            observer.showPlayer(plugin, target);
        }
    }

    public void undisguiseAll() {
        for (UUID uuid : new HashSet<>(disguises.keySet())) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                undisguise(p);
            } else {

                DisguiseInfo info = disguises.remove(uuid);
                if (info != null) {
                    if (info.taskId != -1) Bukkit.getScheduler().cancelTask(info.taskId);
                    Entity mob = Bukkit.getEntity(info.mobUuid);
                    if (mob != null) mob.remove();
                }
            }
        }
    }



    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Map.Entry<UUID, DisguiseInfo> entry : disguises.entrySet()) {
                Player disguised = Bukkit.getPlayer(entry.getKey());
                if (disguised != null && !disguised.equals(joiner)) {
                    joiner.hidePlayer(plugin, disguised);
                }
            }
        }, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player quitter = event.getPlayer();
        if (isDisguised(quitter)) {
            undisguise(quitter);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobDamage(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata(META_KEY)) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getEntity().hasMetadata(META_KEY)) {
            event.setCancelled(true);
        }
        if (event.getTarget() != null && event.getTarget().hasMetadata(META_KEY)) {
            event.setCancelled(true);
        }
    }



    static class DisguiseInfo {
        final EntityType entityType;
        final UUID mobUuid;
        final int mobEntityId;
        int taskId = -1;

        DisguiseInfo(EntityType entityType, UUID mobUuid, int mobEntityId) {
            this.entityType = entityType;
            this.mobUuid = mobUuid;
            this.mobEntityId = mobEntityId;
        }
    }
}
