package com.kristoff.robomaster_simulator.teams;

import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.robomasters.modules.TeamColor;

import java.util.concurrent.CopyOnWriteArrayList;

public class Team extends CopyOnWriteArrayList<RoboMaster> {
    String name;

    public Team() {
    }

    public Team(String teamName) {
        this.name = teamName;
    }

    public int roboMastersLeft() {
        int count = 0;
        for (RoboMaster roboMaster : this) {
            if (roboMaster.isAlive()) count++;
        }
        return count;
    }
}
