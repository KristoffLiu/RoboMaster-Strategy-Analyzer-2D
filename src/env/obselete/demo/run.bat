@echo off
start cmd /k python ./openjava.py
TIMEOUT /T 3
C:\Users\kristoffliu\.conda\envs\rm_simulator4j\python.exe ../demo/setroamer.py
C:\Users\kristoffliu\.conda\envs\rm_simulator4j\python.exe costmap3dvisualization.py
echo
pause
