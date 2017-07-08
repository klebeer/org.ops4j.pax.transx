/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.transx.itests;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.transx.connector.ConnectionManagerFactory;
import org.ops4j.pax.transx.jdbc.ManagedConnectionFactoryFactory;
import org.ops4j.pax.transx.tm.TransactionManager;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.inject.Inject;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.sql.DataSource;
import java.util.Properties;

import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.transx.itests.TestConfiguration.mvnBundle;
import static org.ops4j.pax.transx.itests.TestConfiguration.regressionDefaults;

@RunWith(PaxExam.class)
public class GeronimoTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Inject
    @Filter(value = "(osgi.jdbc.driver.name=H2 JDBC Driver)")
    private DataSourceFactory dsf;

    @Inject
    private TransactionManager tm;

    @Configuration
    public Option[] config() throws Exception {
        return options(
                regressionDefaults(),
                mvnBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec"),
                mvnBundle("org.apache.geronimo.specs", "geronimo-j2ee-connector_1.6_spec"),
                mvnBundle("org.apache.geronimo.specs", "geronimo-jms_2.0_spec"),
                mvnBundle("org.ops4j.pax.transx", "transx-tm-api"),
                mvnBundle("org.ops4j.pax.transx", "transx-tm-geronimo"),
                mvnBundle("org.ops4j.pax.transx", "transx-connector"),
                mvnBundle("org.ops4j.pax.transx", "transx-jms"),
                mvnBundle("org.ops4j.pax.transx", "transx-jdbc"),
                mvnBundle("com.h2database", "h2")
        );
    }

    @Test
    public void createJdbcResource() throws Exception {
        System.out.println("DSF: " + dsf);

        Properties jdbc = new Properties();
        jdbc.setProperty("url", "jdbc:h2:mem:test");
        jdbc.setProperty("user", "sa");
        jdbc.setProperty("password", "");
        ManagedConnectionFactory mcf = ManagedConnectionFactoryFactory.create(dsf.createXADataSource(jdbc));
        ConnectionManager cm = ConnectionManagerFactory.builder()
                .transaction(ConnectionManagerFactory.TransactionSupportLevel.Xa)
                .managedConnectionFactory(mcf)
                .transactionManager(tm)
                .name("h2")
                .build();
        DataSource ds = (DataSource) mcf.createConnectionFactory(cm);

    }

}
