import sys
import os
import time

sys.path.append(os.getcwd().rstrip("env"))

from env.rosanalyzer.Localization import Position
# os.path.abspath()
from env.rosanalyzer.Analyzer import Analyzer

if __name__ == '__main__':
    analyzer = Analyzer()
    
    analyzer.setTeamColor(0)
    # analyzer.updateBuffZone(0, 4, True)
    # analyzer.updateBuffZone(5, 2, True)

    # analyzer.updateBuffZone(2, 1, True)
    # analyzer.updateBuffZone(3, 3, True)

    # analyzer.updateBuffZone(1, 5, True)
    # analyzer.updateBuffZone(4, 6, True)

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
        analyzer.enemy1.updateDetectionState()
        analyzer.enemy2.updateDetectionState()

        
        # if a:
        #     i += 0.16
        # else:
        #     i -= 0.16
        # pos = Position(5.21, i)
        # pos1 = Position(0.5, 4.0)

        # analyzer.localizationFilter.inputSignal2(position1 = pos, position2=pos1)
        # analyzer.enemy2.setPosition(5.21, i, float(0))

        if a and i > 4:
            a = False
        if not a and i < 0.5:
            a = True
        time.sleep(0.01)

    analyzer.ally1.getDecisionPath()
    analyzer.ally2.getDecisionPath()
