package net.skyprison.skyprisoncore.utils.claims;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class ClaimFlagMob {
    private ItemStack item;
    private final ClaimData claim;
    private final EntityType type;
    private boolean isEnabled;
    public ClaimFlagMob(ItemStack item, ClaimData claim, EntityType type, boolean isEnabled) {
        this.item = item;
        this.claim = claim;
        this.type = type;
        this.isEnabled = isEnabled;
    }
    public ClaimData getClaim() {
        return claim;
    }
    public EntityType getType() {
        return type;
    }
    public boolean isEnabled() {
        return isEnabled;
    }
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
    public ItemStack getItem() {
        return item;
    }
    public void setItem(ItemStack item) {
        this.item = item;
    }
}
