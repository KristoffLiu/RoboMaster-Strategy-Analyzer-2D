import math
from enum import Enum

class RoboMaster:
    x, y, yaw = 0.0, 0.0, 0.0
    old_goal_x, old_goal_y = 0.0, 0.0
    theta = 0.25

    def __init__(self, entrypoint, object):
        self._entrypoint = entrypoint
        self._object = object
        self.name = object.getName()
        self.no = int(self.name[len(self.name) - 1]) - 1 # index start from 0
        self.x, self.y, self.yaw = 0.0, 0.0, 0.0
        self.health = 2000
        self.numOfBullets = 0

    def setPosition(self, x, y, yaw):
        self.x, self.y, self.yaw = x, y, yaw
        radian = math.radians(self.yaw + 180)
        self._object.setPosition(int(x*1000), int(y*1000), radian)

    def setHealth(self, health):
        self.health = health
        self._object.setHealth(health)
    
    def setNumOfBullets(self, numOfBullets):
        self.numOfBullets = numOfBullets
        self._object.setNumOfBullets(numOfBullets)


    def __str__(self):
        return "   %s - [x]%.2f [y]%.2f [yaw]%.2f°" % (self.name, self.x, self.y, self.yaw) + self.display_health() + self.display_num_of_bullets()

    def display_health(self):
        str = "\n      HP: ["
        hpleft = self.health // 100
        for i in range(20):
            str += "|" if i <= hpleft else " "
        str += "] {:d} / 2000".format(self.health)
        return str

    def display_num_of_bullets(self):
        return "   Bullets: {:d} left".format(self.numOfBullets)
        
    def __eq__(self, other):
        return abs(self.x - other.x) < self.theta and abs(self.y - other.y) < self.theta
    def __sub__(self, other):
        return self.x - other.x, self.y - other.y

class Ally(RoboMaster):
    def __init__(self, entrypoint, allyObject):
        super(Ally, self).__init__(entrypoint, allyObject)
        self.strategyMaker = self._object.getStrategyMaker()
        self.isStrategyMakerOn = True
        self.pathList = []
        self.previousStrategyState = StrategyState.STATIC
        self.strategyState = StrategyState.STATIC
    
    def __str__(self):
        if self.isStrategyMakerOn:
            boolstr = "(→)" + " {}".format(self.strategyState) + " - " + str(len(self.pathList)) + " points path"
            if len(self.pathList) > 0:
                destinationPoint = self.pathList[len(self.pathList) - 1]
                boolstr += "\n      Destination: " + "%.2f, %.2f, %2f" % (destinationPoint.x, destinationPoint.y, math.degrees(destinationPoint.yaw) )
        else:
            boolstr = "(x) \n      Destination: None"
        string = "\n      Strategy State: {}".format(boolstr) 
        return super(Ally, self).__str__() + string
    
    def getDecisionMade(self):
        pos = self._object.getDecisionMade()
        return pos.getX() / 100.0, pos.getY() / 100.0

    def updateStrategyState(self):
        self.strategyState = StrategyState(self._object.getStrategyState())

    def getDecisionPath(self):
        posList = self._object.getPath()

        def TowardsEnemyOnlyAtLast():
            list = []
            last = DecisionNode()
            for i in posList:
                temp = DecisionNode(i.getX() / 100.0, i.getY() / 100.0)
                last.yawAngle2Point(temp.x, temp.y)
                list.append(temp)
                last = temp
            enemy = self._entrypoint.getLockedEnemy()
            enemyPosition = enemy.getPointPosition()
            gx = enemyPosition.getX() / 100.0
            gy = enemyPosition.getY() / 100.0
            last.yawAngle2Point(gx, gy)
            self.pathList = list
            return self.pathList

        def TowardsEnemyAtBothSides():
            list = []
            last = DecisionNode()
            enemy = self._entrypoint.getLockedEnemy()
            enemyPosition = enemy.getPointPosition()
            gx = enemyPosition.getX() / 100.0
            gy = enemyPosition.getY() / 100.0
            for i in posList:
                temp = DecisionNode(i.getX() / 100.0, i.getY() / 100.0)
                last.yawAngle2Point(temp.x, temp.y)
                list.append(temp)
                last = temp
            first = list[0]
            first.yawAngle2Point(gx, gy)
            last.yawAngle2Point(gx, gy)
            self.pathList = list
            return self.pathList

        def AlwaysTowardsEnemy():
            list = []
            last = DecisionNode()
            enemy = self._entrypoint.getLockedEnemy()
            enemyPosition = enemy.getPointPosition()
            gx = enemyPosition.getX() / 100.0
            gy = enemyPosition.getY() / 100.0
            for i in posList:
                temp = DecisionNode(i.getX() / 100.0, i.getY() / 100.0)
                last.yawAngle2Point(gx, gy)
                list.append(temp)
                last = temp
            last.yawAngle2Point(gx, gy)
            self.pathList = list
            return self.pathList

        def JustTowardsEnemy():
            currentNode = DecisionNode(self.x, self.y)
            enemy = self._entrypoint.getLockedEnemy()
            enemyPosition = enemy.getPointPosition()
            gx = enemyPosition.getX() / 100.0
            gy = enemyPosition.getY() / 100.0
            currentNode.yawAngle2Point(gx, gy)
            self.pathList = [currentNode]
            return self.pathList

        return AlwaysTowardsEnemy() if len(posList) > 0 else JustTowardsEnemy()

    def setStrategyMaker(self, bool):  
        self.isStrategyMakerOn = bool


class Enemy(RoboMaster):
    def __init__(self, entrypoint, enemyObject):
        self.visualX = -1
        self.visualY = -1
        self.visualTimeStamp = 0
        self.detectionState = DetectionState.INITIALIZED

        super(Enemy, self).__init__(entrypoint, enemyObject)

    def __str__(self):
        string = "\n      Visual Localization: " + "%.2f, %.2f, %.1f" % (self.visualX, self.visualY, self.visualTimeStamp)
        return super(Enemy, self).__str__() + string

    def setVisualPosition(self, x, y):
        self.visualX = x
        self.visualY = y
        self.setVisualTimeStamp(0)

    def setVisualTimeStamp(self, time):
        self.visualTimeStamp = time

    def increaseVisualTimeStamp(self, time):
        self.visualTimeStamp += time

    def isVisualPositionMatched(self, x, y):
        if self.visualX == -1 or self.visualY == -1:
            return -1
        if self.visualTimeStamp < 1.5 and abs(self.visualX - x) < 1 and abs(self.visualY - y) < 1:
            return 1
        else:
            return 0

    def isOldPositionMatched(self, x, y):
        return (math.sqrt(math.pow(self.x - x,2) + math.pow(self.y - y,2))) < 0.3

    def isLocked(self):
        return self._object.isLocked()

    def updateDetectionState(self):
        self._object.getDetectionState

    def setIfVisualPositionMatched(self, x, y, yaw):
        if self.visualX == -1 or self.visualY == -1:
            return -1
        if abs(self.visualX - x) < 0.5 and abs(self.visualY - y) < 0.5:
            self.setPosition(x, y, yaw)
            return 1
        else:
            return 0

class DecisionNode():
    def __init__(self, x = 0, y = 0, yaw = 0.0):
        self.x = x
        self.y = y
        self.yaw = yaw
    
    def yawAngle2Point(self, targetX, targetY):
        self.yaw = math.atan2(targetY - self.y, targetX - self.x)

    def __str__(self):
        return "{:f} {:f} {:f}".format(self.x, self.y, self.yaw)

class StrategyState(Enum):
    ERROR = -1
    NOTWORKING = 0
    DEAD = 1
    STATIC = 2
    MOVING = 3
    ATTACKING = 4
    GETTINGBUFF = 5
    ROTATING = 6
    PATROLLING = 7
    def __str__(self) -> str:
        if(self.value == 0):
            return "Not Working At All"
        elif(self.value == 1):
            return "No strategy because its dead"
        elif(self.value == 2):
            return "Static"
        elif(self.value == 3):
            return "Moving"
        elif(self.value == 4):
            return "Attacking(Andrew Spinning Mode)"
        elif(self.value == 5):
            return "Getting Buff"
        elif(self.value == 6):
            return "Rotating"
        elif(self.value == 7):
            return "Patrolling"
        else:
            return "Error Raised"

class DetectionState(Enum):
    ERROR = -1
    INITIALIZED = 0
    LOST = 1
    IN_VIEW = 2
    GUESSING = 3

    def __str__(self) -> str:
        if(self.value == 0):
            return "Not Working At All"
        elif(self.value == 1):
            return "No strategy because its dead"
        elif(self.value == 2):
            return "Static"
        elif(self.value == 3):
            return "Moving"
        elif(self.value == 4):
            return "Attacking(Spinning Mode)"
        elif(self.value == 5):
            return "Getting Buff"
        elif(self.value == 6):
            return "Rotating"
        elif(self.value == 7):
            return "Patrolling"
        else:
            return "Error Raised"
    



