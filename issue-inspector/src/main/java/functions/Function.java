package functions;

import io.quarkus.funqy.Funq;
import io.quarkus.funqy.knative.events.CloudEvent;
import io.quarkus.funqy.knative.events.CloudEventBuilder;
import io.quarkus.logging.Log;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Function {

    static String LABEL_REQUEST_EVENT_TYPE = "AddLabelRequest";

    @Funq
    public CloudEvent<AddLabelRequest> function(CloudEvent<GitHub.Issue> cloudEvent) {

        GitHub.Issue issue = cloudEvent.data();
        Log.infof("INCOMING EVENT %s %s", cloudEvent.type(), issue);

        if (!issue.isPR() && issue.body != null) {
            String kind = parseKind(issue.body);
            if (kind != null) {
                String labelToAdd = kindLabelMap.get(kind);

                // Returns a AddLabelRequest event in case the label does not exists
                if (labelToAdd != null && !issue.existsLabel(labelToAdd)) {
                    AddLabelRequest requestEvent = new AddLabelRequest(issue.repositoryUrl, issue.number, labelToAdd);

                    Log.infof("OUTGOING EVENT %s %s", LABEL_REQUEST_EVENT_TYPE, requestEvent);
                    return CloudEventBuilder.create()
                            .source("issue-inspector")
                            .type(LABEL_REQUEST_EVENT_TYPE)
                            .id(UUID.randomUUID().toString())
                            .build(requestEvent);
                }
            }
        }
        // Returns an empty event in case label is already in the issue
        return CloudEventBuilder.create().id(UUID.randomUUID().toString()).build(new AddLabelRequest());
    }

    private String parseKind(String body) {
        Pattern pattern = Pattern.compile("(/kind[ ]+)([a-zA-Z]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(body);
        return matcher.find() ? matcher.group(2) : null;
    }

    static Map<String, String> kindLabelMap = Map.of(
            "enhancement", "enhancement",
            "feature", "enhancement",
            "bug", "bug",
            "doc", "documentation"
    );

}
