package com.opencqrs.framework.command2;

import java.util.UUID;

public record PurchaseBookCommand(
        UUID bookId
) implements Command2 {
    @Override
    public String getSubject() {
        return "/books/" + bookId();
    }
}
