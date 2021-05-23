package com.kristoff.robomaster_simulator.robomasters;

import com.kristoff.robomaster_simulator.teams.allies.Allies;
import com.kristoff.robomaster_simulator.teams.Team;

//AlexanderIII
public class Ally extends RoboMaster {
    boolean isRoamer = false;

    public Ally(Team team, String name){
        super("RoboMasters/AlexanderMaster.png",
                team, name);

    }

    public boolean isRoamer(){
        return isRoamer && Allies.ally1.isAlive;
    }
    public void setAsRoamer(){
         isRoamer = true;
    }

    @Override
    public void start(){
        super.start();
    }
}
