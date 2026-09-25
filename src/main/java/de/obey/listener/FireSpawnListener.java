package de.obey.listener;


/*
    Author: Obey
    Date: 15.05.2026
    Time: 14:42
    Project: BlockDestuctionDrops
*/

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.obey.CrownExtraWGFlags;
import de.obey.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FireSpawnListener implements Listener {
    private final double MAX_DISTANCE_SQUARED = 12 * 12;
    private final Set<Location> recentAnchorExplosions = ConcurrentHashMap.newKeySet();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAnchorInteract(final PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.useInteractedBlock() == Event.Result.DENY) return;

        final Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.RESPAWN_ANCHOR) return;

        final RespawnAnchor data = (RespawnAnchor) block.getBlockData();
        if (data.getCharges() <= 0) return;

        registerAnchor(block.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAnchorExplode(final BlockExplodeEvent event) {
        if (event.getBlock().getType() != Material.RESPAWN_ANCHOR) return;
        registerAnchor(event.getBlock().getLocation());
    }

    private void registerAnchor(final Location loc) {
        recentAnchorExplosions.add(loc);
        SchedulerUtil.runAsyncDelayed(CrownExtraWGFlags.getInstance(),
                () -> recentAnchorExplosions.remove(loc), 100L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onIgnite(final BlockIgniteEvent event) {
        if (CrownExtraWGFlags.ANCHOR_FIRE_SPAWN == null) return;

        final Block block = event.getBlock();
        final Location igniteLoc = block.getLocation();

        final boolean nearAnchorExplosion = recentAnchorExplosions.stream()
                .anyMatch(anchorLoc -> anchorLoc.getWorld() != null
                        && anchorLoc.getWorld().equals(igniteLoc.getWorld())
                        && anchorLoc.distanceSquared(igniteLoc) <= MAX_DISTANCE_SQUARED);


        if (!nearAnchorExplosion) return;

        final RegionContainer container = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer();

        final RegionQuery query = container.createQuery();
        final com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(igniteLoc);
        final ApplicableRegionSet regionsLoc = query.getApplicableRegions(weLoc);

        final StateFlag.State state = regionsLoc.queryState(null, CrownExtraWGFlags.ANCHOR_FIRE_SPAWN);

        if (state == StateFlag.State.DENY) {
            event.setCancelled(true);
        }
    }
}
