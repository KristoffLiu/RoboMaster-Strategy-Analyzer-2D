package com.kristoff.robomaster_simulator.robomasters;

import com.badlogic.gdx.math.Vector2;
import com.kristoff.robomaster_simulator.core.Simulator;
import com.kristoff.robomaster_simulator.robomasters.Strategy.StrategyMaker;
import com.kristoff.robomaster_simulator.systems.Systems;
import com.kristoff.robomaster_simulator.systems.pointsimulator.PointSimulator;
import com.kristoff.robomaster_simulator.systems.pointsimulator.PointState;
import com.kristoff.robomaster_simulator.teams.Team;
import com.kristoff.robomaster_simulator.teams.RoboMasters;
import com.kristoff.robomaster_simulator.robomasters.modules.*;
import com.kristoff.robomaster_simulator.systems.pointsimulator.StatePoint;
import com.kristoff.robomaster_simulator.utils.Position;

import java.util.List;

/***
 * //    private double weight = 17.1;           //Kg
 * //    private int max_forward_speed = 3;      //m/s
 * //    private int max_cross_range_speed = 2;  //m/s
 * //    private float shooting_speed = 6;       //per second
 * //    private float cannon_range = 180;       //degree
 * //    private float bullet_speed = 25;        //m/s
 * //    private int max_carrying_bullet = 300;  //m/s
 */
public abstract class RoboMaster {
    public int PIN;                                             //PIN
    public Team team;                                           //归属队伍 team
    public boolean isAlive = true;
    public TeamColor teamColor;

    public Property                  property;                  //基本属性 Basic Property
    public Actor                     actor;                     //行为器 Acting System
    public RMPhysicalSimulation      RMPhysicalSimulation;      //主体的2d物理建模 Physical Modelling
    public Weapon                    weapon;                    //武器 Weapon
    public Renderer                  renderer;                  //渲染器 Renderer
    public LidarObservation          lidarObservation;          //激光雷达Lidar发生器 Lidar Sensor Simulator
    public Dynamics                  dynamics;                  //动力系统 Dynamic System
    public StrategyMaker             strategyMaker;             //决策器 Decision System
    public CostMap                   costMap;                   //决策器 Decision System

    public String name;
    public int No;
    public int teamIndex;
    public int health;
    public int numOfBullets;
    public PointState pointState;

    /***
     * Constructor 构造器
     * @param textureRegionPath File path of the texture
     * @param team              Team
     * @param name              Name
     */
    public RoboMaster(String textureRegionPath, Team team, String name) {
        this.team = team;
        this.name = name;
        this.teamColor = TeamColor.BLUE;
        No = this.team == RoboMasters.allies ? RoboMasters.allies.size() + 1 : (3 + RoboMasters.enemies.size());
        teamIndex = this.team == RoboMasters.allies ? RoboMasters.allies.size() : RoboMasters.enemies.size();

        switch (this.No){
            case 0 -> pointState = PointState.Ally1;
            case 1 -> pointState = PointState.Ally2;
            case 2 -> pointState = PointState.Enemy1;
            case 3 -> pointState = PointState.Enemy2;
        }

        property = new Property();
        actor = new Actor(this);
        renderer = new Renderer(textureRegionPath,this);
        weapon = new Weapon(this);
        lidarObservation = new LidarObservation(this);

        if(this.team == RoboMasters.allies){
            costMap = new CostMap(this);
            strategyMaker = new StrategyMaker(this);
        }

        //pathPlanning = new PathPlanning(this.enemiesObservationSimulator.matrix, this);

        switch (Simulator.current.config.mode){
            case simulator,simulatorRLlib ->{
                RMPhysicalSimulation = new RMPhysicalSimulation(this);
                dynamics = new Dynamics(this);
            }
            case realMachine -> {}
        }
        this.health = property.health;
        this.numOfBullets = 0;
    }

    public void setTeamColor(TeamColor color){
        this.teamColor = color;
    }

    public String getTeamColor(){
        switch (this.teamColor){
            case RED -> {
                return "Red" + (this.No <= 2 ? this.No : this.No / 2);
            }
            case BLUE -> {
                return "Blue" + (this.No <= 2 ? this.No : this.No / 2);
            }
        }
        return null;
    }

    public void start(){
        switch (Simulator.current.config.mode){
            case realMachine -> {

            }
            case simulator, simulatorRLlib -> {
                this.RMPhysicalSimulation.start();
                this.dynamics.start();
            }
        }
        if(this.name.contains("Blue1")){

        }
        this.lidarObservation.start();
        this.renderer.start();
        this.actor.startToFormMatrix();
    }

    //API
    public float getRotation() {
        return this.actor.rotation;
    }

    public void setPosition(int x, int y) {
        for(RoboMaster roboMaster : RoboMasters.all){
            if(this != roboMaster){
                if(roboMaster.getPointPosition().distanceTo(x / 10,y / 10) < 50){
                    return;
                }
            }
        }
        if(Systems.pointSimulator.isPointTheObstacle(x / 10,y / 10)) return;
        this.actor.update(x, y);
    }

    public void setPosition(int x, int y, float rotation) {
//        for(RoboMaster roboMaster : RoboMasters.all){
//            if(this != roboMaster){
//                if(roboMaster.getPointPosition().distanceTo(x / 10,y / 10) < 50){
//                    return;
//                }
//            }
//        }
        if(!PointSimulator.isPointInsideMap(x / 10,y / 10)) return;
        if(Systems.pointSimulator.isPointTheObstacle(x / 10,y / 10)) return;
        this.actor.update(x, y, rotation);
    }

    public Position getPosition() {
        return new Position(this.actor.x,this.actor.y);
    }

    public int getX() {
        return this.actor.x;
    }

    public int getY() {
        return this.actor.y;
    }

    public Position getLidarPosition() {
        return getPosition();
    }

    public float getFacingAngle() {
        return this.actor.getFacingAngle();
    }

    public float getCannonAngle() {
        return this.actor.getCannonAngle();
    }

    public List<StatePoint> getLidarObservation(){
        return this.lidarObservation.others;
    }

    public Vector2 getLinearVelocity() {
        return this.RMPhysicalSimulation.body.getLinearVelocity();
    }

    public float getAngularVelocity() {
        return this.RMPhysicalSimulation.body.getAngularVelocity();
    }

    public boolean isAlive(){
        return isAlive;
    }

    public Position getDecisionMade(){
        return this.strategyMaker.getDecisionMade();
    }
    public List<Position> getPath(){
        return this.strategyMaker.getPath();
    }

    public Position getPointPosition(){return new Position(this.actor.x / 10,this.actor.y / 10);};

    public void die(){
        this.isAlive = false;
    }

    public void setHealth(int value){
        this.health = value > 0 ? value : 0;
        if(this.health <= 0)
            this.isAlive = false;
        else this.isAlive = true;
    }

    public void loseHealth(int healthLost){
        this.health = this.health > 0 ? health - healthLost : 0;
        if(this.health <= 0) this.isAlive = false;
    }

    public int getHealth(){
        return this.health;
    }

    public float getHealthPercent(){
        return (float)this.getHealth() / (float)this.property.health;
    }

    public int[][] getCostMap(){
        return costMap.getCostMap();
    }
    public int getCost(int x, int y){
        return costMap.getCost(x, y);
    }

    public StrategyMaker getStrategyMaker(){
        return this.strategyMaker;
    }

    public String getName() {
        return name;
    }

    public void setNumOfBullets(int bulletnum){
        this.numOfBullets = bulletnum;
    }

    public int getStrategyState(){
        return this.strategyMaker.getStrategyState2Int();
    }
}

