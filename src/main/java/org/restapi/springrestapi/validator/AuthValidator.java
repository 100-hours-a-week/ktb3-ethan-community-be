package org.restapi.springrestapi.validator;

public interface AuthValidator {
    void validateSamePassword(String loginPassword, String userPassword);
    void validateNewPassword(String loginPassword, String userPassword);
}
