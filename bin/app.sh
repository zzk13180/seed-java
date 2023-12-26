#!/bin/sh
APP_NAMES="seed-java.jar xxx.jar"
# log_dir=./log_dir/
command=$1
# 启动
start(){
    if [ -z "$APP_NAMES" ]; then
        echo "No applications to start. APP_NAMES is empty."
        return 1
    fi

    # if [ ! -d "${log_dir}" ];then
    #     mkdir "${log_dir}"
    # fi

    for i in $APP_NAMES;
    do
        # split=$(echo $i|cut -d "." -f 1)
        PID=$(ps -ef |grep java|grep $i|grep -v grep|awk '{print $2}')
        # -Duser.timezone=Asia/Beijing -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDateStamps  -XX:+PrintGCDetails -XX:NewRatio=1 -XX:SurvivorRatio=30 -XX:+UseParallelGC -XX:+UseParallelOldGC
        # nohup java -jar $i 1>"./log_dir/$split.log" 2>"./log_dir/$split-err.log" &
        if [ x"$PID" != x"" ]; then
            echo "$i is running..."
        else
            nohup java -Xms512m -Xmx1024m -jar $i >/dev/null 2>&1 &
            if [ $? -eq 0 ]; then
                echo "start $i success."
            else
                echo "start $i failed."
                return 1
            fi
        fi
    done
    if type "check" > /dev/null; then
        check
    else
        echo "check function does not exist."
    fi

    if type "showpid" > /dev/null; then
        showpid
    else
        echo "showpid function does not exist."
    fi
}

# 停止
stop(){
    for i in $APP_NAMES;
    do
    pid=`ps -ef|grep $i|grep -v grep|grep -v kill|awk '{print $2}'`
    if [ ${pid} ]; then
        echo "$i stop process..."
        kill -15 $pid
    fi
    sleep 5
    pid=`ps -ef|grep $i|grep -v grep|grep -v kill|awk '{print $2}'`
    if [ ${pid} ]; then
        echo "$i Kill Process!"
        kill -9 $pid
    else
        echo "$i Stop Success!"
    fi
    done
}
# 检查
check(){
    for i in $APP_NAMES;
    do
    pid=`ps -ef|grep $i|grep -v grep|grep -v kill|awk '{print $2}'`
    if [ ${pid} ]; then
        echo "$i is running."
    else
        echo "$i is NOT running."
    fi
    done
}
# 强制kill进程
forcekill(){
    for i in $APP_NAMES;
    do
    pid=`ps -ef|grep $i|grep -v grep|grep -v kill|awk '{print $2}'`
    if [ ${pid} ]; then
        echo "$i Kill Process!" 
        kill -9 $pid
    fi
    done
}
# 输出进程号
showpid(){
    for i in $APP_NAMES;
    do
    pid=$(ps -ef|grep $i|grep -v grep|grep -v kill|awk '{print $2}')
    if [ ${pid} ]; then
        echo 'process '$i' pid is '$pid
    else
        echo 'process '$i' is not running.'
    fi
    done
}
# 删除jar
remove(){
    for i in $APP_NAMES;
    do
    rm -rf $i
    echo ''$i' is not removed.'
    done
}
if [ "${command}" =  "start" ]; then
    start
elif [ "${command}" =  "stop" ]; then
    stop
elif [ "${command}" =  "check" ]; then
    check
elif [ "${command}" =  "kill" ]; then
    forcekill
elif [ "${command}" = "pid" ];then
    showpid
elif [ "${command}" = "remove" ];then
    remove
else
    echo "unknown command: ${command}"
fi
