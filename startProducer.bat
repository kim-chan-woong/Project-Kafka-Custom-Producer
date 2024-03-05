@echo off
jps -l | findstr "KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar"
if %errorlevel%==0 (
  echo "kafka producer가 이미 실행 상태입니다."
  msg * "kafka producer가 이미 실행 상태입니다."
) else (
  msg * "kafka producer 실행 시작"
  echo "kafka producer 실행 시작"
  start javaw -jar ./KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar ./config.properties
)
