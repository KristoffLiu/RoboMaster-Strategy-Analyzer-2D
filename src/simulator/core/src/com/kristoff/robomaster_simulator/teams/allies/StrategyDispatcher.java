package com.kristoff.robomaster_simulator.teams.allies;

import com.kristoff.robomaster_simulator.robomasters.Ally;
import com.kristoff.robomaster_simulator.systems.Systems;
import com.kristoff.robomaster_simulator.systems.buffs.BuffZone;
import com.kristoff.robomaster_simulator.teams.Team;
import com.kristoff.robomaster_simulator.utils.LoopThread;

import java.util.List;

public class StrategyDispatcher extends LoopThread {
    Allies team;
    List<BuffZone> avaibleBuffZoneList;

    public StrategyDispatcher(Allies team){
        isStep = true;
        delta = 1/5f;
    }

    public void dispatch(){
        List<BuffZone> availableBuffZoneList = Systems.refree.getAvailableBuffZone(team.teamColor);
        for(BuffZone buffZone : availableBuffZoneList){
            //buffZone.is
        }
    }
}
