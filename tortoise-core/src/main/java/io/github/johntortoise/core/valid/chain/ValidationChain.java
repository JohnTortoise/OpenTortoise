package io.github.johntortoise.core.valid.chain;

import java.util.ArrayList;
import java.util.List;

public class ValidationChain {
    private final List<Runnable> validations = new ArrayList<>();
    
    public static ValidationChain create() {
        return new ValidationChain();
    }
    
    public ValidationChain addCheck(Runnable validation) {
        validations.add(validation);
        return this;
    }
    
    public void execute() {
        for (Runnable validation : validations) {
            validation.run();
        }
    }
}