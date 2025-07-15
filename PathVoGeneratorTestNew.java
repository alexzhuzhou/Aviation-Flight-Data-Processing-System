package br.atech.pista.stats;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;

import br.atech.gsa.commons.helper.IMergeSupport;
import br.atech.gsa.commons.helper.ReplaySerializer;
import br.atech.gsa.commons.historic.ReplayPath;

@ContextConfiguration(classes = PathVoGeneratorTest.Config.class)
@SuppressWarnings({ "all", "java:S2699" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PathVoGeneratorTest extends AbstractJUnit4SpringContextTests {

	@Configuration
	@EnableTransactionManagement
	@ComponentScan(basePackages = "br.atech.pista.dam")
	public static class Config {

		@Bean
		public DataSource ds() {
			final var url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=10.103.3.8)(PORT=1521))(CONNECT_DATA=(SERVER=DEDICATED)(service_name=SIGMA_PLT3_DEV1_APP)))";
			final var ds = new BasicDataSource();
			ds.setUrl(url);
			ds.setUsername("sigma");
			ds.setPassword("mudar123");
			ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
			ds.setTestOnBorrow(true);

			return ds;
		}

		@Bean("jdbcTemplateSigma")
		public JdbcOperations ops() {
			final var template = new JdbcTemplate();
			template.setDataSource(ds());
			return template;
		}

		@Bean
		public JtaTransactionManager transactionManager() {
			final var txm = new JtaTransactionManager();
			txm.setTransactionManager(new TransactionManagerImple());
			txm.setUserTransaction(new UserTransactionImple());
			return txm;
		}
	}

	@Autowired
	IMergeSupport repo;

	Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void _00_run() throws Exception {
        // 1) stream all packets for 2025-07-11
        var streamPackets = repo.streamPackets(LocalDate.of(2025, 7, 11));

        // 2) collectors and snapshot container
        List<PathVo> allPoints            = new ArrayList<>();
        List<FlightIntentionVo> allIntents = new ArrayList<>();
        AtomicLong snapshotTime          = new AtomicLong(-1L);

        // 3) process every packet
        streamPackets.forEach(packet -> {
            // extract and deserialize
            final byte[] value         = packet.getValue();
            final ReplayPath input     = ReplaySerializer.input(value);

            // capture the 'time' from the first ReplayPath
            if (snapshotTime.get() < 0) {
                snapshotTime.set(input.getTime());
            }

            // accumulate both lists
            allPoints .addAll(input.getListRealPath());
            allIntents.addAll(input.getListFlightIntention());
        });

        // 4) wrap under three top‐level keys
        Map<String,Object> output = Map.of(
            "listRealPath",         allPoints,
            "listFlightIntention",  allIntents,
            "time",                 snapshotTime.get()
        );

        // 5) serialize to pretty JSON
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String finalJson = mapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(output);

		// … after you build finalJson …
		Files.writeString(Path.of("replay.json"), finalJson, StandardCharsets.UTF_8);
		log.info("Wrote JSON to replay.json");
    }
}


