import os

#上面2种方法 是python 执行终端/控制台 命令的常见方法
os.system('dir') #执行成功 返回 0 
os.system('java -jar ../../../simulator/core-1.0.jar') #执行成功 返回 0 
#ping = os.popen('pint www.baidu.com').read().strip()  返回输出结果
#注：os.system() 执行完成 会关闭 所以当执行后续 命令需要依赖前面的命令时，请将多条命令写到一个 os.system() 内
