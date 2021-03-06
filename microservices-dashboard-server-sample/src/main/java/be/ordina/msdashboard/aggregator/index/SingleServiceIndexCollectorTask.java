package be.ordina.msdashboard.aggregator.index;

import be.ordina.msdashboard.aggregator.health.DependenciesListFilterPredicate;
import be.ordina.msdashboard.aggregator.health.HealthToNodeConverter;
import be.ordina.msdashboard.aggregator.health.ToolBoxDependenciesModifier;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.Service;
import com.google.common.annotations.VisibleForTesting;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

public class SingleServiceIndexCollectorTask implements Callable<Node> {

	private static final Logger LOG = LoggerFactory.getLogger(SingleServiceIndexCollectorTask.class);
	private final String uriString;
	private final static String GATEWAY = "";
	private final static String HEALTH = "health";
	private final Service service;
	private IndexToNodeConverter indexToNodeConverter;
	private DependenciesListFilterPredicate dependenciesListFilterPredicate;
	private ToolBoxDependenciesModifier toolBoxDependenciesModifier;

	public SingleServiceIndexCollectorTask(final Service service, final HttpServletRequest originRequest) {
		this.service = service;
		uriString = buildHealthUri(service);
		indexToNodeConverter = new IndexToNodeConverter();
		dependenciesListFilterPredicate = new DependenciesListFilterPredicate();
		toolBoxDependenciesModifier = new ToolBoxDependenciesModifier();
	}

	private String buildHealthUri(final Service service) {
		Assert.notNull(service.getId());
		StringBuilder builder = new StringBuilder("http://");
		builder.append(service.getHost())
				.append(":")
				.append(service.getPort())
				.append("/")
				.append(service.getId());
		return builder.toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Node call() throws Exception {
		long startTime = 0;
		if (LOG.isDebugEnabled()) {
			startTime = new DateTime().getMillis();
		}
		RestTemplate restTemplate = getRestTemplate();

		LOG.debug("Calling URI: {}", uriString);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.valueOf("application/hal+json")));
		ResponseEntity<Map> responseRest = restTemplate.exchange(uriString, HttpMethod.GET, new HttpEntity<byte[]>(headers), Map.class);
		Node node = indexToNodeConverter.convert(responseRest.getBody(), service);
		if (LOG.isDebugEnabled()) {
			long totalTime = new DateTime().getMillis() - startTime;
			LOG.debug("Finished URI: {} Total time: {}", uriString, totalTime);
		}
		return node;
	}

	@VisibleForTesting
	RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			protected boolean hasError(final HttpStatus statusCode) {
				if (HttpStatus.SERVICE_UNAVAILABLE.equals(statusCode)) {
					return false;
				} else {
					return (statusCode.series() == HttpStatus.Series.CLIENT_ERROR ||
							statusCode.series() == HttpStatus.Series.SERVER_ERROR);
				}
			}
		});
		return restTemplate;
	}
}
