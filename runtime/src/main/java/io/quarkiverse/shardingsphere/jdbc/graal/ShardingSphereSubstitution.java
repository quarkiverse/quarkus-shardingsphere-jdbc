package io.quarkiverse.shardingsphere.jdbc.graal;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.shardingsphere.driver.jdbc.core.driver.spi.ApolloDriverURLProvider;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.yaml.swapper.NewYamlLoggingRuleConfigurationSwapper;
import org.apache.shardingsphere.logging.yaml.swapper.YamlLoggingRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.sql.JDBCRepositorySQL;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.sql.JDBCRepositorySQLLoader;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import io.quarkiverse.shardingsphere.jdbc.JBossLoggingRuleConfigurationBuilder;

final public class ShardingSphereSubstitution {
}

@TargetClass(ApolloDriverURLProvider.class)
final class ApolloDriverURLProviderSubstitution {
    @Substitute
    public byte[] getContent(final String url) {
        return new byte[0];
    }
}

@TargetClass(YamlLoggingRuleConfigurationSwapper.class)
final class YamlLoggingRuleConfigurationSwapperSubstitution {
    @Substitute
    private LoggingRuleConfiguration getDefaultLoggingRuleConfiguration() {
        return new JBossLoggingRuleConfigurationBuilder().build();
    }
}

@TargetClass(NewYamlLoggingRuleConfigurationSwapper.class)
final class NewYamlLoggingRuleConfigurationSwapperSubstitution {
    @Substitute
    private LoggingRuleConfiguration getDefaultLoggingRuleConfiguration() {
        return new JBossLoggingRuleConfigurationBuilder().build();
    }
}

@TargetClass(JDBCRepositorySQLLoader.class)
final class JDBCRepositorySQLLoaderSubstitution {
    @Alias
    private static String FILE_EXTENSION;

    @Substitute
    private static JDBCRepositorySQL loadFromDirectory(final URL url, final String type) throws IOException {
        FileSystem fileSystems = FileSystems.getDefault();
        try {
            if (url.getProtocol().equals("resource")) {
                fileSystems = FileSystems.newFileSystem(URI.create("resource:/"), Collections.EMPTY_MAP);
            }
            final JDBCRepositorySQL[] result = new JDBCRepositorySQL[1];
            Files.walkFileTree(fileSystems.getPath(url.getPath()), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
                    if (file.toString().endsWith(FILE_EXTENSION)) {
                        JDBCRepositorySQL provider = null;
                        try {
                            provider = (JDBCRepositorySQL) JAXBContext.newInstance(JDBCRepositorySQL.class).createUnmarshaller()
                                    .unmarshal(Files.newInputStream(file));
                        } catch (JAXBException e) {
                            throw new RuntimeException(e);
                        }
                        if (provider.isDefault()) {
                            result[0] = provider;
                        }
                        if (Objects.equals(provider.getType(), type)) {
                            result[0] = provider;
                            return FileVisitResult.TERMINATE;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return result[0];
        } finally {
            if (url.getProtocol().equals("resource")) {
                fileSystems.close();
            }
        }
    }
}
