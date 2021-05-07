import sys
import os
import time
sys.path.append(os.getcwd().rstrip("env"))
# os.path.abspath()
from env.rosanalyzer.Analyzer import Analyzer

if __name__ == '__main__':
    analyzer = Analyzer()
    analyzer.entrypoint.setAsRoamer("blue1")
    analyzer.updateBuffZone(0, 0, True)
    analyzer.updateBuffZone(1, 1, True)
    analyzer.updateBuffZone(2, 4, True)
    analyzer.updateBuffZone(3, 5, True)
    analyzer.updateBuffZone(4, 3, True)
    analyzer.updateBuffZone(5, 2, True)
