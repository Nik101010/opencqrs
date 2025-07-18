package com.opencqrs.framework.command2;

public class BookHandler {

    static class PurchaseBookCommandQuery implements CommandHandlingMode.Query<PurchaseBookCommand> {

        @Override
        public Query query(QueryBuilder cb, PurchaseBookCommand command) {
            return "";
        }
    }

    @CommandHandlingForQuery(query = PurchaseBookCommandQuery.class)
    public void foo() {

    }
}
