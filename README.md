# Kafka Producer

로컬(windows or linux) pc의 파일들을 원격 kafka cluster에 전송하는 프로듀서

### 사전 준비 ###
1. java 1.8


### 실행 방법 1 (WINDOWS)
1. __파일 준비__ <br/>
```
~/build/libs/KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar
~/startProduver.bat
~/stopProducer.bat
~/statusProducer.bat
~/config.properties

위 4개의 파일들을 한 폴더에 저장
꼭 한 폴더에 모든 파일이 존재해야함

```
2. __config 수정__ <br/>
```
config.properties 수정

// kafka brokers 설정 - disable ssl
bootstrap.servers=mast01:9092,mast02:9092,mast03:9092
// kafka brokers 설정 - enable ssl
bootstrap.servers=mast01:9093,mast02:9093,mast03:9093
// 토픽 지정
topicName=kcw-test
// 가져올 파일들의 경로 지정
// 윈도우 경로 그대로 복사 붙여넣기 --> "\" 유지
targetDirectory=C:\Users\KCW\Desktop\kafka-producer
// 파일 생성 감지 후 대기 시간 (해당 시간 후 실제 파일 전송, 아래는 1초 대기)
pollIntervalMs=1000
// 직렬화 하기위한 클래스
key.serializer=org.apache.kafka.common.serialization.StringSerializer
// 직렬화 하기위한 클래스
value.serializer=org.apache.kafka.common.serialization.StringSerializer
// 통신에 사용될 프로토콜
// SSL OR PLANTEXT 
security.protocol=SSL
ssl.key.password=passwd
// jks 파일 존재 확인
ssl.keystore.location=C:\Users\KCW\Desktop\kafka-producer\keystore.jks
ssl.keystore.password=passwd
// jks 파일 존재 확인
ssl.truststore.location=C:\Users\KCW\Desktop\kafka-producer\truststore.jks
ssl.truststore.password=passwd
```

3. __.bat 실행__ <br/>
```
startProducer.bat 더블클릭 --> 프로듀서 실행
stopProducer.bat 더블클릭 --> 프로듀서 종료
statusProducer.bat 더블클릭 --> 프로듀서 실행상태 확인

위 파일 실행 시 한글이 깨진다면 .bat 3가지 파일들을 다시 저장
우클릭 -> 편집 -> 파일 -> 다른 이름으로 저장 -> 파일 형식(모든 파일) -> 인코딩(E) (ANSI) -> 저장 -=> 덮어쓰기
```

### 실행 방법 2 (LINUX)

1. __파일 준비__ <br/>
```
~/build/libs/KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar
~/config.properties

위 두 파일이 같은 경로에 위치할 필요는 없지만,
명령어 실행 시 config.properties의 경로를 정확히 기입해야함
```

2. __config 수정__ <br/>
```
config.properties 수정

// kafka brokers 설정 - disable ssl
bootstrap.servers=mast01:9092,mast02:9092,mast03:9092
// kafka brokers 설정 - enable ssl
bootstrap.servers=mast01:9093,mast02:9093,mast03:9093
// 토픽 지정
topicName=kcw-test
// 가져올 파일들의 경로 지정
targetDirectory=/opt/apps/temp/data
// 파일 생성 감지 후 대기 시간 (해당 시간 후 실제 파일 전송, 아래는 1초 대기)
pollIntervalMs=1000
// 직렬화 하기위한 클래스
key.serializer=org.apache.kafka.common.serialization.StringSerializer
// 직렬화 하기위한 클래스
value.serializer=org.apache.kafka.common.serialization.StringSerializer
// 통신에 사용될 프로토콜
// SSL OR PLANTEXT 
security.protocol=SSL
ssl.key.password=passwd
// jks 파일 존재 확인
ssl.keystore.location=/opt/apps/temp/data/keystore.jks
ssl.keystore.password=passwd
// jks 파일 존재 확인
ssl.truststore.location=/opt/apps/temp/data/truststore.jks
ssl.truststore.password=passwd
```

3. __프로듀서 실행__ <br/>
```
nohup java -jar ./KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar ./config.properties &

```

4. __상태확인 및 로그 확인__ <br/>
```
jps | grep KAFKA_IMAGE_PRODUCER-1.0-SNAPSHOT.jar
tail -500f ./nohup.out --> 로그 확인
```

### 카프카 클러스터 측 확인

1. __파일 전송 확인__ <br/>
```
enable ssl
${KAFKA_HOME}/bin/kafka-console-consumer.sh --bootstrap-server mast01:9093,mast02:9093,mast03:9093 --topic kcw-test -consumer.config config/client-ssl-auth.properties

disable ssl
/bin/kafka-console-consumer.sh --bootstrap-server mast01:9092,mast02:9092,mast03:9092 --topic kcw-test
```



### 비고
- 원본파일은 카프카 토픽에 전송 후 삭제
