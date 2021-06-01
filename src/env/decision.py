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


from genpy import rostime

sys.path.append(os.getcwd().rstrip("env"))
# os.path.abspath()
from env.rosanalyzer.Analyzer import Analyzer
from env.rosanalyzer.RoboMaster import Ally, Enemy

import rospy
from geometry_msgs.msg import PoseStamped
from nav_msgs.msg import Path
from obstacle_detector.msg import Obstacles
from roborts_msgs.msg import GameRobotHP
from roborts_msgs.msg import GameZoneArray
from roborts_msgs.msg import GameStatus
from roborts_msgs.msg import GameRobotBullet
from roborts_msgs.msg import RobotDamage
from visualization_msgs.msg import Marker
from autofire.msg import enemy_id
from actionlib_msgs.msg import GoalStatusArray
from scipy.spatial.transform import Rotation as R

class Brain:
    def __init__(self, control_rate):
        self.is_game_start = False
        self.cnt = 1
        self._control_rate = control_rate

        self.analyzer = Analyzer()

        # self._decision_pub = [rospy.Publisher("/CAR1/move_base_simple/goal", PoseStamped, queue_size=10),
        #                     rospy.Publisher("/CAR2/move_base_simple/goal", PoseStamped, queue_size=10)]

        self._global_planner_pub = [rospy.Publisher("/CAR1/decision_global_path", Path, queue_size=10),
                            rospy.Publisher("/CAR2/decision_global_path", Path, queue_size=10)]
        self._enemy_id = [rospy.Subscriber("/CAR1/enemy_id", enemy_id, self.updateEnemyID1),
                          rospy.Subscriber("/CAR2/enemy_id", enemy_id, self.updateEnemyID2)]

        self._robots_subscriber = [rospy.Subscriber("/CAR1/amcl_pose", PoseStamped, self.ownPositionCB0),
                                   rospy.Subscriber("/CAR2/amcl_pose", PoseStamped, self.ownPositionCB1)]
        self._enemies_subscriber = rospy.Subscriber("/obstacle_preprocessed", Obstacles, self.enemyInfo)
        self._hp_subscriber = rospy.Subscriber("/CAR1/game_robot_hp", GameRobotHP, self.robotHP)
        self._buff_zone_subscriber = rospy.Subscriber("/CAR1/game_zone_array_status", GameZoneArray, self.gameZone)
        self._game_status_subscriber = rospy.Subscriber("/CAR2/game_status", GameStatus, self.gameState)
        self._vis_pub = [rospy.Publisher("/CAR1/visualization_marker", Marker, queue_size=10),
                         rospy.Publisher("/CAR2/visualization_marker", Marker, queue_size=10)]
        self._is_goal_reach_subscribers = [rospy.Subscriber("/CAR1/global_planner_node_action/status", GoalStatusArray, self.enable_decision1),
                                           rospy.Subscriber("/CAR2/global_planner_node_action/status", GoalStatusArray, self.enable_decision2)]


        self.decision_1_activate = True
        self.decision_2_activate = True

    def enable_decision1(self, msg):
        isOn = True
        for i in range(len(msg.status_list)):
            if(msg.status_list[i].status) == 1:
                isOn = False
                break
        self.analyzer.ally1.setStrategyMaker(isOn)

    def enable_decision2(self, msg):
        isOn = True
        for i in range(len(msg.status_list)):
            if(msg.status_list[i].status) == 1:
                isOn = False
                break
        self.analyzer.ally2.setStrategyMaker(isOn)

    def ownPositionCB0(self, msg):
        [y, p, r] = R.from_quat([msg.pose.orientation.x,
                                 msg.pose.orientation.y,
                                 msg.pose.orientation.z,
                                 msg.pose.orientation.w]).as_euler('zyx', degrees=True)
        self.analyzer.ally1.setPosition(msg.pose.position.x, msg.pose.position.y,float(y))

    def ownPositionCB1(self, msg):
        [y, p, r] = R.from_quat([msg.pose.orientation.x,
                                 msg.pose.orientation.y,
                                 msg.pose.orientation.z,
                                 msg.pose.orientation.w]).as_euler('zyx', degrees=True)
        self.analyzer.ally2.setPosition(msg.pose.position.x, msg.pose.position.y,float(y))

    def enemyInfo(self, data):
        enemies = data.circles
        if len(enemies) == 1:
            enemy = enemies[0]
            if enemy.center.x == None or enemy.center.y == None:
                print("something is null")
                return

            # if abs(self.analyzer.enemy1.x - enemy.center.x) < abs(self.analyzer.enemy2.x - enemy.center.x) and abs(self.analyzer.enemy1.y - enemy.center.y) < abs(self.analyzer.enemy2.y - enemy.center.y):
            #     if self.analyzer.enemy1.isVisualPositionMatched(enemy.center.x, enemy.center.y, float(0)) == 1:
            #         self.analyzer.enemy1.setPosition(enemy.center.x, enemy.center.y, float(0))
            #     elif self.analyzer.enemy2.isVisualPositionMatched(enemy.center.x, enemy.center.y, float(0)) == 1:
            #         self.analyzer.enemy2.setPosition(enemy.center.x, enemy.center.y, float(0))
            # else:
            #     if self.analyzer.enemy2.isVisualPositionMatched(enemy.center.x, enemy.center.y, float(0)) == 1:
            #         self.analyzer.enemy2.setPosition(enemy.center.x, enemy.center.y, float(0))
            #     elif self.analyzer.enemy1.isVisualPositionMatched(enemy.center.x, enemy.center.y, float(0)) == 1:
            #         self.analyzer.enemy1.setPosition(enemy.center.x, enemy.center.y, float(0))
        elif len(enemies) == 2:
            enemyPos1 = enemies[0]
            enemyPos2 = enemies[1]
            if enemyPos1.center.x == None or enemyPos1.center.y == None or enemyPos2.center.x == None or enemyPos2.center.y == None:
                print("something is null")
            elif self.analyzer.enemy1.x == None or self.analyzer.enemy1.y == None:
                print("B is null")
            else:
                if abs(self.analyzer.enemy1.x - enemyPos1.center.x) < abs(self.analyzer.enemy2.x - enemyPos1.center.x) and abs(self.analyzer.enemy1.y - enemyPos1.center.y) < abs(self.analyzer.enemy2.y - enemyPos1.center.y):
                    if self.analyzer.enemy1.isVisualPositionMatched(enemyPos1.center.x, enemyPos1.center.y, float(0)) == 1:
                        self.analyzer.enemy1.setPosition(enemyPos1.center.x, enemyPos1.center.y, float(0))
                        self.analyzer.enemy2.setPosition(enemyPos2.center.x, enemyPos2.center.y, float(0))
                    elif self.analyzer.enemy2.isVisualPositionMatched(enemyPos1.center.x, enemyPos1.center.y, float(0)) == 1:
                        self.analyzer.enemy2.setPosition(enemyPos1.center.x, enemyPos1.center.y, float(0))
                        self.analyzer.enemy1.setPosition(enemyPos2.center.x, enemyPos2.center.y, float(0))
                else:
                    if self.analyzer.enemy2.isVisualPositionMatched(enemyPos1.center.x, enemyPos1.center.y, float(0)) == 1:
                        self.analyzer.enemy2.setPosition(enemyPos1.center.x, enemyPos1.center.y, float(0))
                        self.analyzer.enemy1.setPosition(enemyPos2.center.x, enemyPos2.center.y, float(0))
                    elif self.analyzer.enemy1.isVisualPositionMatched(enemyPos1.center.x, enemyPos1.center.y, float(0)) == 1:
                        self.analyzer.enemy1.setPosition(enemyPos1.center.x, enemyPos1.center.y, float(0))
                        self.analyzer.enemy2.setPosition(enemyPos2.center.x, enemyPos2.center.y, float(0))


    def robotHP(self, data):
        self.analyzer.ally1.setHealth(data.blue1)
        self.analyzer.ally2.setHealth(data.blue2)
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
            # self.analyzer.updateBuffZone(i, 0, False)


    def _createQuaternionFromYaw(self, yaw):
        # input: r p y
        r = R.from_euler('zyx', [0, 0, yaw], degrees=False).as_quat()
        # output: w x y z
        return [r[3], r[2], r[1], r[0]]

    def updateEnemyID1(self, msg):
        self.updateEnemyID(msg)
            
    def updateEnemyID2(self, msg):
        self.updateEnemyID(msg)

    def updateEnemyID(self, msg):
        id = int(msg.id[len(msg.id)-1])
        if id == 1:
            self.analyzer.enemy1.setVisualPosition(msg.x, msg.y)
        elif id == 2:
            self.analyzer.enemy2.setVisualPosition(msg.x, msg.y)
        else:
            print("visual localization update error!")


    def get_next_position1(self):
        if self.analyzer.ally1.isStrategyMakerOn:
            # pos = self.Blue2.getPointAvoidingFacingEnemies()
            [rx, ry] = self.analyzer.ally1.getDecisionMade()

            enemy = self.analyzer.entrypoint.getLockedEnemy()
            enemyPosition = enemy.getPointPosition()
            gx = enemyPosition.getX() / 100.0
            gy = enemyPosition.getY() / 100.0

            eDistance = math.dist([self.analyzer.ally1.old_goal_x, self.analyzer.ally1.old_goal_y], [rx, ry]) 
            if (eDistance < 0.15):
                self.analyzer.ally1.old_goal_x, self.analyzer.ally1.old_goal_y = rx, ry
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
        if self.analyzer.ally2.isStrategyMakerOn:
            # pos = self.Blue2.getPointAvoidingFacingEnemies()
            [rx, ry] = self.analyzer.ally2.getDecisionMade()

            enemy = self.analyzer.entrypoint.getLockedEnemy()
            enemyPosition = enemy.getPointPosition()
            gx = enemyPosition.getX() / 100.0
            gy = enemyPosition.getY() / 100.0

            eDistance = math.dist([self.analyzer.ally2.old_goal_x, self.analyzer.ally2.old_goal_y], [rx, ry]) 
            if (eDistance < 0.15):
                self.analyzer.ally2.old_goal_x, self.analyzer.ally2.old_goal_y = rx, ry
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

    def get_next_path(self, ally : Ally):
        if ally.isStrategyMakerOn:
            rawPath = ally.getDecisionPath()
        if len(rawPath) == 0:
            return
        path = Path()
        path.header.frame_id = "/map"
        path.header.stamp = rospy.get_rostime()

        for node in rawPath:
            goal = PoseStamped()
            goal.header.frame_id = "/map"
            goal.pose.position.x, goal.pose.position.y = node.x, node.y

            [goal.pose.orientation.w,
            goal.pose.orientation.x,
            goal.pose.orientation.y,
            goal.pose.orientation.z] = self._createQuaternionFromYaw(node.yaw)
            # print("yaw angle: " , node.yaw)
            path.poses.append(goal)
        self._global_planner_pub[0].publish(path)
        # mark = Marker()
        # mark.header.frame_id = "/map"
        # mark.header.stamp = rospy.Time.now()
        # mark.ns = "showen_point"
        # mark.id = 1
        # mark.type = Marker().ARROW
        # mark.action = Marker().ADD
        # mark.pose = goal.pose
        # mark.scale.x = 0.4
        # mark.scale.y = 0.05
        # mark.scale.z = 0.05
        # mark.color.a = 1.0
        # mark.color.r = 0.2
        # mark.color.g = 1.0
        # mark.color.b = 0.3
        # mark.lifetime = rospy.Duration(self._control_rate, 0)
        # self._vis_pub[1].publish(mark)

    def get_next_path1(self):
        if self.analyzer.ally1.isStrategyMakerOn:
            # pos = self.Blue2.getPointAvoidingFacingEnemies()
            rawPath = self.analyzer.ally1.getDecisionPath()

            if len(rawPath) == 0:
                return

            path = Path()
            path.header.frame_id = "/map"
            path.header.stamp = rospy.get_rostime()

            for node in rawPath:
                goal = PoseStamped()
                goal.header.frame_id = "/map"
                goal.pose.position.x, goal.pose.position.y = node.x, node.y

                [goal.pose.orientation.w,
                goal.pose.orientation.x,
                goal.pose.orientation.y,
                goal.pose.orientation.z] = self._createQuaternionFromYaw(node.yaw)
                # print("yaw angle: " , node.yaw)
                path.poses.append(goal)
            self._global_planner_pub[0].publish(path)
            
            # mark = Marker()
            # mark.header.frame_id = "/map"
            # mark.header.stamp = rospy.Time.now()
            # mark.ns = "showen_point"
            # mark.id = 1
            # mark.type = Marker().ARROW
            # mark.action = Marker().ADD
            # mark.pose = goal.pose
            # mark.scale.x = 0.4
            # mark.scale.y = 0.05
            # mark.scale.z = 0.05
            # mark.color.a = 1.0
            # mark.color.r = 0.2
            # mark.color.g = 1.0
            # mark.color.b = 0.3
            # mark.lifetime = rospy.Duration(self._control_rate, 0)
            # self._vis_pub[1].publish(mark)

    def get_next_path2(self):
        if self.analyzer.ally2.isStrategyMakerOn:
            # pos = self.Blue2.getPointAvoidingFacingEnemies()
            rawPath = self.analyzer.ally2.getDecisionPath()
            if len(rawPath) == 0:
                return
            path = Path()
            path.header.frame_id = "/map"
            path.header.stamp = rospy.get_rostime()

            for node in rawPath:
                
                goal = PoseStamped()
                goal.header.frame_id = "/map"
                goal.pose.position.x, goal.pose.position.y = node.x, node.y

                [goal.pose.orientation.w,
                goal.pose.orientation.x,
                goal.pose.orientation.y,
                goal.pose.orientation.z] = self._createQuaternionFromYaw(node.yaw)
                path.poses.append(goal)

            self._global_planner_pub[1].publish(path)

            # mark = Marker()
            # mark.header.frame_id = "/map"
            # mark.header.stamp = rospy.Time.now()
            # mark.ns = "showen_point"
            # mark.id = 0
            # mark.type = Marker().ARROW
            # mark.action = Marker().ADD
            # mark.pose = goal.pose
            # mark.scale.x = 0.4
            # mark.scale.y = 0.05
            # mark.scale.z = 0.05
            # mark.color.a = 1.0
            # mark.color.r = 0.2
            # mark.color.g = 1.0
            # mark.color.b = 0.3
            # mark.lifetime = rospy.Duration(self._control_rate, 0)
            # self._vis_pub[0].publish(mark)

    def display(self):
        self.analyzer.displayOnce()
        #pass
        

def call_rosspin():
    rospy.spin()


if __name__ == '__main__':
    try:
        print(__file__ + " start!!")
        rospy.init_node('decision_node', anonymous=True)
        control_rate = 0.5
        rate = rospy.Rate(1.0 / control_rate)
        brain = Brain(control_rate)
        spin_thread = threading.Thread(target=call_rosspin).start()

        brain.analyzer.setTeamColor(1) #调队伍颜色的，0是蓝色，1是红色

        while not rospy.core.is_shutdown():
            brain.display()
            if (brain.analyzer.game_status == Analyzer.GameStatus.GAME):
                # brain.get_next_position1()
                # brain.get_next_position2()
                brain.get_next_path1()
                brain.get_next_path2()
                pass
            rate.sleep()

    except rospy.ROSInterruptException:
        pass