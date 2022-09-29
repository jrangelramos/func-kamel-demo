package functions;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

public class GitHub {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Issue {

        public String repositoryUrl;
        public int number;
        public String title;
        public String body;
        public List<Label> labels;
        public URLRef pullRequest;

        public boolean isPR() {
            return pullRequest != null && pullRequest.url != null;
        }
        public boolean existsLabel(String label) {
            return labels != null && labels.stream().anyMatch( l -> label.equals(l.name) );
        }

        @Override
        public String toString() {
            return "Issue{" +
                    "repositoryUrl='" + repositoryUrl + '\'' +
                    ", number=" + number +
                    ", title='" + title + '\'' +
                    ", body='" + body + '\'' +
                    ", labels=" + labels +
                    '}';
        }
    }

    public static class Label {
        public String name;
    }
    public static class URLRef {
        public String url;
    }
}
