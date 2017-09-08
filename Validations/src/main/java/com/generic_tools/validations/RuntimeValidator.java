package com.generic_tools.validations;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class RuntimeValidator {

	private Validator validator;

	public RuntimeValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	public ValidatorResponse validate(Object obj) {
		System.out.println("Validate " + obj);
		Set<ConstraintViolation<Object>> constraints = validator.validate(obj);
		if (constraints.isEmpty()) {
			return new ValidatorResponse(ValidatorResponse.Status.SUCCESS, "Validated successfully");
		}
		
		String error_messege = "";
		for (ConstraintViolation<?> constraint : constraints) {
			error_messege += constraint.getMessage() + "\n";
		}
		
		return new ValidatorResponse(ValidatorResponse.Status.FAILURE, error_messege);
	}
}
