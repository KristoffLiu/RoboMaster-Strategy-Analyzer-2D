class Enemy:
    x, y, yaw = 0.0, 0.0, 0.0
    old_goal_x, old_goal_y = 0.0, 0.0
    theta = 0.25
    def __init__(self, x, y, yaw):
        self.x, self.y, self.yaw = x, y, yaw
    def __str__(self):
        return "x: %.2f, y: %.2f" % (self.x, self.y)
    def __eq__(self, other):
        return abs(self.x - other.x) < self.theta and abs(self.y - other.y) < self.theta
    def __sub__(self, other):
        return self.x - other.x, self.y - other.y

class Robot:
    def __init__(self, x, y, yaw):
        self.x, self.y, self.yaw = x, y, yaw

class Allies:
    def __init__(self, x, y, yaw):
        self.x, self.y, self.yaw = x, y, yaw

