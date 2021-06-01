package com.kristoff.robomaster_simulator.teams.allies;

import com.kristoff.robomaster_simulator.utils.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PossibleTargets {
    CopyOnWriteArrayList<Position> targets;

    public PossibleTargets(){
        targets = new CopyOnWriteArrayList<>();
    }

    public void clear(){
        targets = new CopyOnWriteArrayList<>();
    }

    public void add(int x, int y){

        targets.add(new Position(x, y));
    }

    public Position getTarget(int x){
        return targets.get(x);
    }

    public List<Position> getTargets(){
        return targets;
    }
}
