from env.rosanalyzer.RoboMaster import DetectionState, Enemy, Ally
import math

class Localization:
    def __init__(self, ally1 : Ally, ally2 : Ally, enemy1 : Enemy, enemy2 : Enemy):
        self.ally1 = ally1
        self.ally2 = ally2
        self.enemy1 = enemy1
        self.enemy2 = enemy2

    def allocateToEnemy(self, enemy : Enemy, position):
        enemy.setPosition(position.x, position.y, float(0))

    def inputSignal1(self, position):
        if self.allocateByVisionSingle(position):
            pass
        elif self.isOneEnemyInView():
            if self.allocateByOldPositionSingle(position):
                pass
        elif self.allocate(position):
            pass
        else:
            self.allocateToEnemy(self.enemy1, position) 

    def inputSignal2(self, position1, position2):
        if self.allocateByVision(position1, position2):
            pass
        elif self.isBothEnemiesInView():
            if self.allocateByOldPosition(position1, position2):
                pass
        elif self.isBothEnemiesLost() or self.isBothEnemiesInitialized():
            if self.allocateByDistance(position1, position2):
                pass
        else:
            self.allocateJustInOrder(position1, position2)

    def allocateJustInOrder(self, position1, position2):
        self.allocateToEnemy(self.enemy1, position1)
        self.allocateToEnemy(self.enemy2, position2)

    def allocateByOldPositionSingle(self, position):
        if self.enemy1.isOldPositionMatched(position.x, position.y):
            self.allocateToEnemy(self.enemy1, position)
            return True
        elif self.enemy2.isOldPositionMatched(position.x, position.y):
            self.allocateToEnemy(self.enemy2, position)
            return True
        else:
            return False

    def allocateByOldPosition(self, position1, position2):
        if self.enemy1.isOldPositionMatched(position1.x, position1.y):
            self.allocateToEnemy(self.enemy1, position1)
            self.allocateToEnemy(self.enemy2, position2)
            return True
        elif self.enemy2.isOldPositionMatched(position1.x, position1.y):
            self.allocateToEnemy(self.enemy2, position1)
            self.allocateToEnemy(self.enemy1, position2)
            return True
        elif self.enemy1.isOldPositionMatched(position2.x, position2.y):
            self.allocateToEnemy(self.enemy1, position2)
            self.allocateToEnemy(self.enemy2, position1)
            return True
        elif self.enemy2.isOldPositionMatched(position2.x, position2.y):
            self.allocateToEnemy(self.enemy2, position2)
            self.allocateToEnemy(self.enemy1, position1)
            return True
        else:
            return False

    def allocateByVisionSingle(self, position):
        if self.enemy1.isVisualPositionMatched(position.x, position.y) == 1:
            self.allocateToEnemy(self.enemy1, position)
            return True
        elif self.enemy2.isVisualPositionMatched(position.x, position.y) == 1:
            self.allocateToEnemy(self.enemy2, position)
            return True
        else:
            return False

    def allocateByVision(self, position1, position2):
        if self.enemy1.isVisualPositionMatched(position1.x, position1.y) == 1:
            self.allocateToEnemy(self.enemy1, position1)
            self.allocateToEnemy(self.enemy2, position2)
            return True
        elif self.enemy2.isVisualPositionMatched(position1.x, position1.y) == 1:
            self.allocateToEnemy(self.enemy2, position1)
            self.allocateToEnemy(self.enemy1, position2)
            return True
        elif self.enemy1.isVisualPositionMatched(position2.x, position2.y) == 1:
            self.allocateToEnemy(self.enemy1, position2)
            self.allocateToEnemy(self.enemy2, position1)
            return True
        elif self.enemy2.isVisualPositionMatched(position2.x, position2.y) == 1:
            self.allocateToEnemy(self.enemy2, position2)
            self.allocateToEnemy(self.enemy1, position1)
            return True
        else:
            return False

    def allocate(self, position):
        if self.enemy1.isLocked():
            self.allocateToEnemy(self.enemy1, position)
            return True
        elif self.enemy2.isLocked():
            self.allocateToEnemy(self.enemy2, position)
            return True
        else:
            return False

    def allocateByDistance(self, position1, position2):
        if self.enemy1.isLocked():
            if(position1.distanceTo(self.ally1.x, self.ally1.y) + position1.distanceTo(self.ally2.x, self.ally2.y) < position2.distanceTo(self.ally1.x, self.ally1.y) + position2.distanceTo(self.ally2.x, self.ally2.y)):
                self.allocateToEnemy(self.enemy1, position1)
                self.allocateToEnemy(self.enemy2, position2)
            else:
                self.allocateToEnemy(self.enemy1, position2)
                self.allocateToEnemy(self.enemy2, position1)
            return True
        elif self.enemy2.isLocked():
            if(position1.distanceTo(self.ally1.x, self.ally1.y) + position1.distanceTo(self.ally2.x, self.ally2.y) < position2.distanceTo(self.ally1.x, self.ally1.y) + position2.distanceTo(self.ally2.x, self.ally2.y)):
                self.allocateToEnemy(self.enemy2, position1)
                self.allocateToEnemy(self.enemy1, position2)
            else:
                self.allocateToEnemy(self.enemy2, position2)
                self.allocateToEnemy(self.enemy1, position1)
            return True
        else:
            return False

    def isBothEnemiesInView(self):
        return self.enemy1.detectionState == DetectionState.IN_VIEW and self.enemy2.detectionState == DetectionState.IN_VIEW

    def isBothEnemiesInitialized(self):
        return self.enemy1.detectionState == DetectionState.INITIALIZED and self.enemy2.detectionState == DetectionState.INITIALIZED

    def isBothEnemiesLost(self):
        return self.enemy1.detectionState == DetectionState.LOST and self.enemy2.detectionState == DetectionState.LOST

    def isOneEnemyInView(self):
        return self.enemy1.detectionState == DetectionState.IN_VIEW or self.enemy2.detectionState == DetectionState.IN_VIEW

    def isOneEnemyInitialized(self):
        return (self.enemy1.detectionState == DetectionState.INITIALIZED and self.enemy2.detectionState != DetectionState.INITIALIZED) or (self.enemy1.detectionState != DetectionState.INITIALIZED and self.enemy2.detectionState == DetectionState.INITIALIZED)


class Position:
    def __init__(self, x = 0, y = 0):
        self.x = x
        self.y = y

    def setPosition(self, x, y):
        self.x = x
        self.y = y

    def distanceTo(self, pos):
        return self.distanceTo(self, pos.x, pos.y)
    
    def distanceTo(self, x, y):
        return math.sqrt(math.pow(self.x - x,2) + math.pow(self.y - y,2))


