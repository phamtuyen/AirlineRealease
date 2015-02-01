#! /bin/sh
NAME="air_service"
DESC="Air service"

# The path to Jsvc
EXEC="/usr/local/bin/jsvc"

# The path to the folder containing MyDaemon.jar
FILE_PATH=`dirname "${BASH_SOURCE-$0}"`
FILE_PATH=`cd "$FILE_PATH">/dev/null; pwd`

# The path to the folder containing the java runtime
JAVA_HOME="/Library/Java/JavaVirtualMachines/1.6.0_65-b14-462.jdk/Contents/Home"

# Our classpath including our jar file and the Apache Commons Daemon library
CLASS_PATH="$FILE_PATH:$FILE_PATH/*:$FILE_PATH/lib/*:$FILE_PATH/bin/*:$FILE_PATH/config/*"

# The fully qualified name of the class to execute
CLASS="com.mbv.ticketsystem.launcher.Console"

# The file that will contain our process identification number (pid) for other scripts/programs that need to access it.
PID="$FILE_PATH/$NAME.pid"

# System.out writes to this file...
LOG_OUT="$FILE_PATH/log/$NAME.out"

# System.err writes to this file...
LOG_ERR="$FILE_PATH/err/$NAME.err"

jsvc_exec()
{  
    cd $FILE_PATH
    $EXEC -home $JAVA_HOME -cp $CLASS_PATH -outfile $LOG_OUT -errfile $LOG_ERR -pidfile $PID $1 $CLASS
}

case "$1" in
    start) 
        echo "Starting the $DESC..."       
       
        # Start the service
        jsvc_exec
       
        echo "The $DESC has started."
    ;;
    stop)
        echo "Stopping the $DESC..."
       
        # Stop the service
        jsvc_exec "-stop"      
       
        echo "The $DESC has stopped."
    ;;
    restart)
        if [ -f "$PID" ]; then
           
            echo "Restarting the $DESC..."
           
            # Stop the service
            jsvc_exec "-stop"
           
            # Start the service
            jsvc_exec
           
            echo "The $DESC has restarted."
        else
            echo "Daemon not running, no action taken"
            exit 1
        fi
            ;;
    *)
    echo "Usage: $FILE_PATH/$NAME {start|stop|restart}" >&2
    exit 3
    ;;
esac