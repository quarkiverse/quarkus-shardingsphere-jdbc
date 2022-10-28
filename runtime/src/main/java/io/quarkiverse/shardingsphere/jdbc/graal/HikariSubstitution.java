package io.quarkiverse.shardingsphere.jdbc.graal;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.util.ConcurrentBag;

final public class HikariSubstitution {

}

@TargetClass(className = "com.zaxxer.hikari.pool.HikariPool")
final class HikariPoolSubstitution {
    @Alias
    public void setMetricsTrackerFactory(MetricsTrackerFactory metricsTrackerFactory) {
    }

    @Substitute
    public void setMetricRegistry(Object metricRegistry) {
        setMetricsTrackerFactory(null);
    }

    @Substitute
    public void setHealthCheckRegistry(Object healthCheckRegistry) {
        // do nothing
    }
}

@TargetClass(ConcurrentBag.class)
final class ConcurrentBagSubstitution {
    @Substitute
    private boolean useWeakThreadLocals() {
        return true;
    }
}
