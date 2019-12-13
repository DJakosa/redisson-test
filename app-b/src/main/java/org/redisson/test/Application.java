package org.redisson.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class Application extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final String SESSION_ATTRIBUTE_LAST_REQUEST = "last_request";

    private static final String SESSION_ATTRIBUTE_COUNT = "count";

    @Autowired
    private TestSessionBean testSessionBean;

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping(value = "/session-values")
    public Map login(HttpSession session) {
        Instant now = Instant.now();

        log.info("Session type: {}, id: {}", session.getClass(), session.getId());
        log.info("Last Request: {}", session.getAttribute(SESSION_ATTRIBUTE_LAST_REQUEST));
        log.info("This Request: {}", now);

        if (testSessionBean.getSessionId() != null) {
            log.info("Existing session value: {}", testSessionBean.getSessionId());
        } else {
            testSessionBean.setSessionId(session.getId());
        }

        if (testSessionBean.getCount() != null) {
            testSessionBean.setCount(testSessionBean.getCount() + 1);
        }

        if (session.getAttribute(SESSION_ATTRIBUTE_COUNT) == null) {
            session.setAttribute(SESSION_ATTRIBUTE_COUNT, 1);
        } else {
            session.setAttribute(SESSION_ATTRIBUTE_COUNT, (Integer) session.getAttribute(SESSION_ATTRIBUTE_COUNT) + 1);
        }

        session.setAttribute(SESSION_ATTRIBUTE_LAST_REQUEST, now);
        testSessionBean.setLastRequest(now);

        Map<String, Object> map = new HashMap<>();
        map.put("session_id", session.getId());
        map.put("session_attribute_last_request", session.getAttribute(SESSION_ATTRIBUTE_LAST_REQUEST));
        map.put("session_attribute_count", session.getAttribute(SESSION_ATTRIBUTE_COUNT));
        map.put("session_bean_count", testSessionBean.getCount());
        map.put("session_bean_session_id", testSessionBean.getSessionId());
        map.put("session_bean_last_request", testSessionBean.getLastRequest());

        // Specific for App B.
        ClassOnlyInAppB classOnlyInAppB = new ClassOnlyInAppB();
        classOnlyInAppB.setValue(session.getId());
        session.setAttribute("session_attribute_app_b_class", classOnlyInAppB);
        map.put("session_attribute_app_b_class", classOnlyInAppB);

        map.put("session_attribute_app_b_value", session.getId());

        return map;
    }
}

@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
class TestSessionBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer count = 0;

    private Instant lastRequest;

    private String sessionId;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Instant getLastRequest() {
        return lastRequest;
    }

    public void setLastRequest(Instant lastRequest) {
        this.lastRequest = lastRequest;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}

