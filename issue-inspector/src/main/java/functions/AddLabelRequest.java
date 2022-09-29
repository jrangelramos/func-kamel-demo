package functions;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddLabelRequest {

    public String url;
    public Integer number;
    public String label;

    public AddLabelRequest() {}

    public AddLabelRequest(String url, Integer number, String label) {
        this.url = url;
        this.number = number;
        this.label = label;
    }
    @Override
    public String toString() {
        return "AddLabelRequest{" +
                "url='" + url + '\'' +
                ", number=" + number +
                ", labelToAdd='" + label + '\'' +
                '}';
    }
}