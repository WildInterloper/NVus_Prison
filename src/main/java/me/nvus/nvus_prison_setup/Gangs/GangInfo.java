package me.nvus.nvus_prison_setup.Gangs;

public class GangInfo {
    private String name;
    private String ownerName;
    private int memberCount;

    // Constructor
    public GangInfo(String name, String ownerName, int memberCount) {
        this.name = name;
        this.ownerName = ownerName;
        this.memberCount = memberCount;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public int getMemberCount() {
        return memberCount;
    }
}
