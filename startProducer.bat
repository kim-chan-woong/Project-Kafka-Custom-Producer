@echo off
jps -l | findstr "KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar"
if %errorlevel%==0 (
  echo "kafka producer�� �̹� ���� �����Դϴ�."
  msg * "kafka producer�� �̹� ���� �����Դϴ�."
) else (
  msg * "kafka producer ���� ����"
  echo "kafka producer ���� ����"
  start javaw -jar ./KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar ./config.properties
)
