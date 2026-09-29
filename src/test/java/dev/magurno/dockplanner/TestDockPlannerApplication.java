package dev.magurno.dockplanner;

import org.springframework.boot.SpringApplication;

public class TestDockPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.from(DockPlannerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
