package br.ufc.ds.trabalho3.api.queue;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateInvestorMessage.class, name = "CreateInvestor"),
        @JsonSubTypes.Type(value = AddBalanceMessage.class, name = "AddBalance"),
        @JsonSubTypes.Type(value = CreateOrderMessage.class, name = "CreateOrder")
})
public sealed interface QueueMessage permits CreateInvestorMessage, AddBalanceMessage, CreateOrderMessage {
}
