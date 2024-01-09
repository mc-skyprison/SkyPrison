package net.skyprison.skyprisoncore.utils.claims;

import org.bukkit.inventory.ItemStack;

public class ClaimFlag {
    private ItemStack item;
    private final ClaimData claim;
    private final AvailableFlags flag;
    private Object flagValue;
    public ClaimFlag(ClaimData claim, AvailableFlags flag, Object flagValue, ItemStack item) {
        this.claim = claim;
        this.flag = flag;
        this.flagValue = flagValue;
        this.item = item;
    }
    public ClaimData getClaim() {
        return claim;
    }
    public AvailableFlags getFlag() {
        return flag;
    }
    public Object getFlagValue() {
        return flagValue;
    }
    public void setFlagValue(Object flagValue) {
        this.flagValue = flagValue;
    }
    public ItemStack getItem() {
        return item;
    }
    public void setItem(ItemStack item) {
        this.item = item;
    }
}
