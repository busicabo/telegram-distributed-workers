package com.downloadvideo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DownloadvideoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DownloadvideoApplication.class, args);
	}

}
