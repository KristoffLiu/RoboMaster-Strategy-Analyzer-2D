# TODO:
# 1. 连接裁判系统，当裁判系统发布Five Second CD的时候，准备开启机器人
# 2. 测试到达终点时，脸朝着敌人
# 3. 

from py4j.java_gateway import JavaGateway
from py4j.java_gateway import java_import
import matplotlib
import time
from matplotlib import cm
import numpy as np
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


x = np.arange(0, totallength,10)
y = np.arange(0, totalwidth,10)

# 生成网格数据
X, Y = np.meshgrid(y, x)

b = []
for i in range(0,totallength,10):
    a = []
    for j in range(totalwidth, 0, - 10):
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
ax = plt.axes(projection='3d')
surf = ax.plot_surface(X, Y, Z, rstride=1, cstride=1,
                cmap="rainbow", edgecolor='none')
                # cmap="rainbow",



# Plot scatterplot data (20 2D points per colour) on the x and z axes.
colors = ('r', 'g', 'b', 'k')
m = np.array([totalwidth - blue1.getPointPosition().getY() + border])
n = np.array([blue1.getPointPosition().getX() + border])
 
# By using zdir='y', the y value of these points is fixed to the zs value 0
# and the (x,y) points are plotted on the x and z axes.
# ax.scatter(m, n, zs=255, zdir='z', s=300, label='points in (x,z)')
# for i in range(len(x)):
#     plt.annotate(txt[i], xy = (x[i], y[i]), xytext = (x[i]+0.1, y[i]+0.1))

ax.view_init(elev=75, azim=-45)
ax.set_zlim(-150, 150)
ax.set_title('Final Costmap For One Of The RoboMasters', fontsize=32, fontweight='bold')
# ax.xaxis.label.set_color('white')
# ax.yaxis.label.set_color('white')
ax.tick_params(axis='x', labelsize=14)
ax.tick_params(axis='y', labelsize=14)
ax.tick_params(axis='z', labelsize=14)
ax.set_xlabel('Width (cm)' , fontsize=26, fontweight='bold', labelpad = 12.5)
ax.set_ylabel('Length (cm)', fontsize=26, fontweight='bold', labelpad = 25.5)
ax.set_box_aspect((np.ptp(X), np.ptp(Y), np.ptp(Z)))

plt.show()


