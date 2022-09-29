// camel-k: language=java dependency=camel:caffeine property=period=30000 property=namespace=func-demo

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.caffeine.CaffeineConstants;
import org.apache.camel.model.ClaimCheckOperation;

public class IssueSource extends RouteBuilder {
  @Override
  public void configure() throws Exception {

      from("timer:issuesource?period={{period}}")
        .to("http://issue-puller.{{namespace}}.svc")
        .unmarshal().json()
        .split().jsonpath("$.[*]")
        
           // Set Vars
           .setProperty("newEvent", constant(Boolean.TRUE))
           .setProperty("issueNumber", simple("${body[number]}"))
           .setProperty("issueUpdatedAt", simple("${body[updated_at]}"))

           // Save Body
          .claimCheck(ClaimCheckOperation.Push)

           // Check Cache
           .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_GET))
           .setHeader(CaffeineConstants.KEY, exchangeProperty("issueNumber"))
           .toF("caffeine-cache://%s", "issues")
           
           .choice()
              .when(header(CaffeineConstants.ACTION_HAS_RESULT).isEqualTo(Boolean.TRUE))
               // Issue already found on Cache
               // Mark newEvent flag in case it is modified.
               // On Kamelet we can accomplish the same using Groovy
               .process( p-> {
                  String lastUpdate = p.getIn().getBody(String.class);
                  String newUpdate = p.getProperty("issueUpdatedAt", String.class);
                  boolean hasChanged = !lastUpdate.equals(newUpdate);
                  p.setProperty("newEvent", hasChanged);
                })
          .end()
          
          // Save on cache the issue update date
          .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_PUT))
          .setHeader(CaffeineConstants.VALUE, simple("${exchangeProperty.issueUpdatedAt}"))
          .toF("caffeine-cache://%s", "issues")
          .claimCheck(ClaimCheckOperation.Pop)
          .choice()
              .when(exchangeProperty("newEvent").isEqualTo(Boolean.TRUE))
                .log(">> new or modified issue found. #${exchangeProperty.issueNumber}")
                .to("log:info")
          .end()      
        .end()
        ;
   }
}
