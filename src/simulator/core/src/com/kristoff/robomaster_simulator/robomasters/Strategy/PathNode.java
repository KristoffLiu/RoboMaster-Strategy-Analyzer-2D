package com.kristoff.robomaster_simulator.robomasters.Strategy;

import com.kristoff.robomaster_simulator.utils.Position;

public class PathNode extends Position {
    float yaw = 0;

    public PathNode(int x, int y, float yaw){
        super(x, y);
        this.yaw = yaw;
    }

    public float getYaw() {
        return yaw;
    }
}
