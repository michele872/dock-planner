package dev.magurno.dockplanner.common;

/** The request breaks a business rule, e.g. the vehicle does not fit the dock (HTTP 422). */
public class BusinessRuleException extends RuntimeException {

	public BusinessRuleException(String message) {
		super(message);
	}

}
