@echo off
jps -l | findstr "KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar"
if %errorlevel%==0 (
  echo "kafka producer�� ���� �����Դϴ�."
  msg * "kafka producer�� ���� �����Դϴ�."
) else (
  echo "kafka producer�� ���� �����Դϴ�."
  msg * "kafka producer�� ���� �����Դϴ�."
)