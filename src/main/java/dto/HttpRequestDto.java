package dto;

import enums.RequestMethod;

public class HttpRequestDto {
    private RequestMethod requestMethod;
    private String requestPath;
    private String queryString;

    public HttpRequestDto(String firstLine) {
        this.requestMethod = RequestMethod.valueOf(firstLine.split(" ")[0]);

        String uri = firstLine.split(" ")[1];
        int index = uri.indexOf("?");
        if (index != -1) {
            this.requestPath = uri.substring(0, index);
            this.queryString = uri.substring(index + 1);
        } else {
            this.requestPath = uri;
        }
    }

    public boolean matchBy(RequestMethod requestMethod,
                           String requestPath) {

        return this.requestMethod == requestMethod
                && this.requestPath.equals(requestPath);
    }

    public String getRequestPath() {
        return requestPath;
    }
}