package app.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ValidationUtil {

    private final  Validator validator;

    public void validate(Object object) {
        Set<ConstraintViolation<Object>> validate = validator.validate(object);

        if (validate.size() > 0) {
            throw new ConstraintViolationException(validate);
        }
    }
}
