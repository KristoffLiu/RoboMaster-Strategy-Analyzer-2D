package com.kristoff.robomaster_simulator.robomasters;

import com.kristoff.robomaster_simulator.robomasters.modules.*;
import com.kristoff.robomaster_simulator.teams.allies.Allies;
import com.kristoff.robomaster_simulator.teams.Team;

/***
 * //    private double weight = 17.1;           //Kg
 * //    private int max_forward_speed = 3;      //m/s
 * //    private int max_cross_range_speed = 2;  //m/s
 * //    private float shooting_speed = 6;       //per second
 * //    private float cannon_range = 180;       //degree
 * //    private float bullet_speed = 25;        //m/s
 * //    private int max_carrying_bullet = 300;  //m/s
 */
public class Enemy extends RoboMaster {
    static Enemy lockedEnemy;

    public DetectionState detectionState = DetectionState.INITIALIZED;

    InView inView;
    int timerCount = 0;
    public int count = 0;

    public Enemy(Team team, String name){
        super("RoboMasters/AlexanderMaster.png", team, name);
        this.teamColor = TeamColor.RED;
        inView = new InView(this, 2);
        inView.start();
    }

    @Override
    public void setPosition(int x, int y) {
        if(isInTheView() && this.getPosition().distanceTo(x, y) > 500 && timerCount < 5){
            timerCount++;
            return;
        }
        count ++;
        timerCount = 0;
        setInTheView();
        super.setPosition(x, y);
    }

    @Override
    public void setPosition(int x, int y, float rotation) {
        if(isInTheView() && this.getPosition().distanceTo(x, y) > 200 && timerCount < 4){
            timerCount++;
            return;
        }
        count ++;
        timerCount = 0;
        setInTheView();
        super.setPosition(x, y, rotation);
    }

    public void setInTheView(){
        if(count > 2){
            detectionState = DetectionState.IN_VIEW;
        }
        inView.resetTimer();
    }

    public void setNotInTheView(){
        detectionState = DetectionState.LOST;
    }

    public void lock(){
        lockedEnemy = this;
    }

    public boolean isLocked(){
        return lockedEnemy == this;
    }

    public static Enemy getLockedEnemy(){
        return lockedEnemy;
    }

    public static Enemy getUnlockedEnemy(){
        return Allies.enemy().get(0) != lockedEnemy ? (Enemy)Allies.enemy().get(0) : (Enemy)Allies.enemy().get(1);
    }

    public boolean isAvailable(){
        return (this.isInTheView() || isInitialized()) && this.isAlive;
    }

    public boolean canBeSeen(){
        return isInTheView() || isInitialized();
    }

    public boolean isInTheView(){
        return this.detectionState == DetectionState.IN_VIEW;
    }

    public boolean isInitialized(){
        return this.detectionState == DetectionState.INITIALIZED;
    }

    public int getDetectionState(){
        return this.detectionState.ordinal();
    }
}
