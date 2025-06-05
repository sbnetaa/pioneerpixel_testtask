package ru.terentyev.pionerpixel_testtask;

import org.springframework.boot.SpringApplication;

public class TestPionerpixelTesttaskApplication {

	public static void main(String[] args) {
		SpringApplication.from(PionerpixelTesttaskApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
