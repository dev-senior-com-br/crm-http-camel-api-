package br.com.senior.crm.http.camel.services.impl;

import br.com.senior.crm.http.camel.entities.account.Account;
import br.com.senior.crm.http.camel.entities.account.AccountDefinition;
import br.com.senior.crm.http.camel.entities.account.AccountDefinitionCollection;
import br.com.senior.crm.http.camel.utils.constants.AccountParamsConstant;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class FilterFields {
    @NonNull
    private RouteBuilder builder;
    private final UUID id = UUID.randomUUID();
    @Getter
    private final String directImpl = "direct:crm-filter-fields-" + id.toString();
    @Getter
    private final String directResponse = "direct:crm-filter-fields-response-" + id.toString();

    private static String DIRECT_END_REST = "direct:crm-filter-fields-end-rest";

    public void prepare()
    {
        builder
            .from(directImpl)
            .choice()
            .when(this::filter)
                .to(directResponse)
            .otherwise()
                .to(DIRECT_END_REST)
            .endChoice()
        ;

        builder.
            from(DIRECT_END_REST)
            .log("Não passou no filtro")
            .process(exchange -> exchange.getMessage().setBody(null))
            .endRest()
        ;
    }

    private boolean filter(Exchange exchange)
    {
        boolean isValid = false;

        if (exchange.getMessage().getBody() instanceof AccountDefinitionCollection) {
            isValid = this.filterAccountDefinition(exchange);
        }

        return isValid;
    }

    private boolean filterAccountDefinition(Exchange exchange)
    {
        AccountDefinitionCollection collection = exchange.getMessage().getBody(AccountDefinitionCollection.class);
        Long typeAccount = exchange.getProperty(AccountParamsConstant.TYPE_ACCOUNT, Long.class);
        boolean isValid = false;

        for (AccountDefinition accountDefinition : collection.definitions) {
            if (!isValid) {
                isValid = accountDefinition.getAccountType().getId().equals(typeAccount);
                log.info("Comparative between account type: Definition " + accountDefinition.getId() + " with type account " + accountDefinition.getAccountType().getId() + " and parameter type account " + typeAccount);
            }
        }

        return isValid;
    }
}
