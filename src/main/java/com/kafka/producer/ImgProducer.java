package com.kafka.producer;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;


public class ImgProducer {

    private String bootStrapServer;
    private String topicName;
    private String targetDirectory;
    private Long pollIntervalMs;
    private String keySerializer;
    private String valueSerializer;
    private String securityProtocol;
//    private Password sslKeyPassword;
    private String sslKeyPassword;
    private String sslKeystoreLocation;
//    private Password sslKeystorePassword;
    private String sslKeystorePassword;
    private String sslTruststoreLocation;
    private String sslTruststorePassword;


    // 특정 폴더의 이벤트를 감지하는 자바 라이브러리
    private WatchService watchService;

    // 토픽에 전송하지못한 건수 count, 2건이상이면 프로세스 종료
    int errorCnt = 0;

    // 에러 시 윈도우 팝업
    WindowsPopUp windowsPopUp = new WindowsPopUp();


    public void startProducer(Map<String, String> configMap) throws IOException {

//        System.out.println("========= START KAFKA PRODUCER =========");

        // kafka producer 생성
        KafkaProducer<String, String> producer = new KafkaProducer<>(setKafkaConfig(configMap));

        // watchService 시작
        startWatchService();

        // 첫 실행 시, 이미 존재하는 파일들을 먼저 전송
        // watchservice는 초기화된 시점부터의 이벤트를 감지함
        // 이에 기존 파일들에 대해서는 아무 이벤트가 일어나지않음
        // 폴더 내 파일들이 쌓이지 않게하기 위함
        sendCurrentFiles(producer);


        // kafka producer 실행
        try {
            while (true) {

                WatchKey watchKey = watchService.take();

                // watchservice 이벤트 리스트 for문
                for (WatchEvent<?> event : watchKey.pollEvents()) {

                    // 세 번 이상 토픽에 전송하지못했을 시, 종료
                    if (errorCnt == 3) {
//                        System.out.println("========= CLOSING PRODUCER =========");
                        String msg = "메시지를 3건 이상 전송하지 못했습니다. 프로그램을 종료합니다.";
                        producer.close();
                        watchService.close();
                        windowsPopUp.errorPopUp(msg);
                        System.exit(0);
                    }

                    WatchEvent.Kind<?> kind = event.kind();

                    // 운영체제상 이벤트가 소멸되었을때 구문
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
//                        System.out.println("========= WatchService Overflow =========");
                        continue;
                    }


                    // 새로 생성된 이벤트에 대한 구문
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {

//                        System.out.println("========= WATING "+ pollIntervalMs + " =========");
                        // 설정 값 1000 = 1초 대기
                        Thread.sleep(pollIntervalMs);

                        String fileName = event.context().toString();

                        // kafka 토픽 전송
                        // 각 건당 결과값을 받음 -> 동기처리
                        try {
                            RecordMetadata metadata = producer.send(createMessage(fileName)).get();

                            if (!metadata.topic().isEmpty()) {
                                // 전송 성공 시, 원본 파일 삭제
                                removeFile(fileName);
                            }

//                            System.out.println("========= SEND TOPIC: " + metadata.topic() + " =========");
//                            System.out.println("========= File Name: " + fileName + " =========");


                        } catch (Exception e) {
//                            System.out.println("========= ERROR SENDING KAFKA TOPIC From For =========");
                            errorCnt += 1;
                        }

                    }
                }

                // watchservice key 리셋 후 while문 재 반복, 리셋이 안될 시, 종료
                if(!watchKey.reset()) break;
            }

        } catch (Exception e) {
//            System.out.println("========= ERROR SENDING KAFKA TOPIC From While =========");

        } finally {
            watchService.close();
            producer.close();
//            System.out.println("========= CLOSE WATCHSERVICE & KAFKA PRODUCER =========");
        }

    }

    // config.properties파일의 내용을 파싱
    public Properties setKafkaConfig(Map<String, String> configMap) {

        Properties props = new Properties();

        try {
            // 변수 파싱
            bootStrapServer = configMap.get("bootstrap.servers");
            topicName = configMap.get("topicName");

            // 끝 문자 "/" 고정
            if (configMap.get("targetDirectory").endsWith("/")) {
                targetDirectory = configMap.get("targetDirectory");
            } else {
                targetDirectory = configMap.get("targetDirectory") + "/";
            }

            pollIntervalMs = Long.parseLong(configMap.get("pollIntervalMs"));
            keySerializer = configMap.get("key.serializer");
            valueSerializer = configMap.get("value.serializer");


            props.put("bootstrap.servers", bootStrapServer);
            props.put("key.serializer", keySerializer);
            props.put("value.serializer", valueSerializer);
            props.put("acks", "all");

            // SSL 설정일 때
            if (configMap.get("security.protocol").equals("SSL")) {
//                System.out.println("========= PRODUCER IN SSL MODE =========");
                securityProtocol = configMap.get("security.protocol");
                sslKeyPassword = configMap.get("ssl.key.password");
                sslKeystoreLocation = configMap.get("ssl.keystore.location");
                sslKeystorePassword = configMap.get("ssl.keystore.password");
                sslTruststoreLocation = configMap.get("ssl.truststore.location");
                sslTruststorePassword = configMap.get("ssl.truststore.password");

                props.put("security.protocol", securityProtocol);
                props.put("ssl.key.password", sslKeyPassword);
                props.put("ssl.keystore.location", sslKeystoreLocation);
                props.put("ssl.keystore.password", sslKeystorePassword);
                props.put("ssl.truststore.location", sslTruststoreLocation);
                props.put("ssl.truststore.password", sslTruststorePassword);

            } else {
//                System.out.println("========= PRODUCER IN PLANTEXT MODE =========");
            }

        } catch (Exception e) {
//            System.out.println("========= Kafka Config Parsing ERROR =========");
            String msg = "Kafka Config 설정 중 오류가 발생했습니다.";
            e.printStackTrace();
            windowsPopUp.errorPopUp(msg);
            System.exit(0);
        }

        return props;

    }

    // watchService 시작 
    public void startWatchService() {
        try {

            // watchService 생성
            watchService = FileSystems.getDefault().newWatchService();
            Path directory = Paths.get(targetDirectory);

            // watchService 폴더 감지 시작
            directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
//            System.out.println("========= LISTENING FOLDER: " + targetDirectory + " =========");

        } catch (Exception e) {

//            System.out.println("========= Start watchService ERROR =========");
            String msg = "WatchService 초기화 중 오류가 발생했습니다.";
            e.printStackTrace();
            windowsPopUp.errorPopUp(msg);
            System.exit(0);

        }
    }


    // kafka record 생성
    // key: 시스템상 카프카 업로드 시간 (yyyyMMddHHmmssSSS)
    // value:
    //  {
    //      "file_name": 이미지 파일명,
    //      "create_time": 파일의 마지막 수정 시간 == 최종 업로드 시간 (yyyyMMddHHmmssSSS),
    //      "image": 인코딩된 이미지 파일
    //  }
    public ProducerRecord<String, String> createMessage (String fileName) {

        String topicKey = "";
        String topicValue = "";

        try {

            SimpleDateFormat sdfKafka = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            SimpleDateFormat sdfFile = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Path filePath = Paths.get(targetDirectory, fileName);

            // 시스템상 카프카 업로드 시간
            topicKey = sdfKafka.format(new Date());

            // 파일이 마지막으로 수정된 시간
            File fileFullPath = new File(targetDirectory + fileName);
            FileTime lastModifiedTime = (FileTime) Files.getAttribute(fileFullPath.toPath(), "lastModifiedTime");
            long timestamp = lastModifiedTime.toMillis();
            String modified_time = sdfFile.format(new Date(timestamp));

            Map<String, String> fileMap = new HashMap<>();

            fileMap.put("file_name", fileName);

            fileMap.put("create_time", modified_time);

            // 이미지 인코딩
            byte[] fileContent = Files.readAllBytes(filePath);
            String imgValue = Base64.getEncoder().encodeToString(fileContent);
            fileMap.put("image", imgValue);


            // map -> json
            ObjectMapper mapper = new ObjectMapper();
            topicValue = mapper.writeValueAsString(fileMap);


        } catch (Exception e) {

//            System.out.println("========= Create Kafka Record ERROR =========");

        }

        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, topicKey, topicValue);

        return record;

    }

    // kafka topic 전송 후 원본 파일 삭제
    public void removeFile(String fileName) {

        Path filePath = Paths.get(targetDirectory, fileName);
        File fileFullPath = new File(targetDirectory + fileName);

        try {

            if (fileFullPath.isFile()) {
                Files.delete(filePath);
//                System.out.println("========= Delete File: " + filePath + "=========");
            }

        } catch (Exception e) {

//            System.out.println("========= Delete File ERROR =========");

        }
    }

    // 첫 jar 실행 시 이미 존재한 파일들 우선적으로 push
    public void sendCurrentFiles(KafkaProducer<String, String> producer) throws IOException {

//        System.out.println("========= First Sending Current Files in : " + targetDirectory + " =========");

        // 디렉토리 내 이미 존재한 파일리스트
        File dir = new File(targetDirectory);
        String[] fileNames = dir.list();

        for (String fileName : fileNames) {

            // 세 번 이상 토픽에 전송하지못했을 시, 종료
            if (errorCnt == 3) {
//                System.out.println("========= CLOSING PRODUCER =========");
                String msg = "메시지를 3건 이상 전송하지 못했습니다. 프로그램을 종료합니다.";
                producer.close();
                watchService.close();
                windowsPopUp.errorPopUp(msg);
                System.exit(0);
            }

            try {
                // kafka producer 전송
                RecordMetadata metadata = producer.send(createMessage(fileName)).get();

                if (!metadata.topic().isEmpty()){
                    // 전송 성공 시, 원본 파일 삭제
                    removeFile(fileName);
                }
//                System.out.println("========= First SEND TOPIC: " + metadata.topic() + " =========");
//                System.out.println("========= First File Name: " + fileName + " =========");

            } catch (Exception e) {
//                System.out.println("========= ERROR SENDING KAFKA TOPIC From First Sending =========");
                errorCnt += 1;
            }

        }


    }

}
