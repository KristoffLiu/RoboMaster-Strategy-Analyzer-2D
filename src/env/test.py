import sys
import os
import time
sys.path.append(os.getcwd().rstrip("env"))
# os.path.abspath()
from env.rosanalyzer.Analyzer import Analyzer

if __name__ == '__main__':
    analyzer = Analyzer()
    analyzer.display()
    analyzer.updateBuffZone(0, 4, False)
    analyzer.updateBuffZone(5, 2, False)

    analyzer.updateBuffZone(2, 1, False)
    analyzer.updateBuffZone(3, 3, False)

    analyzer.updateBuffZone(1, 5, False)
    analyzer.updateBuffZone(4, 6, False)

    # for i in range(2000):
    #     analyzer.allies1.setHealth(2000 - i)
    #     analyzer.allies2.setHealth(2000 - i)
    analyzer.enemy1.setHealth(1000)
    analyzer.enemy2.setHealth(1000)
    analyzer.ally1.setHealth(1000)
    analyzer.ally1.setHealth(1000)

    analyzer.ally1.getDecisionPath()
    analyzer.ally2.getDecisionPath()
