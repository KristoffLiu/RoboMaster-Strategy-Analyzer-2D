package com.kristoff.robomaster_simulator.robomasters.Strategy;

import com.badlogic.gdx.math.Vector2;
import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.robomasters.modules.CostMap;
import com.kristoff.robomaster_simulator.robomasters.Enemy;
import com.kristoff.robomaster_simulator.robomasters.Ally;
import com.kristoff.robomaster_simulator.systems.Systems;
import com.kristoff.robomaster_simulator.systems.buffs.BuffZone;
import com.kristoff.robomaster_simulator.systems.pointsimulator.PointState;
import com.kristoff.robomaster_simulator.teams.allies.Allies;
import com.kristoff.robomaster_simulator.teams.RoboMasters;
import com.kristoff.robomaster_simulator.teams.allies.enemyobservations.EnemiesObservationSimulator;
import com.kristoff.robomaster_simulator.systems.pointsimulator.PointSimulator;
import com.kristoff.robomaster_simulator.utils.LoopThread;
import com.kristoff.robomaster_simulator.utils.Position;
import org.lwjgl.Sys;

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

    public boolean turtleModeSwitch = false;
    public float turtleModeTimer = 0;
    public long startedTime = 0;


    public CounterState counterState = CounterState.TwoVSTwo;
    public StrategyState strategyState = StrategyState.INITIALIZED;

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
            switch(this.strategyState){
                case FAILED               -> {

                }
                case DEAD                 -> {}
                case INITIALIZED          -> {
                    if(shouldSetAsDead()) return;
                    if(shouldGetBuff()) return;
                    else if(this.roboMaster.numOfBullets > 55) this.strategyState = StrategyState.TURTLE_MODE;
                }
                case GETTING_BUFF         -> {
                    if(shouldSetAsDead()) return;
                    if(shouldStartToRotate()) return;
                    else if(shouldStartApproching()) return;
//                    if(ifBuffingFail()) return
                }
                case APPROACHING          -> {
                    if(shouldSetAsDead()) return;
                    if(isBuffAvailable()) return;
                    if(shouldStartAttacking()) return;
                }
                case ANDRE_ATTACKING_MODE -> {
                    if(shouldSetAsDead()) return;
                    if(isBuffAvailable()) return;
                    else if(!shouldStartAttacking()) this.strategyState = StrategyState.APPROACHING;
                    else if(shouldGoBack()) return;
                }
                case BACKING              -> {
                    if(shouldSetAsDead()) return;
                    if(shouldGetBuff()) return;
                    else if(this.roboMaster.numOfBullets > 55) this.strategyState = StrategyState.APPROACHING;
                }
                case TURTLE_MODE          -> {
                    if(shouldSetAsDead()) return;
                    if(!turtleModeSwitch)
                    {
                        this.strategyState = StrategyState.TURTLE_MODE;
                        turtleModeSwitch = true;
                        this.turtleModeTimer = 0;
                        this.startedTime = System.currentTimeMillis();
                    }
                    else if(this.turtleModeTimer > 30000){
                        turtleModeTimer = 0;
                        this.strategyState = StrategyState.APPROACHING;
                        System.out.println(this.turtleModeTimer);
                    }
                    else {
                        this.turtleModeTimer = System.currentTimeMillis() - this.startedTime;
                        System.out.println(this.turtleModeTimer);
                    }
                }
            }
        }
        catch (NullPointerException e){
            System.out.println(e.getMessage());
            this.strategyState = StrategyState.ANDRE_ATTACKING_MODE;
        }
    }

    private boolean shouldSetAsDead(){
        if(!this.roboMaster.isAlive){
            this.strategyState = StrategyState.DEAD;
            return true;
        }
        else return false;
    }

    private boolean shouldGetBuff(){
        if(isBuffAvailable()){
            this.strategyState = StrategyState.GETTING_BUFF;
            return true;
        }
        else return false;
    }

    public boolean isBuffAvailable(){
        return this.roboMaster.numOfBullets <= 50 && (BuffZone.isHPRecoveryNeeded != 0 || BuffZone.isBulletSupplyNeeded != 0);
    }

    private boolean shouldStartToRotate(){
        if(!isBuffAvailable()){
            this.strategyState = StrategyState.TURTLE_MODE;
            return true;
        }
        else return false;
    }

    private boolean shouldStartApproching(){
        if(!isBuffAvailable()){
            this.strategyState = StrategyState.APPROACHING;
            return true;
        }
        else return false;
    }

    private boolean ifBuffingFail(){
        if(BuffZone.isHPRecoveryNeeded == 0 && BuffZone.isBulletSupplyNeeded == 0 && BuffZone.isEnemyHPRecoveryNecessary == 0)
        {
            this.strategyState = StrategyState.TURTLE_MODE;
        }
        //still needs to be filled, if the car used 10S or some time still haven't get the buff
        //turn to turtle mode, until next new buffzones.
        return true;
    }

    private boolean shouldStartAttacking(){
        if(!EnemiesObservationSimulator.isOutOfBothEnemiesView(this.roboMaster.getPointPosition().getX(), this.roboMaster.getPointPosition().getY())){
            //in condition write if enemy is locked and the distance between is in th shoting range
            this.strategyState = StrategyState.ANDRE_ATTACKING_MODE;
            return true;
        }
        else return false;
    }

    private boolean shouldGoBack(){
        if(this.roboMaster.numOfBullets == 0 && !BuffZone.AllyBulletSupplyBuffZone().isActive()){
            this.strategyState = StrategyState.BACKING;
            return true;
        }
        else return false;
    }

    private boolean shouldBecomeTurtle(){
        if(this.roboMaster.getPointPosition().getX() > 778){
            //true when the car is back to initial point
            this.strategyState = StrategyState.TURTLE_MODE;
            return true;
        }
        else return false;
    }

    public StrategyState getStrategyState(){
        return this.strategyState;
    }

    public boolean isAttckingStarted(){
        return this.roboMaster.numOfBullets >= 100 && BuffZone.AllyHPRecoveryBuffZone().isActive();
    }

    public boolean isOutOfBullet(){
        return this.roboMaster.numOfBullets == 0 && BuffZone.AllyBulletSupplyBuffZone().isActive();
    }

    public boolean isReturned(){
        return isOutOfBullet();    }

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
