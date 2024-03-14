package me.nvus.nvus_prison_setup.Ranks;

import java.util.List;

public class Rank {
    private String name;
    private double cost;
    private List<String> commands;

    public Rank(String name, double cost, List<String> commands) {
        this.name = name;
        this.cost = cost;
        this.commands = commands;
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getCost() {
        return cost;
    }

    public List<String> getCommands() {
        return commands;
    }

    // Additional method to get commands as a single string, separated by a semicolon
    public String getCommandsString() {
        return String.join(";", this.commands);
    }

    // Add any additional methods or modifications here
}
