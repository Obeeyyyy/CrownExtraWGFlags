package de.obey.listener;


/*
    Author: Obey
    Date: 17.05.2026
    Time: 11:16
    Project: BlockDestuctionDrops
*/

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.obey.CrownExtraWGFlags;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Set;

public class EnderPearlListener implements Listener {

    @EventHandler
    public void on(final PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
            return;

        final Location to = event.getTo();
        final Location from = event.getFrom();

        final RegionContainer container = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer();

        final RegionQuery query = container.createQuery();

        com.sk89q.worldedit.util.Location weTo = BukkitAdapter.adapt(to);
        com.sk89q.worldedit.util.Location weFrom = BukkitAdapter.adapt(from);

        final ApplicableRegionSet regionsTo = query.getApplicableRegions(weTo);
        final ApplicableRegionSet regionsFrom = query.getApplicableRegions(weFrom);

        final StateFlag.State stateTo = regionsTo.queryState(null, CrownExtraWGFlags.PEARL_INTO);
        final StateFlag.State stateFrom = regionsFrom.queryState(null, CrownExtraWGFlags.PEARL_INTO);


        if (stateTo != StateFlag.State.DENY)
            return;

        final Set<ProtectedRegion> regionsToSet = regionsTo.getRegions();
        final Set<ProtectedRegion> regionsFromSet = regionsFrom.getRegions();

        final boolean enteringNewRegion = regionsToSet.stream()
                .anyMatch(r -> !regionsFromSet.contains(r)
                        && r.getFlag(CrownExtraWGFlags.PEARL_INTO) == StateFlag.State.DENY);

        if(enteringNewRegion)
            event.setCancelled(true);
    }

}
