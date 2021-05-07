from py4j.java_gateway import JavaGateway
from py4j.java_gateway import java_import

gateway = JavaGateway() #启动py4j服务器
entrypoint = gateway.entry_point #获取服务器桥的入口

java_import(gateway.jvm,'java.util.*') #导入java中的类的方法

Blue1 = entrypoint.getRoboMaster("Blue1")
strategy = Blue1.getStrategyMaker()
print(strategy.isOn())
print(strategy.isOn(True))