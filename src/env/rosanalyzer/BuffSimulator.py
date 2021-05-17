import random
import copy
from env.rosanalyzer.Analyzer import Analyzer

class BuffSimulator():
    def __init__(self, analyzer: Analyzer):
        self.analyzer = analyzer
        self.buff_list = [[1,3],[2,4],[5,6]]
        pass

    def generateNewBuffs(self):
        list = copy.deepcopy(self.buff_list)
        for sublist in list:
            random.shuffle(sublist)
        random.shuffle(list)
        
    



    # analyzer.updateBuffZone(0, 6, True)
    # analyzer.updateBuffZone(5, 5, True)

    # analyzer.updateBuffZone(2, 2, True)
    # analyzer.updateBuffZone(3, 4, True)

    # analyzer.updateBuffZone(1, 1, True)
    # analyzer.updateBuffZone(4, 3, True)

