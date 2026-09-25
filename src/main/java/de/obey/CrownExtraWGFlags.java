package de.obey;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import de.obey.listener.BlockDropListener;
import de.obey.listener.EnderPearlListener;
import de.obey.listener.FireSpawnListener;
import de.obey.listener.MaceBlockDestructionListener;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class CrownExtraWGFlags extends JavaPlugin {

    public static StateFlag BLOCK_DROP_FLAG, PEARL_INTO, MACE_BLOCK_DESTRUCTION, ANCHOR_FIRE_SPAWN;

    private volatile Set<Material> excludedDropMaterials = Collections.emptySet();

    public static CrownExtraWGFlags getInstance() {
        return getPlugin(CrownExtraWGFlags.class);
    }

    public static boolean isBlockDropExcluded(final Material material) {
        if (material == null) {
            return false;
        }
        final CrownExtraWGFlags instance = getInstance();
        return instance != null && instance.excludedDropMaterials.contains(material);
    }

    public Set<Material> getExcludedDropMaterials() {
        return excludedDropMaterials;
    }

    public void loadConfiguration() {
        saveDefaultConfig();
        reloadConfig();

        final Set<Material> materials = EnumSet.noneOf(Material.class);
        List<String> list = getConfig().getStringList("block-destruction-drops.exclude");
        if (list.isEmpty()) {
            list = getConfig().getStringList("block-destruction-drops.excluded-blocks");
        }
        if (list.isEmpty()) {
            list = getConfig().getStringList("block-drop-exclude");
        }

        for (final String entry : list) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            String matName = entry.trim().toUpperCase(Locale.ROOT);
            if (matName.startsWith("MINECRAFT:")) {
                matName = matName.substring("MINECRAFT:".length());
            }
            Material material = Material.matchMaterial(matName);
            if (material == null) {
                material = Material.getMaterial(matName);
            }
            if (material != null) {
                materials.add(material);
            } else {
                getLogger().warning("Unknown material in block-destruction-drops exclude list: " + entry);
            }
        }

        this.excludedDropMaterials = materials.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(materials);
    }

    @Override
    public void onLoad() {
        registerFlag();
    }

    @Override
    public void onEnable() {
        loadConfiguration();

        getServer().getPluginManager().registerEvents(new BlockDropListener(), this);
        getServer().getPluginManager().registerEvents(new EnderPearlListener(), this);
        getServer().getPluginManager().registerEvents(new MaceBlockDestructionListener(), this);
        getServer().getPluginManager().registerEvents(new FireSpawnListener(), this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("crownextrawgflags.reload") && !sender.hasPermission("crownextrawgflags.admin")) {
                sender.sendMessage("§cYou do not have permission to execute this command.");
                return true;
            }
            loadConfiguration();
            sender.sendMessage("§aCrownExtraWGFlags configuration reloaded.");
            return true;
        }
        sender.sendMessage("§eUsage: /" + label + " reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase(Locale.ROOT))) {
                return List.of("reload");
            }
        }
        return Collections.emptyList();
    }

    private void registerFlag() {
        final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        BLOCK_DROP_FLAG = registerOrGetFlag(registry, "block-destruction-drops", true);
        PEARL_INTO = registerOrGetFlag(registry, "pearl-into", true);
        MACE_BLOCK_DESTRUCTION = registerOrGetFlag(registry, "mace-block-destruction", true);
        ANCHOR_FIRE_SPAWN = registerOrGetFlag(registry, "anchor-fire-spawn", true);
    }

    private StateFlag registerOrGetFlag(final FlagRegistry registry, final String name, final boolean defaultValue) {
        try {
            final StateFlag flag = new StateFlag(name, defaultValue);
            registry.register(flag);
            return flag;
        } catch (final FlagConflictException e) {
            final com.sk89q.worldguard.protection.flags.Flag<?> existing = registry.get(name);
            if (existing instanceof StateFlag stateFlag) {
                return stateFlag;
            }
            return null;
        }
    }
}
