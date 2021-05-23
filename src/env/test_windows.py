import sys
import os
import time
sys.path.append(os.getcwd().rstrip("env"))
# os.path.abspath()
from env.rosanalyzer.Analyzer import Analyzer

if __name__ == '__main__':
    analyzer = Analyzer()
    analyzer.displayWindows()
    analyzer.updateBuffZone(0, 6, True)
    analyzer.updateBuffZone(5, 5, True)

    analyzer.updateBuffZone(2, 4, True)
    analyzer.updateBuffZone(3, 2, True)

    analyzer.updateBuffZone(1, 3, True)
    analyzer.updateBuffZone(4, 1, True)

    analyzer.enemy1.setHealth(1000)
    analyzer.enemy2.setHealth(1000)
    analyzer.ally1.setHealth(1000)
    analyzer.ally2.setHealth(1000)
    analyzer.ally1.getDecisionPath()
    analyzer.ally2.getDecisionPath()
