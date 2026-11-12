package lab1.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CosmicWordValidator.class)
public @interface CosmicWordCheck {
    String message() default "Product name must contain a cosmic word";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
