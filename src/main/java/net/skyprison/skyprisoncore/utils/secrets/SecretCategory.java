package net.skyprison.skyprisoncore.utils.secrets;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public record SecretCategory(String name, String description, ItemStack displayItem, String permission, String permissionMessage, HashMap<String, List<String>> regions) {

}
