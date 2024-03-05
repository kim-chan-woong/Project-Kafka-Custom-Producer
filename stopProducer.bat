@echo off
jps -l | findstr "KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar"
if %errorlevel%==0 (
  for /f %%i in ('jps -m ^| find "KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar"') do (
    taskkill /f /PID %%i
  )
  echo "kafka producer가 종료되었습니다."
  msg * "kafka producer가 종료되었습니다."
) else (
  echo "kafka producer가 이미 종료 상태입니다."
  msg * "kafka producer가 이미 종료 상태입니다."
)