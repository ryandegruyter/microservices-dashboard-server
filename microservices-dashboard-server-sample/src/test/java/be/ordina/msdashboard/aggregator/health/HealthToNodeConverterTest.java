package be.ordina.msdashboard.aggregator.health;

import static org.assertj.core.api.Assertions.assertThat;

import static be.ordina.msdashboard.constants.Constants.GROUP;
import static be.ordina.msdashboard.constants.Constants.MICROSERVICE;
import static be.ordina.msdashboard.constants.Constants.TYPE;
import static be.ordina.msdashboard.constants.Constants.VERSION;

import java.util.HashMap;
import java.util.Map;

import be.ordina.msdashboard.model.Node;
import org.junit.Test;

public class HealthToNodeConverterTest {
	private static final String STATUS = "status";

	@Test
	public void shouldConvertCorrectly() throws Exception {
		System.out.println(getSourceMap());
		HealthToNodeConverter converter = new HealthToNodeConverter();
		Node result = converter.convert(getSourceMap());
		Map<String, Object> details = result.getDetails();
		assertThat(details.get(STATUS)).isEqualTo("UP");
		assertThat(details.get(TYPE)).isEqualTo(MICROSERVICE);
		Node bciNode = result.getLinkedNodes().get(1);
		assertThat(bciNode.getDetails().get(TYPE)).isEqualTo("SOAP");
		assertThat(bciNode.getDetails().get(VERSION)).isEqualTo("4.0");
		assertThat(bciNode.getDetails().get(GROUP)).isEqualTo("BCI");
		assertThat(bciNode.getDetails().get(STATUS)).isEqualTo("UP");
		Node cslNode = result.getLinkedNodes().get(0);
		assertThat(cslNode.getDetails().get(TYPE)).isEqualTo("SOAP");
		assertThat(cslNode.getDetails().get(VERSION)).isEqualTo("Customer 4.0 (since 2012 CRC03)");
		assertThat(cslNode.getDetails().get(GROUP)).isEqualTo("CSL");
		assertThat(cslNode.getDetails().get(STATUS)).isEqualTo("UP");
	}

	private Map<String, Object> getSourceMap() {
		Map<String, Object> sourceMap = new HashMap<>();
		sourceMap.put(STATUS, "UP");
		sourceMap.put(TYPE, MICROSERVICE);
		Map<String, Object> innerMap1 = new HashMap<>();
		innerMap1.put(STATUS, "UP");
		innerMap1.put(VERSION, "4.0");
		innerMap1.put(TYPE, "SOAP");
		innerMap1.put(GROUP, "BCI");
		sourceMap.put("bciManageCustomerContact", innerMap1);
		Map<String, Object> innerMap2 = new HashMap<>();
		innerMap2.put(STATUS, "UP");
		innerMap2.put(VERSION, "Customer 4.0 (since 2012 CRC03)");
		innerMap2.put(TYPE, "SOAP");
		innerMap2.put(GROUP, "CSL");
		sourceMap.put("cslCustomer", innerMap2);
		return sourceMap;
	}
}