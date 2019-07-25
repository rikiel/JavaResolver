package eu.profinit.manta.connector.java.analysis.mybatis.handler.configuration;

import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.utils.FileContentReader;
import eu.profinit.manta.connector.java.analysis.utils.exception.XmlException;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class MyBatisConfigurationReaderTest extends AbstractTest {
    @Test(dataProvider = "testParseConfigFileDataProvider")
    public void testParseConfigFile(String usedEnvironment, ConnectionConfiguration expectedConfiguration) throws XmlException {
        final MyBatisConfigurationReader reader = new MyBatisConfigurationReader("config/MyBatisConfigurationReaderTest/MyBatisConfiguration.xml",
                new FileContentReader());
        final Map<String, ConnectionConfiguration> configurationMap = reader.parseConfigFile();

        assertReflectionEquals(expectedConfiguration, configurationMap.get(usedEnvironment));
    }

    @Test(expectedExceptions = XmlException.class, expectedExceptionsMessageRegExp = "Failed to read file some/path")
    public void testParseConfigFileNotExistError() throws XmlException {
        new MyBatisConfigurationReader("some/path", new FileContentReader());
    }

    @Test(dataProvider = "testParseConfigErrorDataProvider", expectedExceptions = XmlException.class, expectedExceptionsMessageRegExp = "Failed to parse config file .*")
    public void testParseConfigFileError(String fileName) throws XmlException {
        new MyBatisConfigurationReader(fileName, new FileContentReader()).parseConfigFile();
    }

    @DataProvider
    private Object[][] testParseConfigFileDataProvider() {
        final ConnectionConfiguration expectedDevelopmentConfiguration = new ConnectionConfiguration(
                "org.postgresql.Driver",
                "jdbc:postgresql:mydb",
                "usernameValue");
        final ConnectionConfiguration expectedTestConfiguration = new ConnectionConfiguration(
                "someTestDriver",
                "someTestUrl",
                "someTestUsername");
        return new Object[][] {
                { "development", expectedDevelopmentConfiguration },
                { "test", expectedTestConfiguration },
                { MyBatisConfigurationReader.DEFAULT_ENVIRONMENT_NAME, expectedDevelopmentConfiguration },
                };
    }

    @DataProvider
    private Object[][] testParseConfigErrorDataProvider() {
        return new Object[][] {
                { "config/MyBatisConfigurationReaderTest/MyBatisConfigurationMalformed.xml" },
                { "config/MyBatisConfigurationReaderTest/MyBatisConfigurationMalformed2.xml" },
                };
    }
}