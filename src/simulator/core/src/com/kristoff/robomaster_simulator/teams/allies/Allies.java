package com.kristoff.robomaster_simulator.teams.allies;

import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.kristoff.robomaster_simulator.robomasters.Ally;
import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.robomasters.modules.TeamColor;
import com.kristoff.robomaster_simulator.systems.Systems;
import com.kristoff.robomaster_simulator.teams.Enemies;
import com.kristoff.robomaster_simulator.teams.RoboMasters;
import com.kristoff.robomaster_simulator.teams.Team;
import com.kristoff.robomaster_simulator.teams.allies.enemyobservations.EnemiesObservationSimulator;
import com.kristoff.robomaster_simulator.teams.allies.friendobservations.FriendsObservationSimulator;

public class Allies extends Team {
    public static Ally ally1;
    public static Ally ally2;
    public static TeamColor teamColor;

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
        int i = 0;
        if(teamColor == TeamColor.BLUE) {
            Enemies.setTeamColor(TeamColor.RED);
            for(TextureMapObject textureMapObject : Systems.map.getBirthZones()){
                float halfWidth = textureMapObject.getTextureRegion().getRegionWidth() / 2f;
                float halfHeight = textureMapObject.getTextureRegion().getRegionHeight() / 2f;
                int x = (int)(textureMapObject.getX() + halfWidth);
                int y = (int)(textureMapObject.getY() + halfHeight);
                if(textureMapObject.getProperties().containsKey("blue")){
                    if(i == 0){
                        ally1.actor.update(x, y, (float) (Math.PI));
                    }
                    else if(i == 1){
                        ally2.actor.update(x, y, (float) (Math.PI));
                    }
                    i ++;
                }
            }
        }
        else{
            Enemies.setTeamColor(TeamColor.BLUE);
            for(TextureMapObject textureMapObject : Systems.map.getBirthZones()){
                float halfWidth = textureMapObject.getTextureRegion().getRegionWidth() / 2f;
                float halfHeight = textureMapObject.getTextureRegion().getRegionHeight() / 2f;
                int x = (int)(textureMapObject.getX() + halfWidth);
                int y = (int)(textureMapObject.getY() + halfHeight);
                if(textureMapObject.getProperties().containsKey("red")){
                    if(i == 0){
                        ally1.actor.update(x, y, (float) (Math.PI));
                    }
                    else if(i == 1){
                        ally2.actor.update(x, y, (float) (Math.PI));
                    }
                    i ++;
                }
            }
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
