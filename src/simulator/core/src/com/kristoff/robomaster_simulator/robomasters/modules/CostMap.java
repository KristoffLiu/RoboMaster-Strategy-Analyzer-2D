package com.kristoff.robomaster_simulator.robomasters.modules;

import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.robomasters.Strategy.StrategyMaker;
import com.kristoff.robomaster_simulator.robomasters.Enemy;
import com.kristoff.robomaster_simulator.robomasters.Ally;
import com.kristoff.robomaster_simulator.systems.Systems;
import com.kristoff.robomaster_simulator.systems.pointsimulator.PointSimulator;
import com.kristoff.robomaster_simulator.systems.buffs.BuffZone;
import com.kristoff.robomaster_simulator.systems.costmap.PositionCost;
import com.kristoff.robomaster_simulator.teams.allies.Allies;
import com.kristoff.robomaster_simulator.teams.allies.enemyobservations.EnemiesObservationSimulator;
import com.kristoff.robomaster_simulator.utils.LoopThread;
import com.kristoff.robomaster_simulator.utils.Position;

public class CostMap extends LoopThread {
    public Ally roboMaster;
    public StrategyMaker strategyMaker;
    public int[][] costmap;
    public PositionCost minPositionCost;

    public CostMap(RoboMaster roboMaster){
        this.roboMaster = (Ally) roboMaster;
        this.costmap = new int[849][489];
        isStep = true;
        delta = 1/10f;
        minPositionCost = new PositionCost(0,0,0);
    }

    @Override
    public void step(){
        generateCostMap();
    }

    @Override
    public void start(){
        super.start();
        this.strategyMaker = roboMaster.strategyMaker;
        for(int i = 0; i < 849; i ++){
            for(int j = 0; j < 489; j ++){
                if(PointSimulator.isPointOverTheMap(i, j)){
                    costmap[i][j] = 999;
                }
            }
        }
    }

    public void generateCostMap(){
        synchronized (costmap){
            long  startTime = System.currentTimeMillis();    //获取开始时间
            PositionCost minPositionCost = new PositionCost(9999,0,0);
            for(int i = 19; i < 830; i ++){
                for(int j = 19; j < 470; j ++){
                    int cost = 127;
                    if(Systems.pointSimulator.isPointTheObstacle(i, j)){
                        costmap[i][j] = 999;
                        continue;
                    }
                    cost += costOfEnemyObservation(i, j);
                    cost += costOfBuff(i, j);
                    cost += costOfTheCorners(i, j);
                    cost += costToMyself(i, j);
                    cost += costOfFriendEntity(i, j);
                    cost += costOfFriendDecision(i, j);
                    costmap[i][j] = cost;
                    if(minPositionCost.cost > cost) {
                        minPositionCost.cost = cost;
                        minPositionCost.x = i;
                        minPositionCost.y = j;
                    }
                }
            }
            this.minPositionCost = minPositionCost;
        }
    }

    public int costOfEnemyObservation(int x, int y){
        int cost = 0;

        if(EnemiesObservationSimulator.isInLockedEnemyViewOnly(x, y) && Enemy.getLockedEnemy().isAvailable()) {
            cost = costOfLockedEnemyDistance(x, y);
        }
        else if(EnemiesObservationSimulator.isInUnlockedEnemyViewOnly(x, y) && Enemy.getUnlockedEnemy().isAvailable()) {
            cost = costOfUnlockedEnemyDistance(x, y);
        }
        else if(EnemiesObservationSimulator.isOutOfBothEnemiesView(x, y))
            cost = 0;
        else if(EnemiesObservationSimulator.isInBothEnemiesView(x, y)) {
            if(Enemy.getLockedEnemy().isAvailable() && Enemy.getUnlockedEnemy().isAvailable()){
                cost = costOfBothEnemyDistance(x, y);
            }
            else if(Enemy.getLockedEnemy().isAvailable()){
                cost = costOfLockedEnemyDistance(x, y);
            }
            else if(Enemy.getUnlockedEnemy().isAvailable()){
                cost = costOfUnlockedEnemyDistance(x, y);
            }
        }
        return cost;
    }

    public int costOfFriendEntity(int x, int y){
        if(this.roboMaster.name == "Ally1"){
            float distanceToFriend = Allies.ally2.getPointPosition().distanceTo(x,y);
            float cost = 0;
            if(distanceToFriend <= 65){
                cost = 999;
            }
            return (int) cost;
        }
        else{
            float distanceToFriend = Allies.ally1.getPointPosition().distanceTo(x,y);
            float cost = 0;
            if(distanceToFriend <= 65){
                cost = 999;
            }
            return (int) cost;
        }
    }

    public int costOfFriendDecision(int x, int y){
        if(this.roboMaster.isRoamer()){
            float outerRange = 100;
            float outerPeek = 45;
            float distanceToFriendDecision = this.strategyMaker.getFriendDecision().position.manhattanDistanceTo(x, y);
            float cost = 0;
            if(distanceToFriendDecision <= 65){
                cost = 999;
            }
            else if(distanceToFriendDecision <= 65 + outerRange){
                cost = (outerRange + 65 - distanceToFriendDecision) / outerRange * outerPeek;
            }
            return (int) cost;
        }
        return 0;
    }

    public int costOfLockedEnemyDistance(int x, int y){
        int maxRange = EnemiesObservationSimulator.getRadius();
        int minShootingRange = 80;
        int maxShootingRange = 230;
        int peekVal = 64;
        int troughVal = - 75;
        int troughVal2 = - 100;
        float distanceToEnemy = Enemy.getLockedEnemy().getPointPosition().distanceTo(x,y);
        float cost = 0;
        if(distanceToEnemy <= 65){
            cost = 999;
        }
        else if(distanceToEnemy <= minShootingRange){
            cost = peekVal + distanceToEnemy / minShootingRange * (troughVal - peekVal);
        }
        else if(distanceToEnemy <= maxShootingRange){
            cost = (distanceToEnemy - minShootingRange) / (maxShootingRange - minShootingRange) * (troughVal2 - troughVal) + troughVal;
        }
        else{
            cost = (distanceToEnemy - maxShootingRange) / (maxRange - maxShootingRange) * (- troughVal) + troughVal;
        }
        return (int) cost;
    }

    public int costOfUnlockedEnemyDistance(int x, int y){
        int maxRange = EnemiesObservationSimulator.getRadius();
        int peekVal = 128;
        float distanceToEnemy = Enemy.getUnlockedEnemy().getPointPosition().distanceTo(x,y);
        float cost = 0;
        if(distanceToEnemy <= 65){
            cost = 999;
        }
        else{
            cost = (maxRange - distanceToEnemy) / maxRange * peekVal;
        }
        return (int) cost;
    }

    public int costOfBothEnemyDistance(int x, int y){
        if(!Enemy.getUnlockedEnemy().isAlive){
            return 0;
        }
        int maxRange = EnemiesObservationSimulator.getRadius();
        int peekVal = 64;
        float distanceToEnemy = Enemy.getLockedEnemy().getPointPosition().distanceTo(x,y);
        float distanceToEnemy2 = Enemy.getUnlockedEnemy().getPointPosition().distanceTo(x,y);
        float cost = 0;
        if(distanceToEnemy <= 65 || distanceToEnemy2 <= 65){
            cost = 999;
        }
        else {
            cost = (maxRange - distanceToEnemy) / maxRange * peekVal;
        }
        return (int) cost;
    }

    public int costOfTheCorners(int x, int y){
        Position centre = new Position(849 / 2 , 489 / 2);
        int peekVal = 150;
        float distanceToTheCentre = centre.distanceTo(x,y);
        int costOfDistanceToEnemy = 0;
        if(distanceToTheCentre > 350 && distanceToTheCentre <= 470){
            costOfDistanceToEnemy = (int) ((distanceToTheCentre - 350) / (470 - 350) * peekVal);
        }
        return costOfDistanceToEnemy;
    }

    public int costToMyself(int x, int y){
        Position master = this.roboMaster.getPointPosition();
        int peekVal = 88;
//        float distanceToEnemy = Enemy.getLockedEnemy().getPointPosition().distanceTo(roboMaster.getPointPosition());
//        if(distanceToEnemy < 952f / 2.5f){
//            peekVal = 198;
//        }
        float distance = master.distanceTo(x,y);
        float maxRange = 952f;
        return (int)(distance / maxRange * peekVal);
    }

    public int costOfBuff(int x, int y){
        return BuffZone.costOfBuff(x, y, roboMaster);
    }

    public int costOfBulletOrbit(int x, int y){
        return 0;
    }

    public int[][] getCostMap(){
        return this.costmap;
    }

    public int getCost(int x, int y){
        if(x >= 0 && x < 849 && y >= 0 && y < 489){
            return costmap[x][y];
        }
        else return 999;
    }

    public PositionCost getMinPositionCost(){
        return minPositionCost;
    }
}
