package net.skyprison.skyprisoncore.utils.claims;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimData {
    private final String id;
    private String name;
    private final String parent;
    private final List<ClaimData> children = new ArrayList<>();
    private final String world;
    private long blocks;
    private UUID owner;
    private final List<ClaimMember> members = new ArrayList<>();
    public ClaimData(String id, String name, String parent, String world, long blocks, UUID owner) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.world = world;
        this.blocks = blocks;
        this.owner = owner;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getParent() {
        return parent;
    }
    public List<ClaimData> getChildren() {
        return children;
    }
    public void addChildren(List<ClaimData> children) {
        this.children.addAll(children);
    }
    public void removeChildren(List<ClaimData> children) {
        this.children.removeAll(children);
    }
    public void addChild(ClaimData child) {
        this.children.add(child);
    }
    public void removeChild(ClaimData child) {
        this.children.remove(child);
    }
    public String getWorld() {
        return world;
    }
    public long getBlocks() {
        return blocks;
    }
    public void setBlocks(long blocks) {
        this.blocks = blocks;
    }
    public UUID getOwner() {
        return owner;
    }
    public void setOwner(UUID owner) {
        this.owner = owner;
    }
    public List<ClaimMember> getMembers() {
        return members;
    }
    public ClaimMember getMember(UUID uuid) {
        return this.members.stream().filter(member -> member.getUniqueId().equals(uuid)).findFirst().orElse(null);
    }
    public boolean hasMember(ClaimMember member) {
        return this.members.contains(member);
    }
    public void addMembers(List<ClaimMember> members) {
        this.members.addAll(members);
    }
    public void removeMembers(List<ClaimMember> members) {
        this.members.removeAll(members);
    }
    public void addMember(ClaimMember member) {
        this.members.add(member);
    }
    public void removeMember(ClaimMember member) {
        this.members.remove(member);
    }
}
