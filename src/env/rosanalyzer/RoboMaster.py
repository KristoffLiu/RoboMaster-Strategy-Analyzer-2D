class RoboMaster:
    x, y, yaw = 0.0, 0.0, 0.0
    old_goal_x, old_goal_y = 0.0, 0.0
    theta = 0.25
    def __init__(self, object):
        self._object = object
        self.name = object.getName()
        self.x, self.y, self.yaw = 0, 0, 0
        self.health = 2000
        self.numOfBullets = 0

    def setPosition(self, x, y, yaw):
        self.x, self.y, self.yaw = x, y, yaw
        self._object.setPosition(int(x*1000), int(y*1000), yaw)

    def setHealth(self, health):
        self.health = health
    
    def setNumOfBullets(self, numOfBullets):
        self.numOfBullets = numOfBullets

    def __str__(self):
        return "   %s - position: %.2f, %.2f" % (self.name, self.x, self.y) + self.display_health() + self.display_num_of_bullets()

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

class Allies(RoboMaster):
    def __init__(self, alliesObject):
        super(Allies, self).__init__(alliesObject)
        self.strategyMaker = self._object.getStrategyMaker()
        self.isStrategyMakerOn = self.isStrategyMakerOn()
    
    def __str__(self):
        boolstr = "is working" if self.isStrategyMakerOn else {"not working"}
        str = "\n      StrategyMaker Status: {}".format(boolstr)
        return super(Allies, self).__str__() + str
    
    def getNextPosition(self):
        pos = self.Blue1.getDecisionMade()
        rx = pos.getX() / 100.0
        ry = pos.getY() / 100.0
    
    def isStrategyMakerOn(self):
        return self.strategyMaker.isOn()
    
    def setStrategyMaker(self, bool):
        return self.strategyMaker.isOn(bool)


class Enemy(RoboMaster):
    def __init__(self, enemyObject):
        super(Enemy, self).__init__(enemyObject)


