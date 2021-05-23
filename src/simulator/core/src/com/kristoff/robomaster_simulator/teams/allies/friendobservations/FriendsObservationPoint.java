package com.kristoff.robomaster_simulator.teams.allies.friendobservations;

import com.kristoff.robomaster_simulator.utils.Position;

public class FriendsObservationPoint extends Position {
    public int observationStatus = 0;

    public FriendsObservationPoint(int x, int y, int observationStatus){
        this.x = x;
        this.y = y;
        this.observationStatus = observationStatus;
    }
}
