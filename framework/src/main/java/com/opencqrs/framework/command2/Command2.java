package com.opencqrs.framework.command2;

public interface Command2 {

    /**
     * Specifies the subject of this command, needed to source the necessary events for the command execution.
     *
     * @return the subject path
     */
    String getSubject();
}
