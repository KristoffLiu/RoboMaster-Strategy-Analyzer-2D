package com.kristoff.robomaster_simulator.systems.costmap;

import com.kristoff.robomaster_simulator.utils.Position;

public class PositionCost extends Position {
    public int cost;

    public PositionCost(int cost, int x, int y){
        this.cost = cost;
        this.x = x;
        this.x = y;
    }

    public PositionCost(int value, Position position){
        this(value, position.x, position.y);
    }
}
