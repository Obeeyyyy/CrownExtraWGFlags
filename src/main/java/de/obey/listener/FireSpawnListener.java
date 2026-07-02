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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FireSpawnListener implements Listener {

    private final Set<Long> recentAnchorChunks = ConcurrentHashMap.newKeySet();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAnchorExplode(final BlockExplodeEvent event) {
        if (event.getBlock().getType() != Material.RESPAWN_ANCHOR) return;

        final Location loc = event.getBlock().getLocation();
        final long key = chunkKey(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
        recentAnchorChunks.add(key);

        Bukkit.getScheduler().runTask(CrownExtraWGFlags.getInstance(), () -> recentAnchorChunks.remove(key));
    }

    @EventHandler(ignoreCancelled = true)
    public void onIgnite(final BlockIgniteEvent event) {
        if (event.getCause() != BlockIgniteEvent.IgniteCause.EXPLOSION) return;

        final Block block = event.getBlock();
        final long key = chunkKey(block.getX() >> 4, block.getZ() >> 4);

        if (recentAnchorChunks.contains(key))
            event.setCancelled(true);
    }

    private long chunkKey(final int chunkX, final int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }
}
