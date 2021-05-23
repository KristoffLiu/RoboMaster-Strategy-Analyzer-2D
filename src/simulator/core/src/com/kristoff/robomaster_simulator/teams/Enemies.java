package com.kristoff.robomaster_simulator.teams;

import com.kristoff.robomaster_simulator.robomasters.Ally;
import com.kristoff.robomaster_simulator.robomasters.Enemy;
import com.kristoff.robomaster_simulator.robomasters.modules.TeamColor;

public class Enemies extends Team{
    public static Enemy enemy1;
    public static Enemy enemy2;
    public static TeamColor teamColor;

    public Enemies(){
        this.name = "Enemies";
        this.teamColor = TeamColor.RED;
    }

    public static boolean isHPSupplyNeeded(){
        return (enemy1.health < 1800 && enemy2.health < 1800);
    }

    public static TeamColor getTeamColor(){
        return teamColor;
    }

    public static void setTeamColor(TeamColor teamColor){
        teamColor = teamColor;
        enemy1.teamColor = teamColor;
        enemy2.teamColor = teamColor;
    }
}
