package com.kristoff.robomaster_simulator.robomasters;

import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.teams.Team;

public class Allies extends RoboMaster {
    boolean isRoamer = false;

    public Allies(Team team, String name){
        super("RoboMasters/AlexanderMaster.png",
                team, name);

    }

    public boolean isRoamer(){
        return isRoamer && Team.friend1.isAlive;
    }
    public void setAsRoamer(){
         isRoamer = true;
    }

    @Override
    public void start(){
        super.start();
    }
}
