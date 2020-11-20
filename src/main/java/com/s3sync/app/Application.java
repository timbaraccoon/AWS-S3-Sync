package com.s3sync.app;

import com.s3sync.app.cloudsyncservice.CloudToDBSynchronizerService;
import com.s3sync.app.cloudsyncservice.CloudToDBSynchronizerServiceImpl;
import com.s3sync.app.dao.FileInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {

		SpringApplication.run(Application.class, args);


	}

}
