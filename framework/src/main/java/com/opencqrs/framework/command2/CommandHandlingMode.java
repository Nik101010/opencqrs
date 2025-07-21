package com.opencqrs.framework.command2;

import com.opencqrs.framework.command.Command;
import com.opencqrs.framework.command.SourcingMode;

public sealed interface CommandHandlingMode {
    record Subject(
            SourcingMode sourcingMode,
            Command.SubjectCondition subjectCondition
    ) implements CommandHandlingMode {
        public Subject() {
            this(SourcingMode.RECURSIVE, Command.SubjectCondition.NONE);
        }
    }




    @FunctionalInterface
    non-sealed interface Query<C extends Command2> extends CommandHandlingMode {
        Query query(QueryBuilder qb, C command);
    }
}
