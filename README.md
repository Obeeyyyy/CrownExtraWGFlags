This plugin adds WorldGuard flags:
- `block-destruction-drops`
- `pearl-into`
- `mace-block-destruction`
- `anchor-fire-spawn`

# block-destruction-drops
Will stop blocks from dropping items when destroyed (by players or explosions).

### Configuration Exclude List
Blocks defined under `block-destruction-drops.exclude` in `config.yml` are excluded from the drop denial, meaning they will continue to drop items normally even in regions where `block-destruction-drops` is set to `DENY`:

```yaml
block-destruction-drops:
  exclude:
    - SPAWNER
```

# pearl-into
Will allow/deny you to enderpearl into a region.

# Commands
- `/crownextrawgflags reload` (or `/cewg reload`) - Reloads configuration (`crownextrawgflags.reload`)

