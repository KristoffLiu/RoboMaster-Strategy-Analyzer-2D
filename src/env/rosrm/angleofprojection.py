import math
import time

def calculateAngleOfProjection(u, Sx, Sy, g, inRadian = True):
    '''
    计算发射俯角
    
    Args:
        u: initial velocity 初始速度 (m/s)
        Sx: horizontal displacement 水平位移 (m)
        Sy: vertical displacement 竖直位移 (m)
        g: gravitational constant 重力常数 (m·s^-2)
        inRadian: whether output in radian unit [default True] 是否以弧度单位输出

    Returns:
        AngleCalculated: 
    '''
    a = (g / 2) * (math.pow(Sx, 2) / math.pow(u, 2))
    b = Sx
    c = (g / 2) * (math.pow(Sx, 2) / math.pow(u, 2)) - Sy
    delta = math.pow(b, 2) - 4 * a * c
    result = math.atan((-b + math.sqrt(delta)) / (2 * a))
    return result if inRadian else math.degrees(result)


beginTime = time.time()
print(calculateAngleOfProjection(25, 2.0, 0.25, 9.81, False))
print(time.time() - beginTime)