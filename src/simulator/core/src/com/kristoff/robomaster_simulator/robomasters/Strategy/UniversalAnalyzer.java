package com.kristoff.robomaster_simulator.robomasters.Strategy;

import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.robomasters.Strategy.gradientdescent.GradientDescentAnalyzer;
import com.kristoff.robomaster_simulator.robomasters.modules.CostMap;
import com.kristoff.robomaster_simulator.robomasters.modules.Property;
import com.kristoff.robomaster_simulator.systems.costmap.UniversalCostMap;
import com.kristoff.robomaster_simulator.utils.Position;

import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

public class UniversalAnalyzer implements StrategyAnalyzer {
    public RoboMaster roboMaster;
    public StrategyMaker strategyMaker;

    public SearchNode rootNode;
    public Queue<SearchNode> queue;
    public SearchNode resultNode;
    public CopyOnWriteArrayList<SearchNode>                   resultNodes;
    public CopyOnWriteArrayList<SearchNode>                   pathNodes;
    public GradientDescentAnalyzer gradientDescentAnalyzer;

    public UniversalAnalyzer(StrategyMaker strategyMaker){
        this.roboMaster = strategyMaker.roboMaster;
        this.strategyMaker = strategyMaker;

        this.queue                      = this.strategyMaker.queue;
        this.resultNodes                = new CopyOnWriteArrayList<>();
        this.pathNodes                  = new CopyOnWriteArrayList<>();
    }

    public boolean isTheCentreAvailable(Position centrePosition){
        return getCostMap().getCost(centrePosition.x, centrePosition.y) < 50;
    }

    public boolean isTheSurroundingAreaAvailable(Position centrePosition){
        int centreCost = getCostMap().getCost(centrePosition.x, centrePosition.y);
        for(int i = 0; i < Property.widthUnit ; i++){
            for(int j = 0; j < Property.heightUnit ; j++){
                int x = centrePosition.x + i - Property.widthUnit / 2;
                int y = centrePosition.y + j - Property.heightUnit / 2;
                if(   !(x>=0 && x<849)
                        || !(y>=0 && y<489)
                        || Math.abs(centreCost - getCostMap().getCost(x, y)) > 30
//                        || Math.abs(CostMapGenerator.getCost(x, y) - centreCost) > 10
                ){
                    return false;
                }
            }
        }
        return true;
    }

    //检查节点可访问性
    public boolean hasThisNodeNotBeenVisited(int x, int y, boolean[][] nodeGrid){
        if(x>=0 && x<849 && y>=0 && y<489){
            if(nodeGrid[x][y]){
                return false;
            }
            else {
                nodeGrid[x][y] = true;
                return true;
            }
        }
        else {
            return true;
        }
    }

    //检查节点可访问性
    public boolean hasThisNodeBeenVisited(int x, int y, boolean[][] nodeGrid){
        if(x>=0 && x<849 && y>=0 && y<489){
            return nodeGrid[x][y];
        }
        else {
            return false;
        }
    }

    public void setNodeHasBeenVisited(SearchNode node, boolean[][] nodeGrid){
        setNodeHasBeenVisited(node.position.x, node.position.y, nodeGrid);
    }

    public void setNodeHasBeenVisited(int x, int y, boolean[][] nodeGrid){
        nodeGrid[x][y] = true;
    }

    @Override
    public void analyze(TacticState tacticState) {

    }

    public CostMap getCostMap(){
        return this.roboMaster.costMap;
    }
}
