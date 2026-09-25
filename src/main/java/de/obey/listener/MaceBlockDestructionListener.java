package de.obey.listener;

/*
    Author: Obey
    Date: 17.05.2026
    Time: 11:16
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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MaceBlockDestructionListener implements Listener {

    private final Set<UUID> recentMaceHits = ConcurrentHashMap.newKeySet();

    @EventHandler
    public void onEntityDamage(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        final ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() != Material.MACE) return;

        recentMaceHits.add(player.getUniqueId());

        SchedulerUtil.runGlobalDelayed(CrownExtraWGFlags.getInstance(), () -> recentMaceHits.remove(player.getUniqueId()), 2L);
    }

    @EventHandler
    public void onBlockExplode(final BlockExplodeEvent event) {
        if (CrownExtraWGFlags.MACE_BLOCK_DESTRUCTION == null) return;
        if (event.getBlock().getType() != Material.AIR) return;
        if (recentMaceHits.isEmpty()) return;

        final RegionContainer container = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer();

        final RegionQuery query = container.createQuery();

        event.blockList().removeIf(block -> {
            com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(block.getLocation());
            final ApplicableRegionSet regions = query.getApplicableRegions(weLoc);
            final StateFlag.State state = regions.queryState(null, CrownExtraWGFlags.MACE_BLOCK_DESTRUCTION);
            return state == StateFlag.State.DENY;
        });
    }
}
