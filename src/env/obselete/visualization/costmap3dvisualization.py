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
from matplotlib.colors import LightSource

gateway = JavaGateway() #启动py4j服务器
entrypoint = gateway.entry_point #获取服务器桥的入口
java_import(gateway.jvm,'java.util.*') #导入java中的类的方法

blue1 = entrypoint.getRoboMaster("Blue1")
blue2 = entrypoint.getRoboMaster("Blue2")

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
Z = np.array([])

ax = fig.add_subplot(projection='3d') # projection='3d'
#ax = plt.axes(projection='3d')

# Plot scatterplot data (20 2D points per colour) on the x and z axes.
colors = ('r', 'g', 'b', 'k')
m = np.array([totalwidth - blue1.getPointPosition().getY(), totalwidth - blue2.getPointPosition().getY() ])
n = np.array([blue1.getPointPosition().getX() + border, blue2.getPointPosition().getX() + border])

ax.scatter(m, n, 
            zs= 128,
            marker='o',
            alpha = 1,
            zdir="z",
            s=300,
            color='white', label='points in (x,z)')
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
        zdir="z",
        alpha = 1, linestyle="dashed", linewidth=5, color="green")

x = []
y = []
z = []
for i in blue2.getStrategyPath():
    y.append(i.getX() + border)
    x.append(totalwidth - i.getY())
    z.append(blue2.getCost(i.getX(), i.getY()) - 118)

ax.plot(xs=np.array(x),ys=np.array(y),zs=np.array(z),
        zdir="z", 
        alpha = 1, linestyle="dashed", linewidth=5, color="green")

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

ls = LightSource(270, 20)  #设置你可视化数据的色带
rgb = ls.shade(Z, cmap=cm.gist_earth, vert_exag=0.1, blend_mode='soft')
surf = ax.plot_surface(X, Y, Z, rstride=1, cstride=1,
                cmap="rainbow", alpha=0.80)
                # cmap="rainbow",

#ax.view_init(elev=75, azim=-45)
ax.set_zlim(-150, 150)
ax.set_title('Final Costmap For One Of The RoboMasters', fontsize=32, fontweight='bold')

# ax.xaxis.label.set_color('white')
# ax.yaxis.label.set_color('white')
ax.tick_params(axis='x', labelsize=14)
ax.tick_params(axis='y', labelsize=14)
ax.tick_params(axis='z', labelsize=14)
ax.set_xlabel('Width (cm)' , fontsize=26, fontweight='bold', labelpad = 12.5)
ax.set_ylabel('Length (cm)', fontsize=26, fontweight='bold', labelpad = 25.5)
ax.set_zlabel('height of Cost', fontsize=20, fontweight='bold', labelpad = 25.5)
ax.set_box_aspect((np.ptp(X), np.ptp(Y), np.ptp(Z)))
ax.set_zticks([-150, 0, 150])
plt.show()


