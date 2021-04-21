"""
Breadth-First grid planning
author: Erwin Lejeune (@spida_rwin)
See Wikipedia article (https://en.wikipedia.org/wiki/Breadth-first_search)
"""

from py4j.java_gateway import JavaGateway
from py4j.java_gateway import java_import

class SimulatorHelper:
    class RoboMaster:



    def __init__(self):
        self.gateway = JavaGateway() #启动py4j服务器
        self.entrypoint = gateway.entry_point #获取服务器桥的入口

        java_import(gateway.jvm,'java.util.*') #导入java中的类的方法

        self.RoboMaster1 = entrypoint.get
        self.RoboMaster2 = entrypoint.getEne

    def __init__(self):
        self.4
