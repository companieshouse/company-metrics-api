package uk.gov.companieshouse.company.metrics.config;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Context to store the state.
 */
public enum CucumberContext {

    CONTEXT;

    private static final String RESPONSE = "RESPONSE";

    private final ThreadLocal<Map<String, Object>> testContexts = ThreadLocal.withInitial(HashMap::new);

    public Object get(String name) {
        return testContexts.get()
                .get(name);
    }

    public <T> T set(String name, T object) {
        testContexts.get()
                .put(name, object);
        return object;
    }
}
