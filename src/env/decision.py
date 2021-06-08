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
import time

import threading


from genpy import rostime

sys.path.append(os.getcwd().rstrip("env"))
# os.path.abspath()
from env.rosanalyzer.Analyzer import Analyzer
from env.rosanalyzer.RoboMaster import Ally, Enemy, RoboMaster, StrategyState
from env.rosanalyzer.Localization import Position


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

from std_srvs.srv import SetBool


from roborts_msgs.msg import TwistAccel

class Brain:
    def __init__(self, control_rate):
        self.is_game_start = False
        self.cnt = 1
        self._control_rate = control_rate

        self.analyzer = Analyzer()

        # self._decision_pub = [rospy.Publisher("/CAR1/move_base_simple/goal", PoseStamped, queue_size=10),
        #                     rospy.Publisher("/CAR2/move_base_simple/goal", PoseStamped, queue_size=10)]

        # self._rotate_publisher = [rospy.Publisher("/CAR1/cmd_vel_acc", TwistAccel, queue_size=10),
        #                           rospy.Publisher("/CAR2/cmd_vel_acc", TwistAccel, queue_size=10)]

        self._global_planner_pub = [rospy.Publisher("/CAR1/decision_global_path", Path, queue_size=10),
                            rospy.Publisher("/CAR2/decision_global_path", Path, queue_size=10)]
        self._enemy_id = [rospy.Subscriber("/CAR1/enemy_id", enemy_id, self.updateEnemyID1),
                          rospy.Subscriber("/CAR2/enemy_id", enemy_id, self.updateEnemyID2)]

        self._robots_subscriber = [rospy.Subscriber("/CAR1/amcl_pose", PoseStamped, self.ownPositionCB0),
                                   rospy.Subscriber("/CAR2/amcl_pose", PoseStamped, self.ownPositionCB1)]
        self._enemies_subscriber = rospy.Subscriber("/obstacle_preprocessed", Obstacles, self.enemyInfo)
        self._hp_subscriber = rospy.Subscriber("/CAR1/game_robot_hp", GameRobotHP, self.robotHP)
        self._robot_bullet_subscriber = [rospy.Subscriber("/CAR2/game_robot_bullet", GameRobotBullet, self.robotBullet)]


        self._buff_zone_subscriber = rospy.Subscriber("/CAR1/game_zone_array_status", GameZoneArray, self.gameZone)
        self._game_status_subscriber = rospy.Subscriber("/CAR2/game_status", GameStatus, self.gameState)
        self._vis_pub = [rospy.Publisher("/CAR1/visualization_marker", Marker, queue_size=10),
                         rospy.Publisher("/CAR2/visualization_marker", Marker, queue_size=10)]
        self._is_goal_reach_subscribers = [rospy.Subscriber("/CAR1/global_planner_node_action/status", GoalStatusArray, self.enable_decision1),
                                           rospy.Subscriber("/CAR2/global_planner_node_action/status", GoalStatusArray, self.enable_decision2)]

        self._spin_service = [rospy.ServiceProxy("/CAR1/chassis_spin", SetBool),rospy.ServiceProxy("/CAR2/chassis_spin", SetBool)]
        self._twist_service = [rospy.ServiceProxy("/CAR1/chassis_twist", SetBool),rospy.ServiceProxy("/CAR2/chassis_twist", SetBool)]

        self.decision_1_activate = True
        self.decision_2_activate = True

    def step(self):
        # self.rotateThreads[0].start()
        # self.rotateThreads[1].start()
        # thread1 = threading.Thread(target = self.start_rotate_body1)
        # thread1.start()
        # thread2 = threading.Thread(target = self.start_rotate_body2)
        # thread2.start()

        if (self.analyzer.game_status == Analyzer.GameStatus.GAME):
            self.ally_make_decision(self.analyzer.ally1)
            self.ally_make_decision(self.analyzer.ally2)
            self.analyzer.enemy1.updateDetectionState()
            self.analyzer.enemy2.updateDetectionState()
            self.analyzer.enemy1.increaseVisualTimeStamp(0.5)
            self.analyzer.enemy2.increaseVisualTimeStamp(0.5)

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
            enemyPos  = Position(enemies[0].center.x, enemies[0].center.y)
            self.analyzer.localizationFilter.inputSignal1(enemyPos)

        elif len(enemies) == 2:
            enemyPos1 = Position(enemies[0].center.x, enemies[0].center.y)
            enemyPos2 = Position(enemies[1].center.x, enemies[1].center.y)

            self.analyzer.localizationFilter.inputSignal2(enemyPos1, enemyPos2)

    def robotHP(self, data):
        if self.analyzer.teamColor == 0:
            self.analyzer.ally1.setHealth(data.blue1)
            self.analyzer.ally2.setHealth(data.blue2)
            self.analyzer.enemy1.setHealth(data.red1)
            self.analyzer.enemy2.setHealth(data.red2)
        elif self.analyzer.teamColor == 1:
            self.analyzer.ally1.setHealth(data.red1)
            self.analyzer.ally2.setHealth(data.red2)
            self.analyzer.enemy1.setHealth(data.blue1)
            self.analyzer.enemy2.setHealth(data.blue2)

    def robotBullet(self, data):
        if self.analyzer.teamColor == 0:
            self.analyzer.ally1.setNumOfBullets(data.blue1)
            self.analyzer.ally2.setNumOfBullets(data.blue2)
            self.analyzer.enemy1.setNumOfBullets(data.red1)
            self.analyzer.enemy2.setNumOfBullets(data.red2)
        elif self.analyzer.teamColor == 1:
            self.analyzer.ally1.setNumOfBullets(data.red1)
            self.analyzer.ally2.setNumOfBullets(data.red2)
            self.analyzer.enemy1.setNumOfBullets(data.blue1)
            self.analyzer.enemy2.setNumOfBullets(data.blue2)

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

    def ally_make_decision(self, ally : Ally):
        if ally.isStrategyMakerOn:
            ally.updateStrategyState()
            if ally.strategyState == StrategyState.INITIALIZED:
                pass
            if ally.strategyState == StrategyState.ANDRE_ATTACKING_MODE:
                self.robomaster_twist(ally.no, True)
                # if ally.previousStrategyState != StrategyState.ANDRE_ATTACKING_MODE:
                #     ally.startingAimingTime = time.time()
                # elif ally.previousStrategyState == StrategyState.ANDRE_ATTACKING_MODE:
                #     ally.aimingTime = time.time() - ally.startingAimingTime
                #     if ally.aimingTime > 4:
                #         ally.
            else:
                self.robomaster_twist(ally.no, False)
                if ally.strategyState == StrategyState.TURTLE_MODE:
                    self.robomaster_spin(ally.no, True)
                else:
                    self.robomaster_spin(ally.no, False)
                    # if ally.strategyState == StrategyState.:
                    #     rawPath = ally.getDecisionPath()
                    if ally.strategyState == StrategyState.APPROACHING:
                        self.get_next_path(ally)
                    elif ally.strategyState == StrategyState.GETTING_BUFF:
                        self.get_next_path(ally)
                    elif ally.strategyState == StrategyState.DEAD:
                        pass
            ally.previousStrategyState = ally.strategyState

    def robomaster_spin(self, no, bool):
        self._spin_service[no](bool)
        if not bool:
            time.sleep(1)

    def robomaster_twist(self, no, bool):
        self._twist_service[no](bool) 
        if not bool:
            time.sleep(1)

    def start_rotate_body1(self):
        for _ in range(10):
            cmd = TwistAccel()
            cmd.twist.angular.z = 4.5
            self._rotate_publisher[0].publish(cmd)
            rate.sleep()

    def start_rotate_body2(self):
        for _ in range(10):
            cmd = TwistAccel()
            cmd.twist.angular.z = 4.5
            self._rotate_publisher[1].publish(cmd)
            rate.sleep()

    def get_next_path(self, ally : Ally):
        # pos = self.Blue2.getPointAvoidingFacingEnemies()
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
        self._global_planner_pub[ally.no].publish(path)

    def display(self):
        self.analyzer.displayOnce()
        # pass
        
def call_rosspin():
    rospy.spin()

if __name__ == '__main__':
    try:
        print(__file__ + " start!!")
        rospy.init_node('decision_node', anonymous=True)
        control_rate = 1.0
        rate = rospy.Rate(1.0 / control_rate)
        brain = Brain(control_rate)
        spin_thread = threading.Thread(target=call_rosspin).start()

        brain.analyzer.setTeamColor(0) #调队伍颜色的，0是蓝色，1是红色

        while not rospy.core.is_shutdown():
            brain.display()
            brain.step()
            rate.sleep()

    except rospy.ROSInterruptException:
        pass