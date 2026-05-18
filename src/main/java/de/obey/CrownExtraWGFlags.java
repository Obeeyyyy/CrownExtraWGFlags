package de.obey;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import de.obey.listener.BlockDropListener;
import de.obey.listener.EnderPearlListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrownExtraWGFlags extends JavaPlugin {

    public static StateFlag BLOCK_DROP_FLAG, PEARL_INTO;

    public static CrownExtraWGFlags getInstance() {
        return getPlugin(CrownExtraWGFlags.class);
    }

    @Override
    public void onLoad() {
        registerFlag();
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new BlockDropListener(), this);
        getServer().getPluginManager().registerEvents(new EnderPearlListener(), this);
    }

    private void registerFlag() {
        final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            BLOCK_DROP_FLAG = new StateFlag("block-destruction-drops", true);
            registry.register(BLOCK_DROP_FLAG);

            PEARL_INTO = new StateFlag("pearl-into", true);
            registry.register(PEARL_INTO);
        } catch (final FlagConflictException ignored){}
    }
}
