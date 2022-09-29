package functions;

import io.quarkus.funqy.Funq;
import io.quarkus.funqy.knative.events.CloudEvent;
import io.quarkus.funqy.knative.events.CloudEventBuilder;
import io.quarkus.logging.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Function {

    static String LABEL_ADDED_SUCCESS_EVENT = "IssueLabelAddedSuccess";
    static String LABEL_ADDED_FAILURE_EVENT = "IssueLabelAddedFailure";

    final HttpClient httpClient = HttpClient.newHttpClient();

    @ConfigProperty(name="GITHUB_TOKEN")
    String ghAuthToken;

    @Funq
    public CloudEvent<Output> function(CloudEvent<Input> input) {

        Input addLabelRequest = input.data();
        boolean success = false;
        try {

            String url = String.format("%s/issues/%s/labels",
                    addLabelRequest.url,
                    addLabelRequest.number);

            // Prepare the data to be sent on Post to GitHub API
            String data = String.format("{\"labels\": [\"%s\"]}",
                    addLabelRequest.label);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(data))
                    .setHeader("Accept", "application/vnd.github+json")
                    .setHeader("Authorization", "Bearer " + ghAuthToken)
                    .build();

            success = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;

        } catch (Exception e) {
            Log.error(e);
        }
        Log.infof("Inclusion of label %s to the Issue #%s of repository %s was %s",
                addLabelRequest.label,
                addLabelRequest.number,
                addLabelRequest.url,
                success ? "SUCCESS" : "FAILURE") ;

        return CloudEventBuilder.create()
                .type(success ? LABEL_ADDED_SUCCESS_EVENT : LABEL_ADDED_FAILURE_EVENT)
                .source("issue-labeler")
                .build(new Output("process completed"));
    }

}
