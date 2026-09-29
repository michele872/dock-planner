package dev.magurno.dockplanner.common;

public class NotFoundException extends RuntimeException {

	public NotFoundException(String resource, Object id) {
		super("%s %s not found".formatted(resource, id));
	}

}
