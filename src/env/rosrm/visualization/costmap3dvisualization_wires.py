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

gateway = JavaGateway() #启动py4j服务器
entrypoint = gateway.entry_point #获取服务器桥的入口

java_import(gateway.jvm,'java.util.*') #导入java中的类的方法

blue1 = entrypoint.getRoboMaster("Blue1")

# plt.ion()

# 定义x, y
x = np.arange(0, 900,10)
y = np.arange(0, 900,10)

# 生成网格数据
X, Y = np.meshgrid(y, x)

b = []
for i in range(0,900,10):
    a = []
    for j in range(900, 0, - 10):
        if i > (900 - 849) / 2 and i < 849 + (900 - 849) / 2 and  j > (900 - 489) / 2 and  j < 489 + (900 - 489) / 2 :    
            m = int(i - (900 - 849) / 2 + 1)
            n = int(j - (900 - 489) / 2 + 1)
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

# Normalize to [0,1]
norm = plt.Normalize(Z.min(), Z.max())
colors = cm.viridis(norm(Z))
rcount, ccount, _ = colors.shape


fig = plt.figure(figsize=(10,10), dpi = 80)
ax = fig.gca(projection='3d')
surf = ax.plot_surface(X, Y, Z, rcount=rcount, ccount=ccount,
                cmap="rainbow", edgecolor='none')
                # cmap="rainbow",
surf.set_facecolor((0,0,0,0))

ax.view_init(elev=75, azim=-45)
ax.set_zlim(-200, 200)
ax.set_title('Final Costmap For One Of The RoboMasters', fontsize=18, fontweight='bold')
ax.set_xlabel('Width (cm)')
ax.set_ylabel('Length (cm)')

plt.show()

