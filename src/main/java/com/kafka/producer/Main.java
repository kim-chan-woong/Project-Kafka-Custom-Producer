package com.kafka.producer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class Main {

    public static void main(String[] args) throws IOException {

        WindowsPopUp windowsPopUp = new WindowsPopUp();

        if (!(args.length == 1)) {
            String msg = "파라미터는 하나여야합니다.";
            windowsPopUp.errorPopUp(msg);
            System.exit(0);
        }


        // config.properties 파싱 후 producer.startProducer() 전달
        try {

            // 명령 실행 시 config.properties 경로
            String configFilePath = args[0];

            // producer.startProducer()에 전달할 맵
            Map<String, String> configMap = new HashMap<>();

            // 한 줄씩 읽어 Map에 저장
            BufferedReader reader = new BufferedReader(new FileReader(configFilePath));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String key = parts[0];
                    String value = parts[1];

                    // 윈도우 상 실행 시 대상 이미지 폴더 경로 "\" -> "/" 치환
                    if (value.contains("\\")) {
                        value = value.replace("\\", "/");
                    }

                    configMap.put(key, value);

                }

            }

//            System.out.println("========= SEND config.properties SUCCESS =========");

            ImgProducer producer = new ImgProducer();
            // 입력받은 설정값과 함께 카프카 프로듀서 호출
            producer.startProducer(configMap);

        } catch (Exception e) {
            String msg = "config.properties 파일을 읽지 못하였습니다.";
            e.printStackTrace();
            windowsPopUp.errorPopUp(msg);
            System.exit(0);
        }

    }

}
