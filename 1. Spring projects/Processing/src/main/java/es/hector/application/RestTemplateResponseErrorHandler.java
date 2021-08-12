package es.hector.application;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {
	private Logger logger = LoggerFactory.getLogger(RestTemplateResponseErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) 
      throws IOException {
        return (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR || httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) 
      throws IOException {
        if (httpResponse.getStatusCode()
          .series() == HttpStatus.Series.SERVER_ERROR) {
            logger.error("Error 5XX received: Database seems to be unavailable at the moment");
        } else if (httpResponse.getStatusCode()
          .series() == HttpStatus.Series.CLIENT_ERROR) {
        	logger.error("Error 4XX received. Try again later...");
        }
    }
}