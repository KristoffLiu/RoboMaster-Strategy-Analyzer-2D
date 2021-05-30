package com.kristoff.robomaster_simulator.teams;

import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.kristoff.robomaster_simulator.robomasters.Ally;
import com.kristoff.robomaster_simulator.robomasters.DetectionState;
import com.kristoff.robomaster_simulator.robomasters.Enemy;
import com.kristoff.robomaster_simulator.robomasters.modules.TeamColor;
import com.kristoff.robomaster_simulator.systems.Systems;

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
        int i = 0;
        if(teamColor == TeamColor.BLUE)
        {
            for(TextureMapObject textureMapObject : Systems.map.getBirthZones()){
                float halfWidth = textureMapObject.getTextureRegion().getRegionWidth() / 2f;
                float halfHeight = textureMapObject.getTextureRegion().getRegionHeight() / 2f;
                int x = (int)(textureMapObject.getX() + halfWidth);
                int y = (int)(textureMapObject.getY() + halfHeight);
                if(textureMapObject.getProperties().containsKey("blue")){
                    if(i == 0){
                        enemy1.actor.update(x, y, (float) (Math.PI));
                        enemy1.detectionState = DetectionState.Initialized;
                    }
                    else if(i == 1){
                        enemy2.actor.update(x, y, (float) (Math.PI));
                        enemy2.detectionState = DetectionState.Initialized;
                    }
                    i ++;
                }
            }
        }
        else{
            for(TextureMapObject textureMapObject : Systems.map.getBirthZones()){
                float halfWidth = textureMapObject.getTextureRegion().getRegionWidth() / 2f;
                float halfHeight = textureMapObject.getTextureRegion().getRegionHeight() / 2f;
                int x = (int)(textureMapObject.getX() + halfWidth);
                int y = (int)(textureMapObject.getY() + halfHeight);
                if(textureMapObject.getProperties().containsKey("red")){
                    if(i == 0){
                        enemy1.actor.update(x, y, (float) (Math.PI));
                        enemy1.detectionState = DetectionState.Initialized;
                    }
                    else if(i == 1){
                        enemy2.actor.update(x, y, (float) (Math.PI));
                        enemy2.detectionState = DetectionState.Initialized;
                    }
                    i ++;
                }
            }
        }
    }
}
