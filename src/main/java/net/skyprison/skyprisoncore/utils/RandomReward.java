package net.skyprison.skyprisoncore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.commands.Bomb;
import net.skyprison.skyprisoncore.items.Greg;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public enum RandomReward {
    SMALL_BOMB(6, "", "<dark_gray><italic>Could come in handy.."),
    MEDIUM_BOMB(4.5, "", "<dark_gray><italic>Could come in handy.."),
    LARGE_BOMB(2.5, "", "<dark_gray><italic>Could come in handy.."),
    MASSIVE_BOMB(1, "", "<dark_gray><italic>Could come in handy.."),
    NUKE_BOMB(0.1, "", "<dark_gray><italic>What maniac left this in the bushes!?"),

    DIAMOND(1, "", "<dark_gray><italic>Oooo, shiny!"),
    IRON_INGOT(5, "", "<dark_gray><italic>Looks rusty.."),
    GOLD_INGOT(2.5, "", "<dark_gray><italic>We're rich!"),
    LAPIS_LAZULI(3, "<#2DD4DC>Allay Dust", "<dark_gray><italic>Should keep this hidden from the guards.."),
    GOLD_NUGGET(2.5, "<#FFD700>Lost Tooth", "<dark_gray><italic>Should probably wash this first.."),

    MAGGOTY_BREAD(3, "<#7E6A45>Maggoty Bread", "<dark_gray><italic>We ain't had nothing but maggoty bread for three stinking days!"),
    SUSPICIOUS_MEAT(2.5, "<#C38A8A>Suspicous Meat", "<dark_gray><italic>Looks like meat's back on the menu, boys!"),
    GOLDEN_APPLE(1, "<#f2ff00>Golden Delight", "<dark_gray><italic>Looks delicious."),

    BROKEN_TOOL(1, "Broken", "<dark_gray><italic>Doesn't seem repairable.."),
    DAMAGED_TOOL(2, "Damaged", "<dark_gray><italic>Still got a few uses left.."),
    WORN_TOOL(5, "Worn", "<dark_gray><italic>Started to rust.."),
    USED_TOOL(1.5, "Used", "<dark_gray><italic>Slightly used.."),
    NEW_TOOL(0.1, "Brand New", "<dark_gray><italic>Sparkly new!"),

    GLOW_BERRIES(1.5, "", ""),
    SWEET_BERRIES(2.0, "", ""),

    FECES(0.5, "<#b25f23>Brown Surprise", "<dark_gray><italic>That wasnt a mushroom..");

    private final double chance;
    private final String title;
    private final String desc;

    RandomReward(double chance, String title, String desc) {
        this.chance = chance;
        this.title = title;
        this.desc = desc;
    }

    public double getChance() {
        return this.chance;
    }

    public String getDescription() {
        return this.desc;
    }

    public String getTitle() {
        return this.title;
    }

    private static final Material[] TOOLS = {
            Material.FISHING_ROD,
            Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE,
            Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE,
            Material.STONE_SWORD, Material.IRON_SWORD
    };

    public static ItemStack getRandomReward(SkyPrisonCore plugin) {
        List<RandomReward> weightedList = new ArrayList<>();
        for (RandomReward reward : RandomReward.values()) {
            int chance = (int) (reward.getChance() * 100);
            for (int i = 0; i < chance; i++) {
                weightedList.add(reward);
            }
        }

        int randomIndex = new Random().nextInt(weightedList.size());
        RandomReward reward = weightedList.get(randomIndex);

        return itemFromReward(plugin, reward);
    }

    public static int biasedRandom(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }

        Random r = new Random();
        double rawRandom = r.nextDouble(); // random between 0.0(inclusive) and 1.0(exclusive)
        double biasedRandom = 1 - Math.sqrt(rawRandom); // bias towards lower values

        return (int) (min + (biasedRandom * (max - min)));
    }


    private static ItemStack itemFromReward(SkyPrisonCore plugin, RandomReward reward) {
        String rew = reward.name().toLowerCase();
        ItemStack item = new ItemStack(Material.DIRT, 1);
        ItemMeta iMeta = item.getItemMeta();
        int amount = 1;
        ArrayList<Component> lore = new ArrayList<>();
        Random rand = new Random();

        if(rew.contains("bomb")) {
            if(!rew.contains("nuke")) {
                int chance = rand.nextInt(100);
                if (chance > 94) {
                    amount = biasedRandom(2, 4);
                }
            }
            item = Bomb.getBomb(plugin, reward.toString(), amount);
        } else if(rew.contains("feces")) {
            amount = rand.nextInt(5) + 1;
            item = new ItemStack(Material.BROWN_DYE, amount);
            lore.add(MiniMessage.miniMessage().deserialize(reward.desc));
            iMeta.lore(lore);
            iMeta.displayName(MiniMessage.miniMessage().deserialize(reward.title));
            item.setItemMeta(iMeta);
        } else if(rew.equalsIgnoreCase("maggoty_bread")) {
            amount =biasedRandom(4, 16);
            item = new ItemStack(Material.BREAD, amount);
            lore.add(MiniMessage.miniMessage().deserialize(reward.desc));
            iMeta.lore(lore);
            iMeta.displayName(MiniMessage.miniMessage().deserialize(reward.title));
            item.setItemMeta(iMeta);
        } else if(rew.equalsIgnoreCase("suspicious_meat")) {
            amount = biasedRandom(4, 16);
            item = new ItemStack(Material.COOKED_BEEF, amount);
            lore.add(MiniMessage.miniMessage().deserialize(reward.desc));
            iMeta.lore(lore);
            iMeta.displayName(MiniMessage.miniMessage().deserialize(reward.title));
            item.setItemMeta(iMeta);
        } else if(rew.equalsIgnoreCase("lapis_lazuli")) {
            amount = biasedRandom(4, 16);
            item = Greg.getAllayDust(plugin, amount);
        } else if(rew.equalsIgnoreCase("golden_apple")) {
            amount = rand.nextInt(3) + 1;
            item = new ItemStack(Material.GOLDEN_APPLE, amount);
            lore.add(MiniMessage.miniMessage().deserialize(reward.desc));
            iMeta.lore(lore);
            iMeta.displayName(MiniMessage.miniMessage().deserialize(reward.title));
            item.setItemMeta(iMeta);
        } else if(rew.equalsIgnoreCase("gold_nugget")) {
            amount = rand.nextInt(3) + 1;
            item = new ItemStack(Material.GOLD_NUGGET, amount);
            lore.add(MiniMessage.miniMessage().deserialize(reward.desc));
            iMeta.lore(lore);
            iMeta.displayName(MiniMessage.miniMessage().deserialize(reward.title));
            item.setItemMeta(iMeta);
        } else if(rew.contains("tool")) {
            item = new ItemStack(TOOLS[rand.nextInt(TOOLS.length)], amount);
            String type = item.getType().toString().toLowerCase().split("_")[1];
            String condition = rew.split("_")[0];

            int maxDura = item.getType().getMaxDurability();
            int damage = 0;

            boolean addEnchants = false;
            int enchantLevel = 0;

            switch (condition) {
                case "broken" -> {
                    damage = (int) (maxDura * 0.95);
                    if (rand.nextInt(100) < 20) {
                        addEnchants = true;
                        enchantLevel = ThreadLocalRandom.current().nextInt(1, 6 - rand.nextInt(2));
                    }
                }
                case "damaged" -> {
                    damage = (int) (maxDura * 0.75);
                    if (rand.nextInt(100) < 15) {
                        addEnchants = true;
                        enchantLevel = ThreadLocalRandom.current().nextInt(1, 6 - rand.nextInt(2));
                    }
                }
                case "worn" -> {
                    damage = maxDura / 2;
                    if (rand.nextInt(100) < 10) {
                        addEnchants = true;
                        enchantLevel = ThreadLocalRandom.current().nextInt(1, 6 - rand.nextInt(3));
                    }
                }
                case "used" -> {
                    damage = (int) (maxDura * 0.25);
                    if (rand.nextInt(100) < 5) {
                        addEnchants = true;
                        enchantLevel = ThreadLocalRandom.current().nextInt(1, 6 - rand.nextInt(4));
                    }
                }
                case "new" -> {
                    if (rand.nextInt(100) < 1) {
                        addEnchants = true;
                        enchantLevel = ThreadLocalRandom.current().nextInt(1, 6 - rand.nextInt(5));
                    }
                }
            }

            if(addEnchants) {
                if(item.getType().equals(Material.FISHING_ROD)) {
                    iMeta.addEnchant(Enchantment.LURE, enchantLevel, false);
                    if(enchantLevel > 2) {
                        enchantLevel = enchantLevel / 2;
                        iMeta.addEnchant(Enchantment.LUCK, enchantLevel, false);
                    }
                } else if(item.getType().toString().toLowerCase().contains("sword")) {
                    iMeta.addEnchant(Enchantment.DAMAGE_ALL, enchantLevel, false);
                    if(enchantLevel > 2) {
                        enchantLevel = enchantLevel / 2;
                        iMeta.addEnchant(Enchantment.DURABILITY, enchantLevel, false);
                    }
                } else {
                    iMeta.addEnchant(Enchantment.DIG_SPEED, enchantLevel, false);
                    if(enchantLevel > 2) {
                        enchantLevel = enchantLevel / 2;
                        iMeta.addEnchant(Enchantment.DURABILITY, enchantLevel, false);
                    }
                }
            }

            if(item.getType().equals(Material.FISHING_ROD)) {
                type = item.getType().toString().toLowerCase().replace("_", " ");
            }
            String name = WordUtils.capitalize(reward.title + " " + type);
            lore.add(MiniMessage.miniMessage().deserialize(reward.desc));
            iMeta.lore(lore);
            iMeta.displayName(Component.text(name, NamedTextColor.GRAY));
            item.setItemMeta(iMeta);

            item.setRepairCost(10000);
            item.setDamage(damage);
        } else {
            amount = biasedRandom(4, 16);
            item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(rew)), amount);
        }
        return item;
    }
}
