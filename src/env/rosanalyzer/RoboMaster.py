import math

class RoboMaster:
    x, y, yaw = 0.0, 0.0, 0.0
    old_goal_x, old_goal_y = 0.0, 0.0
    theta = 0.25

    def __init__(self, entrypoint, object):
        self._entrypoint = entrypoint
        self._object = object
        self.name = object.getName()
        self.x, self.y, self.yaw = 0, 0, 0
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
    
    def __str__(self):
        if self.isStrategyMakerOn:
            boolstr = "is working" + " - " + str(len(self.pathList)) + " points path"
            if len(self.pathList) > 0:
                destinationPoint = self.pathList[len(self.pathList) - 1]
                boolstr += "\n      Destination: " + "%.2f, %.2f, %2f" % (destinationPoint.x, destinationPoint.y, math.degrees(destinationPoint.yaw) )
        else:
            boolstr = "not working\n      Destination: None"
        string = "\n      StrategyMaker Status: {}".format(boolstr) 
        return super(Ally, self).__str__() + string
    
    def getDecisionMade(self):
        pos = self._object.getDecisionMade()
        return pos.getX() / 100.0, pos.getY() / 100.0

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
        
        return TowardsEnemyAtBothSides() if len(posList) > 0 else []

    
    
    def setStrategyMaker(self, bool):  
        self.isStrategyMakerOn = bool


class Enemy(RoboMaster):
    def __init__(self, entrypoint, enemyObject):
        super(Enemy, self).__init__(entrypoint, enemyObject)


class DecisionNode():
    def __init__(self, x = 0, y = 0, yaw = 0.0):
        self.x = x
        self.y = y
        self.yaw = yaw
    
    def yawAngle2Point(self, targetX, targetY):
        self.yaw = math.atan2(targetY - self.y, targetX - self.x)

    def __str__(self):
        return "{:f} {:f} {:f}".format(self.x, self.y, self.yaw)
    



