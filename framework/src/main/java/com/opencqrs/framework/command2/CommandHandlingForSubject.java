package com.opencqrs.framework.command2;

import com.opencqrs.framework.command.CommandHandlerDefinition;
import com.opencqrs.framework.command.SourcingMode;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CommandHandlingForSubject {

    /**
     * The {@link SourcingMode} for the {@link CommandHandlerDefinition}.
     *
     * @return the sourcing mode to be used, defaults to {@link SourcingMode#RECURSIVE}
     */
    SourcingMode sourcingMode() default SourcingMode.RECURSIVE;
}