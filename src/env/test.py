import sys
import os
import time
sys.path.append(os.getcwd().rstrip("env"))
# os.path.abspath()
from env.rosanalyzer.Analyzer import Analyzer

if __name__ == '__main__':
    analyzer = Analyzer()
    
    analyzer.setTeamColor(1)
    # analyzer.updateBuffZone(0, 4, True)
    # analyzer.updateBuffZone(5, 2, True)

    # analyzer.updateBuffZone(2, 1, True)
    # analyzer.updateBuffZone(3, 3, True)

    # analyzer.updateBuffZone(1, 5, True)
    # analyzer.updateBuffZone(4, 6, True)

    analyzer.updateBuffZone(0, 4, True)
    analyzer.updateBuffZone(5, 2, True)

    analyzer.updateBuffZone(2, 1, True)
    analyzer.updateBuffZone(3, 3, True)

    analyzer.updateBuffZone(1, 5, True)
    analyzer.updateBuffZone(4, 6, True)

    # for i in range(2000):
    #     analyzer.allies1.setHealth(2000 - i)
    #     analyzer.allies2.setHealth(2000 - i)
    analyzer.enemy1.setHealth(1000)
    analyzer.enemy2.setHealth(1000)
    analyzer.ally1.setHealth(1000)
    analyzer.ally1.setNumOfBullets(100)
    analyzer.ally1.setHealth(1000)

    i = 0

    #analyzer.entrypoint.demo()

    a = True
    i = 0

    while(True):
        analyzer.displayOnce()
        analyzer.ally1.updateStrategyState()
        analyzer.ally2.updateStrategyState()
        analyzer.ally1.getDecisionPath()
        analyzer.ally2.getDecisionPath()
        
        if a:
            i += 0.16
        else:
            i -= 0.16
        analyzer.enemy2.setPosition(5.21, i, float(0))

        if a and i > 4:
            a = False
        if not a and i < 0.5:
            a = True
        time.sleep(0.01)

    analyzer.ally1.getDecisionPath()
    analyzer.ally2.getDecisionPath()
