package com.kristoff.robomaster_simulator.teams.allies;

import com.kristoff.robomaster_simulator.robomasters.Ally;
import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.robomasters.modules.TeamColor;
import com.kristoff.robomaster_simulator.teams.Enemies;
import com.kristoff.robomaster_simulator.teams.RoboMasters;
import com.kristoff.robomaster_simulator.teams.Team;
import com.kristoff.robomaster_simulator.teams.allies.enemyobservations.EnemiesObservationSimulator;
import com.kristoff.robomaster_simulator.teams.allies.friendobservations.FriendsObservationSimulator;

public class Allies extends Team {
    public static Ally ally1;
    public static Ally ally2;
    TeamColor teamColor;

    String name;
    public FriendsObservationSimulator friendsObservationSimulator; //敌军视野模拟
    public EnemiesObservationSimulator enemiesObservationSimulator; //友军视野模拟
    public InfoAnalyzer infoAnalyzer;
    public StrategyDispatcher strategyDispatcher;

    public Allies(){
        this.name = "Allies";
        if(this.name == "Allies"){
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

    public TeamColor getTeamColor(){
        return this.teamColor;
    }

    public void setTeamColor(TeamColor teamColor){
        this.teamColor = teamColor;
        ally1.teamColor = teamColor;
        ally2.teamColor = teamColor;
        if(teamColor == TeamColor.BLUE) {
            Enemies.setTeamColor(TeamColor.RED);
        }
        else{
            Enemies.setTeamColor(TeamColor.BLUE);
        }
    }


    public static Allies me(){
        return RoboMasters.allies;
    }

    public static Enemies enemy(){
        return RoboMasters.enemies;
    }

    public static int[][] getEnemiesObservationGrid(){
        return me().enemiesObservationSimulator.matrix;
    }

    public static boolean isHPSupplyNeeded(){
        return (ally1.health < 1800 && ally2.health < 1800);
    }
}
