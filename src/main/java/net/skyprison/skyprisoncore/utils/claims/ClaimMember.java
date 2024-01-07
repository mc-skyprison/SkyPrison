package net.skyprison.skyprisoncore.utils.claims;

import java.util.UUID;

public class ClaimMember {
    private final String claimId;
    private final UUID uuid;
    private String rank;
    public ClaimMember(String claimId, UUID uuid, String rank) {
        this.claimId = claimId;
        this.uuid = uuid;
        this.rank = rank;
    }
    public String getClaimId() {
        return claimId;
    }
    public UUID getUniqueId() {
        return uuid;
    }
    public String getRank() {
        return rank;
    }
    public void setRank(String rank) {
        this.rank = rank;
    }
}
