# TODO:
# 1. 连接裁判系统，当裁判系统发布Five Second CD的时候，准备开启机器人
# 2. 测试到达终点时，脸朝着敌人
# 3. 

from py4j.java_gateway import JavaGateway
from py4j.java_gateway import java_import
import matplotlib
import time
from matplotlib import cm
#%matplotlib inline
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import axes3d
import matplotlib.pyplot as plt

gateway = JavaGateway() #启动py4j服务器
entrypoint = gateway.entry_point #获取服务器桥的入口

java_import(gateway.jvm,'java.util.*') #导入java中的类的方法

blue1 = entrypoint.getRoboMaster("Blue1")

fig = plt.figure(figsize=(10,10), dpi = 80)
# plt.ion()

# 定义x, y

length = 849
width = 489
border = 20
totallength = length + border * 2
totalwidth = width + border * 2
step = 10

x = np.arange(0, totallength, step)
y = np.arange(0, totalwidth, step)

# 生成网格数据
X, Y = np.meshgrid(y, x)

b = []
for i in range(0,totallength,step):
    a = []
    for j in range(totalwidth, 0, - step):
        if i > (totallength - length) / 2 and i < length + (totallength - length) / 2 and  j > (totalwidth - width) / 2 and  j < width + (totalwidth - width) / 2 :    
            m = int(i - (totallength - length) / 2 + 1)
            n = int(j - (totalwidth - width) / 2 + 1)
            if blue1.getCost(m,n) > 255:
                a.append(255 - 128)
            else:
                a.append(blue1.getCost(m, n) - 128)
        else:
            a.append(-128)
    ar = np.array(a)
    b.append(ar)
# 计算Z轴的高度
Z = np.array(b)

ax = fig.add_subplot(111, projection='3d')
 
# # Grab some test data.
# X, Y, Z = axes3d.get_test_data(0.05)
 
# Plot a basic wireframe.
ax.plot_wireframe(X, Y, Z, rstride=1, cstride=1)

colors = ('r', 'g', 'b', 'k')
m = np.array([totalwidth - blue1.getPointPosition().getY() ])
n = np.array([blue1.getPointPosition().getX() + border])

ax.scatter(m, n, 
            zs= blue1.getCost(blue1.getPointPosition().getX(),blue1.getPointPosition().getY()) - 118,
            marker='o',
            zdir="z",
            s=300,
            color='red', label='points in (x,z)')
# for i in range(len(x)):
#     plt.annotate(txt[i], xy = (x[i], y[i]), xytext = (x[i]+0.1, y[i]+0.1))

x = []
y = []
z = []
for i in blue1.getStrategyPath():
    y.append(i.getX() + border)
    x.append(totalwidth - i.getY())
    z.append(blue1.getCost(i.getX(), i.getY()) - 118)

ax.plot(xs=np.array(x),ys=np.array(y),zs=np.array(z),
        zdir="z", linestyle="dashed", linewidth=5, color="green")

#ax.view_init(elev=75, azim=-45)
ax.set_zlim(-150, 150)
ax.set_title('Final Costmap For One Of The RoboMasters', fontsize=32, fontweight='bold')




ax.tick_params(axis='x', labelsize=14)
ax.tick_params(axis='y', labelsize=14)
ax.tick_params(axis='z', labelsize=14)

ax.set_xlabel('Width (cm)' , fontsize=26, fontweight='bold', labelpad = 12.5)
ax.set_ylabel('Length (cm)', fontsize=26, fontweight='bold', labelpad = 25.5)
ax.set_box_aspect((np.ptp(X), np.ptp(Y), np.ptp(Z)))
 
plt.show()
