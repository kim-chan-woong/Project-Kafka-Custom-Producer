@echo off
jps -l | findstr "KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar"
if %errorlevel%==0 (
  echo "kafka producer가 실행 상태입니다."
  msg * "kafka producer가 실행 상태입니다."
) else (
  echo "kafka producer가 종료 상태입니다."
  msg * "kafka producer가 종료 상태입니다."
)