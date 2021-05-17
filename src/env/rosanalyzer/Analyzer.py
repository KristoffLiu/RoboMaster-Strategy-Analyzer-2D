"""
"""

from py4j.java_gateway import JavaGateway
from py4j.java_gateway import java_import

from env.rosanalyzer.RoboMaster import Allies
from env.rosanalyzer.RoboMaster import Enemy
from enum import Enum
from threading import Timer
import sys
import os

class Analyzer:
    def __init__(self):
        self.gateway = JavaGateway() #启动py4j服务器
        self.entrypoint = self.gateway.entry_point #获取服务器桥的入口
        java_import(self.gateway.jvm,'java.util.*') #导入java中的类的方法

        self.game_status = self.GameStatus.INITIALIZE
        self.remaining_time = 0

        self.entrypoint.setAsRoamer("blue1")
        self.entrypoint.isOurTeamBlue(True)

        #self.allies1 = Allies(self.entrypoint.getAllies(0))
        #self.allies2 = Allies(self.entrypoint.getAllies(1))
        #self.enemy1 = Enemy(self.entrypoint.getEnemy(0))
        #self.enemy2 = Enemy(self.entrypoint.getEnemy(1))
        self.allies1 = Allies(self.entrypoint.getRoboMaster("Blue1"), self.entrypoint)
        self.allies2 = Allies(self.entrypoint.getRoboMaster("Blue2"), self.entrypoint)
        self.enemy1 = Enemy(self.entrypoint.getEnemy("Red1"), self.entrypoint)
        self.enemy2 = Enemy(self.entrypoint.getEnemy("Red2"), self.entrypoint)
        self.ally1 = Allies(self.entrypoint.getRoboMaster("Ally1"))
        self.ally2 = Allies(self.entrypoint.getRoboMaster("Ally2"))
        self.enemy1 = Enemy(self.entrypoint.getEnemy("Enemy2"))
        self.enemy2 = Enemy(self.entrypoint.getEnemy("Enemy2"))

        self.buff_zones = [self.BuffZone(i, self.BuffZone.BuffType.UNKNOWN, False) for i in range(6)]

    def updateGameStatus(self, game_status, remaining_time):
        self.game_status = self.GameStatus(game_status)
        self.remaining_time = remaining_time
        self.entrypoint.updateRemainingTime(remaining_time)

    def updateBuffZone(self, i, type, is_active):
        self.buff_zones[i].setBuffZone(type, is_active)
        self.entrypoint.updateBuffZone(i, type, is_active)
        
    def __str__(self) -> str:
        pass

    def displayWindows(self):
        def displayInfo():
            os.system('cls')
            self.display_title()
            self.display_game_status()
            self.display_robo_status()
            self.display_buff_zones()
            global tr
            tr = Timer(0.5,displayInfo)
            tr.start()

        tr = Timer(1,displayInfo)
        tr.start()


    def display(self):
        def displayInfo():
            os.system('clear')
            self.display_title()
            self.display_game_status()
            self.display_robo_status()
            self.display_buff_zones()
            global tr
            tr = Timer(0.5,displayInfo)
            tr.start()

        tr = Timer(1,displayInfo)
        tr.start()

    def displayOnce(self):
        os.system('clear')
        self.display_title()
        self.display_game_status()
        self.display_robo_status()
        self.display_buff_zones()

    def display_title(self):
        print("RoboMaster 分析器, 版本 v{}".format("1.51"))
    
    def display_game_status(self):
        print("Game Status: {}".format(self.game_status))
        if(self.game_status == self.GameStatus.GAME):
            timeleft = self.remaining_time / 180
            str = "["
            for i in range(30):
                str += "|" if i/30 <= timeleft else " "
            str += "] 比赛还剩 {:d} 分 {:d} 秒".format(self.remaining_time // 60, self.remaining_time % 60)
            print(str)
        print("")

    
    def display_buff_zones(self):
        print("Buff Zone Status:")
        print("                  {}".format(self.buff_zones[2]))
        print("{}          {}".format(self.buff_zones[0],self.buff_zones[4]))
        print("       {}          {}".format(self.buff_zones[1],self.buff_zones[5]))
        print("                  {}".format(self.buff_zones[3]))
        print("")

    def display_robo_status(self):
        print("Allies Status:")
        print("{}".format(self.ally1))
        print("{}".format(self.ally2))
        print("")
        print("Enemies Status:")
        print("{}".format(self.enemy1))
        print("{}".format(self.enemy2))
        print("")


    class GameStatus(Enum):
        READY = 0
        PREPARATION = 1
        INITIALIZE = 2
        FIVE_SEC_CD = 3
        GAME = 4
        END = 5
        def __str__(self) -> str:
            if(self.value == 0):
                return "READY TO GO 准备开始"
            elif(self.value == 1):
                return "IN PREPARATION 准备阶段"
            elif(self.value == 2):
                return "INITIALIZE 初始阶段"
            elif(self.value == 3):
                return "FIVE_SEC_CD 开始前五秒阶段"
            elif(self.value == 4):
                return "GAME 正在游戏"
            elif(self.value == 5):
                return "END 已结束"

    class BuffZone():
        class BuffZoneID(Enum):
            LEFT_UP = 0
            LEFT_DOWN = 1
            CENTER_UP = 2
            CENTER_DOWN = 3
            RIGHT_UP = 4
            RIGHT_DOWN = 5

        class BuffType(Enum):
            UNKNOWN = -1
            RED_HP_RECOVERY = 0
            RED_BULLET_SUPPLY = 1
            BLUE_HP_RECOVERY = 2
            BLUE_BULLET_SUPPLY = 3
            DISABLE_SHOOTING = 4
            DISABLE_MOVEMENT = 5
            def str(self) -> str:
                if(self.value == 0):
                    return "RED_HP_RECOVERY"
                elif(self.value == 1):
                    return "RED_BULLET_SUPPLY"
                elif(self.value == 2):
                    return "BLUE_HP_RECOVERY"
                elif(self.value == 3):
                    return "BLUE_BULLET_SUPPLY"
                elif(self.value == 4):
                    return "DISABLE_SHOOTING"
                elif(self.value == 5):
                    return "DISABLE_MOVEMENT"
                else:
                    return "UNKNOWN"
        
        def __init__(self, buff_zone_id, buff_type, is_active) -> None:
            self.id = buff_zone_id
            self.type = self.BuffType(buff_type)
            self.is_active = is_active
        
        def setBuffZone(self, buff_type, is_active):
            self.type = self.BuffType(buff_type - 1)
            self.is_active = is_active
        
        def __str__(self) -> str:
            activestr = "√" if self.is_active else "×"
            return "{:d} - {} : {}".format(self.id + 1, self.type.name, activestr)




     

