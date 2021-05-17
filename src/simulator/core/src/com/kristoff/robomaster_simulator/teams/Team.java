package com.kristoff.robomaster_simulator.teams;

import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.robomasters.Ally;
import com.kristoff.robomaster_simulator.teams.enemyobservations.EnemiesObservationSimulator;
import com.kristoff.robomaster_simulator.teams.friendobservations.FriendsObservationSimulator;

import java.util.concurrent.CopyOnWriteArrayList;

public class Team extends CopyOnWriteArrayList<RoboMaster> {
    public static boolean isOurTeamBlue = true;
    public static Ally ally1;
    public static Ally ally2;

    String name;
    public FriendsObservationSimulator friendsObservationSimulator; //敌军视野模拟
    public EnemiesObservationSimulator enemiesObservationSimulator; //敌军视野模拟
    public InfoAnalyzer infoAnalyzer;

    public Team(){}

    public Team(String teamName){
        this.name = teamName;
        if(teamName == "Allies"){
            friendsObservationSimulator = new FriendsObservationSimulator(this);
            enemiesObservationSimulator = new EnemiesObservationSimulator(this);
            infoAnalyzer = new InfoAnalyzer(this);
        }
    }

    public void start(){
        //friendsObservationSimulator.start();
        enemiesObservationSimulator.start();
        infoAnalyzer.start();
    }

    public int roboMastersLeft(){
        int count = 0;
        for(RoboMaster roboMaster : this){
            if(roboMaster.isAlive()) count++;
        }
        return count;
    }

    public static Team me(){
        return RoboMasters.allies;
    }

    public static Team enemy(){
        return RoboMasters.enemies;
    }

    public static int[][] getEnemiesObservationGrid(){
        return me().enemiesObservationSimulator.matrix;
    }
}
