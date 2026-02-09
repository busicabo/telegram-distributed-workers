package com.taskdistributor.taskdistributor;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@SpringBootApplication
@EnableScheduling
public class TaskdistributorApplication {

	public static void main(String[] args){
        SpringApplication.run(TaskdistributorApplication.class,args);
	}

}
