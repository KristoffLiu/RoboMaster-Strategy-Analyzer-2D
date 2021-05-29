package com.kristoff.robomaster_simulator;

import com.kristoff.robomaster_simulator.core.Simulator;
import com.kristoff.robomaster_simulator.core.SimulatorConfiguration;
import com.kristoff.robomaster_simulator.core.SimulatorMode;
import com.kristoff.robomaster_simulator.robomasters.Ally;
import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.robomasters.Enemy;
import com.kristoff.robomaster_simulator.robomasters.modules.TeamColor;
import com.kristoff.robomaster_simulator.systems.buffs.BuffZone;
import com.kristoff.robomaster_simulator.systems.pointsimulator.PointState;
import com.kristoff.robomaster_simulator.teams.allies.Allies;
import com.kristoff.robomaster_simulator.teams.RoboMasters;
import com.kristoff.robomaster_simulator.systems.Systems;
import py4j.GatewayServer;

public class RosRMLauncher {
    public Simulator simulator;
    public SimulatorConfiguration config;
    public RoboMaster roboMaster;

    public RosRMLauncher(){
        this.config = new SimulatorConfiguration();
        this.config.mode = SimulatorMode.realMachine;
        this.simulator = new Simulator(config);
        this.simulator.launch();
        this.simulator.init();
    }

    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new RosRMLauncher());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }

    public Simulator getSimulator(){
        return simulator;
    }

    public void setTeamColor(int teamColor){
        if(teamColor == 0){
            RoboMasters.allies.setTeamColor(TeamColor.BLUE);
        }
        else if(teamColor == 1){
            RoboMasters.allies.setTeamColor(TeamColor.RED);
        }
    }

    public PointState[][] getMap(){
        return Systems.pointSimulator.getMatrix();
    }

    public void setPosition(String name, int x, int y, float rotation){
        RoboMasters.setPosition(name, x,y,rotation);
    }

    public RoboMaster getRoboMaster(String name){
        roboMaster = RoboMasters.getRoboMaster(name);
        return roboMaster;
    }

    public Ally getAlly(String name){
        return (Ally) RoboMasters.getRoboMaster(name);
    }

    public Enemy getEnemy(String name){
        return (Enemy)RoboMasters.getRoboMaster(name);
    }

    public void updateBuffZone(int buffZoneNo, int buffType, boolean isActive){
        BuffZone.updateBuffZone(buffZoneNo, buffType, isActive);
    }

    public void buffZoneDemoTest(){
        BuffZone.updateBuffZone(0,1, false);
        BuffZone.updateBuffZone(1,5, false);
        BuffZone.updateBuffZone(2,4, false);
        BuffZone.updateBuffZone(3,2, false);
        BuffZone.updateBuffZone(4,6, false);
        BuffZone.updateBuffZone(5,3, false);
    }

    public void setAsRoamer(String roboName){
        if(roboName.equals("blue1")){
            Allies.ally1.setAsRoamer();
        }
        else{
            Allies.ally2.setAsRoamer();
        }
    }

    public Enemy getLockedEnemy(){
        return Enemy.getLockedEnemy();
    }

    public void updateRemainingTime(int remainTime){
        Systems.refree.remainingTime = remainTime;
    }

    public void updateGameStatus(int gameStatus){
        Systems.refree.updateGameStatus(gameStatus);
    }
}
