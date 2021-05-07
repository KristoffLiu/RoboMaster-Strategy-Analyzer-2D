"""
Breadth-First grid planning
author: Erwin Lejeune (@spida_rwin)
See Wikipedia article (https://en.wikipedia.org/wiki/Breadth-first_search)
"""

# TODO:
# 1. 连接裁判系统，当裁判系统发布Five Second CD的时候，准备开启机器人
# 2. 测试到达终点时，脸朝着敌人
# 3. 

import threading
import math
import matplotlib.pyplot as plt
import numpy as np
import sys
import os

sys.path.append(os.getcwd().rstrip("env"))
# os.path.abspath()
from env.rosanalyzer.Analyzer import Analyzer

import rospy
from geometry_msgs.msg import PoseStamped
from obstacle_detector.msg import Obstacles
from roborts_msgs.msg import GameRobotHP
from roborts_msgs.msg import GameZoneArray
from roborts_msgs.msg import GameStatus
from roborts_msgs.msg import GameRobotBullet
from roborts_msgs.msg import RobotDamage
from visualization_msgs.msg import Marker
from actionlib_msgs.msg import GoalStatusArray
from scipy.spatial.transform import Rotation as R

class Brain:
    def __init__(self, control_rate):
        self.is_game_start = False
        self.cnt = 1
        self._control_rate = control_rate

        self.analyzer = Analyzer()

        self._decision_pub = [rospy.Publisher("/CAR1/move_base_simple/goal", PoseStamped, queue_size=10),
                            rospy.Publisher("/CAR2/move_base_simple/goal", PoseStamped, queue_size=10)]
        self._robots_subscriber = [rospy.Subscriber("/CAR1/amcl_pose", PoseStamped, self.ownPositionCB0),
                                   rospy.Subscriber("/CAR2/amcl_pose", PoseStamped, self.ownPositionCB1)]
        self._enemies_subscriber = rospy.Subscriber("/obstacle_preprocessed", Obstacles, self.enemyInfo)
        self._hp_subscriber = rospy.Subscriber("/CAR1/game_robot_hp", GameRobotHP, self.robotHP)
        self._buff_zone_subscriber = rospy.Subscriber("/CAR1/game_zone_array_status", GameZoneArray, self.gameZone)
        self._game_status_subscriber = rospy.Subscriber("/CAR1/game_status", GameStatus, self.gameState)
        self._vis_pub = [rospy.Publisher("/CAR1/visualization_marker", Marker, queue_size=10),
                         rospy.Publisher("/CAR2/visualization_marker", Marker, queue_size=10)]
        self._is_goal_reach_subscribers = [rospy.Subscriber("/CAR1/global_planner_node_action/status", GoalStatusArray, self.enable_decision1),
                                           rospy.Subscriber("/CAR2/global_planner_node_action/status", GoalStatusArray, self.enable_decision2)]


    def enable_decision1(self, msg):
        if msg.status_list[0].status == 1 :
            self.analyzer.allies1.setStrategyMaker(False)
        elif msg.status_list[0].status == 3:
            self.analyzer.allies1.setStrategyMaker(True)

    def enable_decision2(self, msg):
        if msg.status_list[0].status == 1 :
            self.analyzer.allies2.setStrategyMaker(False)
        elif msg.status_list[0].status == 3:
            self.analyzer.allies2.setStrategyMaker(True)

    def ownPositionCB0(self, msg):
        [y, p, r] = R.from_quat([msg.pose.orientation.x,
                                 msg.pose.orientation.y,
                                 msg.pose.orientation.z,
                                 msg.pose.orientation.w]).as_euler('zyx', degrees=True)
        self.analyzer.allies1.setPosition(msg.pose.position.x, msg.pose.position.y,float(y))

    def ownPositionCB1(self, msg):
        [y, p, r] = R.from_quat([msg.pose.orientation.x,
                                 msg.pose.orientation.y,
                                 msg.pose.orientation.z,
                                 msg.pose.orientation.w]).as_euler('zyx', degrees=True)
        self.analyzer.allies2.setPosition(msg.pose.position.x, msg.pose.position.y,float(y))

    def enemyInfo(self, data):
        enemy = data.circles
        if len(enemy) == 1:
            self.analyzer.enemy1.setPosition(enemy[0].center.x, enemy[0].center.y,float(1.57))
        elif len(enemy) == 2:
            self.analyzer.enemy1.setPosition(enemy[0].center.x*1000, enemy[0].center.y*1000,float(1.57))
            self.analyzer.enemy2.setPosition(enemy[1].center.x*1000, enemy[1].center.y*1000,float(1.57))

    def robotHP(self, data):
        self.analyzer.allies1.setHealth(data.blue1)
        self.analyzer.allies2.setHealth(data.blue2)
        self.analyzer.enemy1.setHealth(data.red1)
        self.analyzer.enemy2.setHealth(data.red2)

    def gameState(self, data):
        self.analyzer.updateGameStatus(data.game_status, data.remaining_time)        
        # uint8 READY = 0
        # uint8 PREPARATION = 1
        # uint8 INITIALIZE = 2
        # uint8 FIVE_SEC_CD = 3
        # uint8 GAME = 4
        # uint8 END = 5

    def gameZone(self, data):
        for i, d in enumerate(data.zone):
            self.analyzer.updateBuffZone(i, d.type, d.active)
            
    def _createQuaternionFromYaw(self, yaw):
        # input: r p y
        r = R.from_euler('zyx', [0, 0, yaw], degrees=False).as_quat()
        # output: w x y z
        return [r[3], r[2], r[1], r[0]]

    def get_next_position1(self):
        # pos = self.Blue2.getPointAvoidingFacingEnemies()
        [rx, ry] = self.analyzer.allies1.getDecisionMade()

        enemy = self.analyzer.entrypoint.getLockedEnemy()
        enemyPosition = enemy.getPointPosition()
        gx = enemyPosition.getX() / 100.0
        gy = enemyPosition.getY() / 100.0

        eDistance = math.dist([self.robots[0].old_goal_x, self.robots[0].old_goal_y], [rx, ry]) 
        if (eDistance < 0.15):
            self.robots[0].old_goal_x, self.robots[0].old_goal_y = rx, ry
            return

        self.cnt = self.cnt+1

        yaw_angle = math.atan2(gy - ry, gx - rx)

        goal = PoseStamped()
        goal.header.frame_id = "/map"
        goal.pose.position.x, goal.pose.position.y = rx, ry

        [goal.pose.orientation.w,
        goal.pose.orientation.x,
        goal.pose.orientation.y,
        goal.pose.orientation.z] = self._createQuaternionFromYaw(yaw_angle)

        self._decision_pub[0].publish(goal)

        mark = Marker()
        mark.header.frame_id = "/map"
        mark.header.stamp = rospy.Time.now()
        mark.ns = "showen_point"
        mark.id = 0
        mark.type = Marker().ARROW
        mark.action = Marker().ADD
        mark.pose = goal.pose
        mark.scale.x = 0.4
        mark.scale.y = 0.05
        mark.scale.z = 0.05
        mark.color.a = 1.0
        mark.color.r = 0.2
        mark.color.g = 1.0
        mark.color.b = 0.3
        mark.lifetime = rospy.Duration(self._control_rate, 0)
        self._vis_pub[0].publish(mark)

    def get_next_position2(self):
        # pos = self.Blue2.getPointAvoidingFacingEnemies()
        [rx, ry] = self.analyzer.allies2.getDecisionMade()

        enemy = entrypoint.getLockedEnemy()
        enemyPosition = enemy.getPointPosition()
        gx = enemyPosition.getX() / 100.0
        gy = enemyPosition.getY() / 100.0

        eDistance = math.dist([self.robots[1].old_goal_x, self.robots[1].old_goal_y], [rx, ry]) 
        if (eDistance < 0.15):
            self.robots[1].old_goal_x, self.robots[1].old_goal_y = rx, ry
            return

        self.cnt = self.cnt+1

        yaw_angle = math.atan2(gy - ry, gx - rx)

        goal = PoseStamped()
        goal.header.frame_id = "/map"
        goal.pose.position.x, goal.pose.position.y = rx, ry

        [goal.pose.orientation.w,
        goal.pose.orientation.x,
        goal.pose.orientation.y,
        goal.pose.orientation.z] = self._createQuaternionFromYaw(yaw_angle)
        
        self._decision_pub[1].publish(goal)

        mark = Marker()
        mark.header.frame_id = "/map"
        mark.header.stamp = rospy.Time.now()
        mark.ns = "showen_point"
        mark.id = 1
        mark.type = Marker().ARROW
        mark.action = Marker().ADD
        mark.pose = goal.pose
        mark.scale.x = 0.4
        mark.scale.y = 0.05
        mark.scale.z = 0.05
        mark.color.a = 1.0
        mark.color.r = 0.2
        mark.color.g = 1.0
        mark.color.b = 0.3
        mark.lifetime = rospy.Duration(self._control_rate, 0)
        self._vis_pub[1].publish(mark)

    def display(self):
        self.analyzer.displayOnce()

def call_rosspin():
    rospy.spin()


if __name__ == '__main__':
    try:
        print(__file__ + " start!!")
        rospy.init_node('decision_node', anonymous=True)
        control_rate = 1
        rate = rospy.Rate(1.0 / control_rate)
        brain = Brain(control_rate)
        spin_thread = threading.Thread(target=call_rosspin).start()

        while not rospy.core.is_shutdown():
            brain.display()
            if (brain.analyzer.game_status == Analyzer.GameStatus.GAME):
                brain.get_next_position1()
                brain.get_next_position2()
            rate.sleep()

    except rospy.ROSInterruptException:
        pass