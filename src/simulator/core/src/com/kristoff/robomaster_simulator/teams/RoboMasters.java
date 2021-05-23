package com.kristoff.robomaster_simulator.teams;

import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.kristoff.robomaster_simulator.core.SimulatorConfiguration;
import com.kristoff.robomaster_simulator.robomasters.modules.Actor;
import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.robomasters.Enemy;
import com.kristoff.robomaster_simulator.robomasters.Ally;
import com.kristoff.robomaster_simulator.systems.Systems;
import com.kristoff.robomaster_simulator.teams.allies.Allies;

public class RoboMasters{
    static SimulatorConfiguration config;

    public static Team all       = new Team();
    public static Allies allies = new Allies();
    public static Enemies enemies = new Enemies();

    public static Team Blue = new Team("Allies");
    public static Team Red = new Team("Red");

    public RoboMasters(SimulatorConfiguration simulatorConfiguration){
        config = simulatorConfiguration;
        init();
    }

    public static void init(){
        if(all.size() == 0){
            allies.add(new Ally(allies,"Ally1"));
            allies.add(new Ally(allies,"Ally2"));
            allies.ally1 = (Ally)allies.get(0);
            allies.ally2 = (Ally)allies.get(1);

            enemies.add(new Enemy(enemies,"Enemy1"));
            enemies.add(new Enemy(enemies,"Enemy2"));
            Enemy lockedEnemy = (Enemy) enemies.get(0);
            lockedEnemy.lock();
            Enemy unlockedEnemy = (Enemy) enemies.get(1);
            unlockedEnemy.lock();

            //((ShanghaiTechMasterIII)teamBlue.get(1)).setAsRoamer();
            all.addAll(allies);
            all.addAll(enemies);
        }
    }

    public void setTeamColor(boolean isBlue){

    }

    public void start(){
        initPosition();
        all.forEach(x->{
            x.start();
        });
        allies.start();
    }

    public void initPosition(){
        int i = 0;
        int j = 0;
        for(TextureMapObject textureMapObject : Systems.map.getBirthZones()){
            float halfWidth = textureMapObject.getTextureRegion().getRegionWidth() / 2f;
            float halfHeight = textureMapObject.getTextureRegion().getRegionHeight() / 2f;
            int x = (int)(textureMapObject.getX() + halfWidth);
            int y = (int)(textureMapObject.getY() + halfHeight);
            if(textureMapObject.getProperties().containsKey("blue")){
                RoboMasters.allies.get(i).actor.update(x, y, (float) (Math.PI));
                i ++;
            }
            else if(textureMapObject.getProperties().containsKey("red")){
                RoboMasters.enemies.get(j).actor.update(x, y, 0f);
                j ++;
            }
        }
    }

    public static void setPosition(String name, int x, int y, float rotation){
        for (RoboMaster roboMaster : RoboMasters.all){
            if(roboMaster.name.equals(name)){
                roboMaster.actor.update(x,y,rotation);
                break;
            }
        }
    }

    public static Actor getPosition(String name){
        for (RoboMaster roboMaster : RoboMasters.all){
            if(roboMaster.name.equals(name)){
                return roboMaster.actor;
            }
        }
        return null;
    }

    public static RoboMaster getRoboMaster(String name){
        for (RoboMaster roboMaster : RoboMasters.all){
            if(roboMaster.name.equals(name)){
                return roboMaster;
            }
        }
        return null;
    }
}
