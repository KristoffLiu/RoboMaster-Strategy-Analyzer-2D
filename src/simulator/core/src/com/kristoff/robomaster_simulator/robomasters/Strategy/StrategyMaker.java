package com.kristoff.robomaster_simulator.robomasters.Strategy;

import com.badlogic.gdx.math.Vector2;
import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.robomasters.modules.CostMap;
import com.kristoff.robomaster_simulator.robomasters.Enemy;
import com.kristoff.robomaster_simulator.robomasters.Ally;
import com.kristoff.robomaster_simulator.systems.Systems;
import com.kristoff.robomaster_simulator.systems.buffs.BuffZone;
import com.kristoff.robomaster_simulator.systems.pointsimulator.PointState;
import com.kristoff.robomaster_simulator.teams.Enemies;
import com.kristoff.robomaster_simulator.teams.allies.Allies;
import com.kristoff.robomaster_simulator.teams.RoboMasters;
import com.kristoff.robomaster_simulator.teams.allies.enemyobservations.EnemiesObservationSimulator;
import com.kristoff.robomaster_simulator.systems.pointsimulator.PointSimulator;
import com.kristoff.robomaster_simulator.utils.LoopThread;
import com.kristoff.robomaster_simulator.utils.Position;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

public class StrategyMaker extends LoopThread {
    public Ally roboMaster;
    StrategyAnalyzer strategyAnalyzer;

    /***
     * -1: Not Working
     * 0: 1 v 1 state
     * 1: 1 v 2 state
     * 2: 2 v 1 state
     * 3: 2 v 2 state
     */
    //int counterState = -1;
    Position decisionMade;

    SearchNode friendDecision;

    public boolean[][] visitedGrid;
    public CostMap costMap;

    public SearchNode                                       rootNode;
    public SearchNode                                       decisionNode;
    public SearchNode                                       resultNode;

    public Queue<SearchNode>                                queue;
    public CopyOnWriteArrayList<SearchNode>                 resultNodes;
    public CopyOnWriteArrayList<SearchNode>                 pathNodes;

    public CounterState counterState = CounterState.TwoVSTwo;
    public StrategyState strategyState = StrategyState.NOTWORKING;

    public StrategyMaker(RoboMaster roboMaster){
        this.roboMaster = (Ally)roboMaster;
        visitedGrid = new boolean[849][489];

        rootNode                = new SearchNode();
        decisionNode            = new SearchNode();
        resultNode              = new SearchNode();
        friendDecision          = new SearchNode();

        queue                  = new LinkedList<>();
        resultNodes            = new CopyOnWriteArrayList<SearchNode>();
        pathNodes              = new CopyOnWriteArrayList<SearchNode>();

        strategyAnalyzer = new StrategyAnalyzer(this);
        costMap = roboMaster.costMap;

        this.delta = 1/30f;
        this.isStep = true;
    }

    @Override
    public void step(){
        makeDecision();
    }

    public void makeDecision(){
        visitedGrid = new boolean[849][489];
        queue.clear();
        strategyAnalyzer.analyze();
    }

    public void update(SearchNode resultNode,
                       boolean[][] visitedGrid,
                       CopyOnWriteArrayList<SearchNode> resultNodes,
                       CopyOnWriteArrayList<SearchNode> pathNodes){
        this.resultNode = resultNode;
        this.visitedGrid = visitedGrid;
        this.resultNodes = resultNodes;
        synchronized (this.pathNodes) {
            this.pathNodes = pathNodes;
        }
        decisionNode = resultNode;
        updateStrategyState();
        if (this.roboMaster.isRoamer()) {
            this.getFriendRoboMaster().strategyMaker.setFriendDecision(new SearchNode(decisionNode.position.x, decisionNode.position.y));
        }
    }

    public void setFriendDecision(SearchNode node){
        this.friendDecision.position = node.position;
    }

    public SearchNode getFriendDecision(){
        return this.friendDecision;
    }

    public void updateCounterState(CounterState counterState){
        this.counterState = counterState;
    }

    public void makeDecision(int counterState){

    }

    public void setDecisionNode(SearchNode node){
        this.decisionNode = node;
    }

    public Position getDecisionMade(){
        if(this.roboMaster.name.equals("Blue1")){
            //System.out.println(this.decisionNode.position.x + "  " + this.decisionNode.position.y);
        }
        return this.decisionNode.position;
    }

    public CopyOnWriteArrayList<SearchNode> getPathNodes(){
        return this.pathNodes;
    }

    public List<Position> getPath(){
        try{
            synchronized (this.pathNodes){
                List<Position> positions = new ArrayList<>();
                for(int i = this.pathNodes.size() - 1; i >= 0; i --){
                    positions.add(this.pathNodes.get(i).position);
                }
                return positions;
            }
        }
        catch (ArrayIndexOutOfBoundsException e){
            return new ArrayList<>();
        }
    }

    public CopyOnWriteArrayList<SearchNode> getResultNodes(){
        return this.resultNodes;
    }

    public int[][] getEnemiesObservationGrid(){
        return ((Allies)roboMaster.team).getEnemiesObservationGrid();
    }

    public Position getCurrentPosition(){
        return new Position(roboMaster.actor.x / 10, roboMaster.actor.y / 10);
    }

    public PointState getPointStatus(){
        return this.roboMaster.pointState;
    }

    public boolean[][] getVisitedGrid(){
        return visitedGrid;
    }

    public boolean isVisited(int x, int y){
        return visitedGrid[x][y];
    }

    public RoboMaster getFriendRoboMaster() {
        for(RoboMaster roboMaster : RoboMasters.allies){
            if(this.roboMaster != roboMaster) return roboMaster;
        }
        return null;
    }

    public float getAngularSeparation(int x, int y) {
        Position friendPosition = getFriendRoboMaster().strategyMaker.getDecisionMade();
        Position enemyPosition = Enemy.getLockedEnemy().getPointPosition();
        Vector2 friendSide = new Vector2(friendPosition.x - enemyPosition.x, friendPosition.y - enemyPosition.y);
        Vector2 myside = new Vector2(x - enemyPosition.x, y - enemyPosition.y);
        float result = friendSide.angleDeg(myside);

        return result;
    }

    public SearchNode getDecisionNode() {
        for(RoboMaster roboMaster : RoboMasters.allies){
            if(this.roboMaster != roboMaster) return roboMaster.strategyMaker.decisionNode;
        }
        return null;
    }


    public boolean isPointInsideMap(Position position){
        return PointSimulator.isPointInsideMap(position.x, position.y);
    }

    public boolean isSafeNow(int x, int y){
        for(int i=0;i<60;i++){
            for(int j=0;j<45;j++){
                Position position = roboMaster.actor.getPoint(i, j);
                if(EnemiesObservationSimulator.isInUnlockedEnemyViewOnly(position.x, position.y))
                    return true;
            }
        }
        return false;
    }

    public int getStrategyState2Int(){
        return this.getStrategyState().ordinal();
    }

    private void updateStrategyState(){
        try{
            int sizeOfPathNodes = 0;
            synchronized (this.pathNodes){
                sizeOfPathNodes = this.pathNodes.size();
            }
            if(!this.roboMaster.isAlive) this.strategyState = StrategyState.DEAD;
            if(this.roboMaster.numOfBullets == 0 && !BuffZone.AllyBulletSupplyBuffZone().isActive() && !BuffZone.AllyHPRecoveryBuffZone().isActive()){
                this.strategyState = StrategyState.ROTATING;
            }
            else if(BuffZone.isHPRecoveryNeeded != 0 || BuffZone.isBulletSupplyNeeded != 0 || BuffZone.isRedHPRecoveryNecessary != 0){
                this.strategyState = StrategyState.GETTINGBUFF;
            }
            else if((!Enemies.enemy1.isAlive() || (!Enemies.enemy1.isInTheView() && !Enemies.enemy1.isInitialized()))
                    && (!Enemies.enemy2.isAlive() || (!Enemies.enemy2.isInTheView() && !Enemies.enemy2.isInitialized())) ){
                this.strategyState = StrategyState.ATTACKING;
            }
            else if(EnemiesObservationSimulator.isInLockedEnemyViewOnly(this.roboMaster.getPointPosition().x, this.roboMaster.getPointPosition().y) ||
                    sizeOfPathNodes == 0){
                this.strategyState = StrategyState.ATTACKING;
            }
            else if(sizeOfPathNodes > 0 || BuffZone.isAnyAvailableBuffZone()){
                this.strategyState = StrategyState.MOVING;
            }
            else{
                this.strategyState = StrategyState.ATTACKING;
            }
        }
        catch (NullPointerException e){
            this.strategyState = StrategyState.MOVING;
        }
    }

    public StrategyState getStrategyState(){
        return this.strategyState;
    }

    public boolean isCollidingWithObstacle(int centreX, int centreY){
        for(int i=0;i<45;i++){
            for(int j=0;j<45;j++){
                int x = centreX + i - 23;
                int y = centreY + j - 23;
                if(!Systems.pointSimulator.isPointEmpty(x, y, getPointStatus())
                ){
                    return false;
                }
            }
        }
        return false;
    }

    public boolean isInLockedEnemyViewOnly(int x, int y){
        return EnemiesObservationSimulator.isInLockedEnemyViewOnly(x, y);
    }

    public Position getMinCostPosition(){
        return this.roboMaster.costMap.minPositionCost;
    }

    public boolean isOn(){
        return this.isStep;
    }

    public boolean isOn(boolean bool){
        this.isStep = bool;
        return this.isStep;
    }
}
