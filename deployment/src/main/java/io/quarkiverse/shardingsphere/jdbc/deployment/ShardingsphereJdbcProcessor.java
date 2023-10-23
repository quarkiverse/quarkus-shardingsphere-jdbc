package io.quarkiverse.shardingsphere.jdbc.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.shardingsphere.authority.spi.AuthorityRegistryProvider;
import org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereURLProvider;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;
import org.apache.shardingsphere.infra.util.yaml.shortcuts.ShardingSphereYamlShortcuts;
import org.apache.shardingsphere.infra.yaml.config.shortcut.YamlRuleConfigurationShortcuts;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.JDBCRepository;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sql.parser.api.parser.SQLLexer;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sqlfederation.compiler.planner.util.SQLFederationFunctionUtils;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;
import org.apache.shardingsphere.timeservice.spi.TimestampService;
import org.codehaus.groovy.reflection.GeneratedMetaMethod.DgmMethodRecord;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import groovy.lang.Closure;
import io.quarkiverse.shardingsphere.jdbc.QuarkusDataSource;
import io.quarkiverse.shardingsphere.jdbc.ShardingSphereAgroalConnectionConfigurer;
import io.quarkiverse.shardingsphere.jdbc.ShardingsphereJdbcRecorder;
import io.quarkus.agroal.spi.JdbcDriverBuildItem;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.datasource.deployment.spi.DefaultDataSourceDbKindBuildItem;
import io.quarkus.datasource.deployment.spi.DevServicesDatasourceConfigurationHandlerBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.SslNativeConfigBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;

public class ShardingsphereJdbcProcessor {

    private static final String FEATURE = "shardingsphere-jdbc";
    private static final String SHARDING_SPHERE_DB_KIND = "shardingsphere";
    private static final DotName YAML_CONFIGURATION = DotName.createSimple(YamlConfiguration.class);
    private static final DotName STANDALONE_PERSIST_REPOSITORY = DotName.createSimple(StandalonePersistRepository.class);
    private static final DotName DRIVER_URL_PROVIDER = DotName.createSimple(ShardingSphereURLProvider.class);
    private static final DotName SHARDING_ALGORITHM = DotName.createSimple(ShardingAlgorithm.class);
    private static final DotName KEY_GENERATE_ALGORITHM = DotName.createSimple(KeyGenerateAlgorithm.class);
    private static final DotName AUTHORITY_REGISTRY_PROVIDER = DotName.createSimple(AuthorityRegistryProvider.class);
    private static final DotName SQL_FEDERATION_DECIDER = DotName.createSimple(SQLFederationDecider.class);
    private static final DotName DML_STATEMENT = DotName.createSimple(DMLStatement.class);
    private static final DotName TIME_SERVICE = DotName.createSimple(TimestampService.class);
    private static final DotName SQL_VISITOR_FACADE = DotName.createSimple(SQLStatementVisitorFacade.class);
    private static final DotName SQL_LEXER = DotName.createSimple(SQLLexer.class);
    private static final DotName SQL_PARSER = DotName.createSimple(SQLParser.class);
    private static final DotName SQL_VISITOR = DotName.createSimple(SQLVisitor.class);
    private static final Logger LOG = Logger.getLogger(ShardingsphereJdbcProcessor.class);

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerDriver(BuildProducer<JdbcDriverBuildItem> jdbcDriver,
            SslNativeConfigBuildItem sslNativeConfigBuildItem) {
        jdbcDriver.produce(
                new JdbcDriverBuildItem(SHARDING_SPHERE_DB_KIND, "org.apache.shardingsphere.driver.ShardingSphereDriver"));
    }

    @BuildStep
    void configureAgroalConnection(BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            Capabilities capabilities) {
        if (capabilities.isPresent(Capability.AGROAL)) {
            additionalBeans
                    .produce(new AdditionalBeanBuildItem.Builder().addBeanClass(ShardingSphereAgroalConnectionConfigurer.class)
                            .setDefaultScope(BuiltinScope.APPLICATION.getName())
                            .setUnremovable()
                            .build());
        }
    }

    @BuildStep
    DevServicesDatasourceConfigurationHandlerBuildItem devDbHandler() {
        return DevServicesDatasourceConfigurationHandlerBuildItem.jdbc(SHARDING_SPHERE_DB_KIND);
    }

    @BuildStep
    void registerDefaultDbType(BuildProducer<DefaultDataSourceDbKindBuildItem> dbKind) {
        dbKind.produce(new DefaultDataSourceDbKindBuildItem(SHARDING_SPHERE_DB_KIND));
    }

    @BuildStep
    void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
    }

    @BuildStep
    void registerReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClasses,
            BuildProducer<ServiceProviderBuildItem> services,
            CombinedIndexBuildItem indexBuildItem) {
        indexBuildItem.getIndex().getAnnotations(SingletonSPI.class).forEach(
                spi -> register(indexBuildItem.getIndex(), spi.target().asClass().name(), services, reflectiveClasses, true));
        register(indexBuildItem.getIndex(), YAML_CONFIGURATION, services, reflectiveClasses, false);
        register(indexBuildItem.getIndex(), SQL_LEXER, services, reflectiveClasses, false);
        register(indexBuildItem.getIndex(), SQL_PARSER, services, reflectiveClasses, false);
        register(indexBuildItem.getIndex(), SQL_VISITOR, services, reflectiveClasses, false);
        register(indexBuildItem.getIndex(), DML_STATEMENT, services, reflectiveClasses, false);
        register(indexBuildItem.getIndex(), STANDALONE_PERSIST_REPOSITORY, services, reflectiveClasses, true);
        register(indexBuildItem.getIndex(), DRIVER_URL_PROVIDER, services, reflectiveClasses, true);
        register(indexBuildItem.getIndex(), SHARDING_ALGORITHM, services, reflectiveClasses, true);
        register(indexBuildItem.getIndex(), KEY_GENERATE_ALGORITHM, services, reflectiveClasses, true);
        register(indexBuildItem.getIndex(), AUTHORITY_REGISTRY_PROVIDER, services, reflectiveClasses, true);
        register(indexBuildItem.getIndex(), SQL_FEDERATION_DECIDER, services, reflectiveClasses, true);
        register(indexBuildItem.getIndex(), TIME_SERVICE, services, reflectiveClasses, true);
        register(indexBuildItem.getIndex(), SQL_VISITOR_FACADE, services, reflectiveClasses, true);

        reflectiveClasses.produce(new ReflectiveClassBuildItem(false, false, YamlRuleConfigurationShortcuts.class));
        reflectiveClasses.produce(new ReflectiveClassBuildItem(false, false, Properties.class));
        reflectiveClasses.produce(new ReflectiveClassBuildItem(false, false, JDBCRepository.class));
        reflectiveClasses.produce(new ReflectiveClassBuildItem(true, false, QuarkusDataSource.class));
        reflectiveClasses.produce(new ReflectiveClassBuildItem(true, false, ScriptBytecodeAdapter.class));
        reflectiveClasses.produce(new ReflectiveClassBuildItem(true, false, SQLFederationFunctionUtils.class));
        reflectiveClasses.produce(new ReflectiveClassBuildItem(true, false, Closure.class));
        reflectiveClasses.produce(new ReflectiveClassBuildItem(true, false, "com.github.benmanes.caffeine.cache.SIMS"));
        reflectiveClasses.produce(new ReflectiveClassBuildItem(true, false, "org.slf4j.event.Level"));

        try {
            List<String> methods = List.of("mod");
            DgmMethodRecord.loadDgmInfo().stream().filter(r -> methods.contains(r.methodName)).forEach(
                    r -> reflectiveClasses.produce(new ReflectiveClassBuildItem(true, false, r.className.replace('/', '.'))));
        } catch (IOException e) {
            LOG.warn("Can not add dgm methods reflection", e);
        }
        services.produce(new ServiceProviderBuildItem(ShardingSphereYamlShortcuts.class.getName(),
                YamlRuleConfigurationShortcuts.class.getName()));
    }

    @BuildStep
    void registerRuntimeInit(BuildProducer<RuntimeInitializedClassBuildItem> initializedClasses) {
    }

    @BuildStep
    @Record(STATIC_INIT)
    void doEvalRules(ShardingsphereJdbcRecorder recorder) {
        recorder.evalRules();
    }

    private void register(IndexView index, DotName serviceName, BuildProducer<ServiceProviderBuildItem> services,
            BuildProducer<ReflectiveClassBuildItem> reflects, boolean isService) {
        LOG.debug("register " + serviceName.toString());
        index.getAllKnownImplementors(serviceName).forEach(
                classInfo -> {
                    LOG.debug("add " + classInfo.name().toString());
                    reflects.produce(new ReflectiveClassBuildItem(true, false, classInfo.name().toString()));
                    if (isService) {
                        services.produce(new ServiceProviderBuildItem(serviceName.toString(), classInfo.name().toString()));
                    }
                });
    }
}
