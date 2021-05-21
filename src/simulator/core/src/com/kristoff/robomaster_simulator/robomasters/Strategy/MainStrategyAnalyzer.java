package com.kristoff.robomaster_simulator.robomasters.Strategy;

import com.kristoff.robomaster_simulator.robomasters.RoboMaster;
import com.kristoff.robomaster_simulator.robomasters.Strategy.gradientdescent.GradientDescentAnalyzer;
import com.kristoff.robomaster_simulator.robomasters.modules.CostMap;
import com.kristoff.robomaster_simulator.robomasters.modules.Property;
import com.kristoff.robomaster_simulator.systems.costmap.PositionCost;
import com.kristoff.robomaster_simulator.systems.costmap.UniversalCostMap;
import com.kristoff.robomaster_simulator.systems.pointsimulator.PointSimulator;
import com.kristoff.robomaster_simulator.teams.Team;
import com.kristoff.robomaster_simulator.utils.Position;
import org.w3c.dom.Node;

import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainStrategyAnalyzer implements StrategyAnalyzer {
    public RoboMaster roboMaster;
    public StrategyMaker strategyMaker;

    public SearchNode rootNode;
    public Queue<SearchNode> queue;
    public SearchNode resultNode;
    public CopyOnWriteArrayList<SearchNode>                   resultNodes;
    public CopyOnWriteArrayList<SearchNode>                   pathNodes;
    public GradientDescentAnalyzer gradientDescentAnalyzer;

    public MainStrategyAnalyzer(StrategyMaker strategyMaker){
        this.roboMaster = strategyMaker.roboMaster;
        this.strategyMaker = strategyMaker;

        this.queue                      = this.strategyMaker.queue;
        this.resultNodes                = new CopyOnWriteArrayList<>();
        this.pathNodes                  = new CopyOnWriteArrayList<>();
        resultNode = new SearchNode();
    }

    public void analyze(TacticState tacticState) {
        Position currentPosition = strategyMaker.getCurrentPosition();
        if(shouldIMove(currentPosition.x, currentPosition.y)){
            scanMap(currentPosition);
        }
        else {
            pathNodes.clear();
            return;
        }
    }

    public boolean shouldIMove(int x, int y){
        //return !strategyMaker.isSafeNow(x, y);
        return true;
    }

    public void scanMap(Position currentPosition){
        //boolean[][] tempVisitedGrid = new boolean[849][489];
        boolean[][] tempVisitedGrid = strategyMaker.visitedGrid;

        PositionCost target = getCostMap().minPositionCost;
        int targetCost = target.cost;
        resultNode = new SearchNode();

        boolean is_find = false;
        while(!is_find){
            queue.clear();
            tempVisitedGrid = new boolean[849][489];

            this.rootNode = new SearchNode(
                    currentPosition.x,
                    currentPosition.y,
                    -1,
                    getCostMap().getCost(currentPosition.x, currentPosition.y),
                    null);
            this.resultNode = rootNode;

            queue.offer(rootNode);
            tempVisitedGrid[rootNode.position.x][rootNode.position.y] = true;

            while (!this.queue.isEmpty()){
                resultNode = this.queue.poll();
                if(isAvailable(resultNode.position, targetCost, target)) {
                    break;
                }
                generateChildrenNodes(resultNode, tempVisitedGrid);
                setNodeHasBeenVisited(resultNode, tempVisitedGrid);
            }
            if(Math.abs(targetCost - getCostMap().getCost(resultNode.position.getX(),resultNode.position.getY())) >= 50){
                targetCost += 50;
            }
            else {
                is_find = true;
            }
        }
        SearchNode node = resultNode;
        pathNodes.clear();
        while (true && node.parentNode != null){
            pathNodes.add(node);
            node = node.parentNode;
        }
        this.strategyMaker.update(resultNode, tempVisitedGrid, resultNodes, pathNodes);
    }

    public boolean isAvailable(Position centre, int targetCost, Position target){
        if(Math.abs(getCostMap().getCost(centre.getX(), centre.getY()) - targetCost) < 50 ||
                centre.x == target.x && centre.y == target.y){
            return true;
            //isTheSurroundingAreaAvailable(centre);
        }
        return false;
    }

    //查找并生成子节点，并返回队列对象
    public void generateChildrenNodes(SearchNode node, boolean[][] visitedGrid){
        if(!PointSimulator.isPointInsideMap(node.position.x, node.position.y)) return;
        visitedGrid[node.position.x][node.position.y] = true;
        for(int i=0; i < SearchNode.childrenNodesFindingCost.length; i++){
            int x = node.position.x + SearchNode.childrenNodesFindingCost[i][0] ;
            int y = node.position.y + SearchNode.childrenNodesFindingCost[i][1] ;
            double currentCost = getCostMap().getCost(node.position.x, node.position.y);
            double nextCost = getCostMap().getCost(x, y);
            double delta = nextCost - currentCost;
            double stepCost = Math.sqrt(SearchNode.childrenNodesFindingCost[i][2]);
            double totalCost = node.cost + delta + stepCost;
            if(!hasThisNodeBeenVisited(x, y, visitedGrid) ){
                SearchNode childNode = new SearchNode(x,y,node.index + 1, totalCost,node);
                boolean shouldSkip = false;
                for (SearchNode nodeInQueue : queue){
                    if(nodeInQueue.isInSamePosition(childNode)){
                        if(nodeInQueue.cost > childNode.cost){
                            nodeInQueue.cost = childNode.cost;
                            nodeInQueue.index = childNode.index;
                            nodeInQueue.parentNode = childNode.parentNode;
                        }
                        shouldSkip = true;
                    }
                }
                if(shouldSkip){
                    continue;
                }
                if(currentCost > 400 || nextCost < 400){
                    node.childrenNodes.add(childNode);
                    queue.offer(childNode);
                }
            }
        }
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

    public CostMap getCostMap(){
        return this.roboMaster.costMap;
    }

}
