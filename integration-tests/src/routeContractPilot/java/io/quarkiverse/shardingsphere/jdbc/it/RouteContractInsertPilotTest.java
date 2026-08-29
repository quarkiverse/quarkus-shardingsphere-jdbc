package io.quarkiverse.shardingsphere.jdbc.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.github.ym0506.routecontract.RouteAssertions;
import io.github.ym0506.routecontract.RouteContract;
import io.github.ym0506.routecontract.RouteSnapshot;
import io.github.ym0506.routecontract.manifest.DataSourceAliases;
import io.github.ym0506.routecontract.manifest.ManifestAssertions;
import io.github.ym0506.routecontract.manifest.ManifestPolicy;
import io.github.ym0506.routecontract.manifest.ManifestStore;
import io.github.ym0506.routecontract.manifest.ManifestVerifier;
import io.github.ym0506.routecontract.manifest.ObservedExecutionManifest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class RouteContractInsertPilotTest {

    @Inject
    ShardingTablesResource resource;

    @Test
    public void keepsTheApprovedInsertStructure() throws Exception {
        assertEquals(0, resource.count("ds_0", "t_account_0"));
        assertEquals(0, resource.count("ds_0", "t_account_1"));
        assertEquals(0, resource.count("ds_1", "t_account_0"));
        assertEquals(0, resource.count("ds_1", "t_account_1"));

        Account account = new Account();
        account.setAccount_id(1);
        account.setUser_id(1);
        account.setStatus("true");

        RouteSnapshot snapshot = RouteContract.capture("accounts.insert", () -> resource.createAccount(account));

        // Keep the representative test's complete business result: only ds_1.t_account_1 changes.
        assertEquals(0, resource.count("ds_0", "t_account_0"));
        assertEquals(0, resource.count("ds_0", "t_account_1"));
        assertEquals(0, resource.count("ds_1", "t_account_0"));
        assertEquals(1, resource.count("ds_1", "t_account_1"));
        RouteAssertions.assertThat(snapshot)
                .hasCompleteCapture()
                .hasNoReportedExecutionFailures()
                .hasExactlyObservedPhysicalAttempts(1)
                .observesExactlyDataSourceNames("ds_1");

        int proposedMaxAttempts = 1;
        int proposedMaxDataSources = 1;
        ManifestPolicy policy = ManifestPolicy.strict(proposedMaxAttempts, proposedMaxDataSources);
        DataSourceAliases aliases = DataSourceAliases.of(Map.of(
                "ds_0", "account-shard-a",
                "ds_1", "account-shard-b"));
        ObservedExecutionManifest candidate = ObservedExecutionManifest.from(snapshot, aliases, policy);

        Path projectDir = Path.of(System.getProperty("routecontract.projectDir")).toAbsolutePath().normalize();
        Path approvedPath = projectDir.resolve(
                "src/routeContractPilot/resources/route-contracts/accounts.insert.json");
        Path candidatePath = projectDir.resolve("target/routecontract/accounts.insert.candidate.json");
        ManifestStore store = new ManifestStore();
        store.writeCandidate(approvedPath, candidatePath, candidate);

        System.out.printf(
                "ROUTECONTRACT_QUARKIVERSE_PILOT businessRows=0->1 attempts=%d dataSources=%s status=%s%n",
                snapshot.observedPhysicalAttemptCount(), snapshot.observedDataSourceNames(), snapshot.status());

        if (Files.notExists(approvedPath)) {
            fail("No approved baseline. Review target/routecontract/accounts.insert.candidate.json "
                    + "and copy its exact bytes only after human approval.");
        }

        ManifestAssertions.assertMatched(
                new ManifestVerifier().verify(store.read(approvedPath), candidate));
    }
}
