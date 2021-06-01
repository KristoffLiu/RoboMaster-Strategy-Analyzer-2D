package com.kristoff.robomaster_simulator.robomasters.modules;

import com.kristoff.robomaster_simulator.robomasters.DetectionState;
import com.kristoff.robomaster_simulator.robomasters.Enemy;
import com.kristoff.robomaster_simulator.utils.LoopThread;

public class InView extends LoopThread {
    Enemy thisEnemy;
    int timer = 0;
    int timerLimit = 0;

    public InView(Enemy enemy, int timerLimit){
        this.thisEnemy = enemy;
        this.timerLimit = timerLimit;
        delta = 1f;
        isStep = true;
    }

    @Override
    public void step() {
        timer ++;
        if(timer > timerLimit && this.thisEnemy.count > 2)
            if(this.thisEnemy.isAlive()){
                this.thisEnemy.setNotInTheView();
            }
            else {
                this.thisEnemy.detectionState = DetectionState.IN_VIEW;
                this.isStep = false;
            }
    }

    public void resetTimer(){
        this.timer = 0;
    }
}
