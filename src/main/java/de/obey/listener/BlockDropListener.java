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
import org.bukkit.Material;
import org.bukkit.entity.AbstractWindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockDropListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (CrownExtraWGFlags.BLOCK_DROP_FLAG == null)
            return;

        if (CrownExtraWGFlags.isBlockDropExcluded(event.getBlock().getType()))
            return;

        final RegionContainer container = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer();

        final RegionQuery query = container.createQuery();
        final com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(event.getBlock().getLocation());
        final ApplicableRegionSet regions = query.getApplicableRegions(weLoc);
        final StateFlag.State state = regions.queryState(null, CrownExtraWGFlags.BLOCK_DROP_FLAG);

        if (state == StateFlag.State.DENY)
            event.setDropItems(false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(EntityExplodeEvent event) {
        if (event.getEntity() instanceof AbstractWindCharge
                || (event.getEntity() != null && event.getEntityType().name().contains("WIND_CHARGE"))) {
            event.blockList().clear();
            return;
        }

        if (CrownExtraWGFlags.BLOCK_DROP_FLAG == null)
            return;

        final RegionContainer container = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer();

        final RegionQuery query = container.createQuery();

        event.blockList().removeIf(block -> {
            if (CrownExtraWGFlags.isBlockDropExcluded(block.getType())) {
                return false;
            }
            final ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));
            if (regions.queryState(null, CrownExtraWGFlags.BLOCK_DROP_FLAG) == StateFlag.State.DENY) {
                block.setType(Material.AIR);
                return true;
            }
            return false;
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockExplodeEvent event) {
        if (CrownExtraWGFlags.BLOCK_DROP_FLAG == null)
            return;

        final RegionContainer container = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer();

        final RegionQuery query = container.createQuery();

        event.blockList().removeIf(block -> {
            if (CrownExtraWGFlags.isBlockDropExcluded(block.getType())) {
                return false;
            }
            final ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));
            if (regions.queryState(null, CrownExtraWGFlags.BLOCK_DROP_FLAG) == StateFlag.State.DENY) {
                block.setType(Material.AIR);
                return true;
            }
            return false;
        });
    }
}
