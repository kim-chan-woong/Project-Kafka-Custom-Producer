@echo off
jps -l | findstr "KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar"
if %errorlevel%==0 (
  for /f %%i in ('jps -m ^| find "KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar"') do (
    taskkill /f /PID %%i
  )
  echo "kafka producer�� ����Ǿ����ϴ�."
  msg * "kafka producer�� ����Ǿ����ϴ�."
) else (
  echo "kafka producer�� �̹� ���� �����Դϴ�."
  msg * "kafka producer�� �̹� ���� �����Դϴ�."
)