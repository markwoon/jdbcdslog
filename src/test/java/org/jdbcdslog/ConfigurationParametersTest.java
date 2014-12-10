package org.jdbcdslog;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Properties;

import org.junit.Test;

/**
 *
 * @author ShunLi
 */
public class ConfigurationParametersTest {

    @Test
    public void testInitSlowQueryTheshold_NotDefined_ThenAssumeMilliSecond() {
        // Given
        Properties mockProperties = mock(Properties.class);
        ConfigurationParameters.props = mockProperties;
        long defaultThreshold = ConfigurationParameters.slowQueryThresholdInNano;

        when(mockProperties.getProperty("jdbcdslog.slowQueryThreshold")).thenReturn(null);

        // When
        ConfigurationParameters.initSlowQueryThreshold();

        // Then
        assertEquals("slowQueryTheshold", defaultThreshold, ConfigurationParameters.slowQueryThresholdInNano);
    }

    @Test
    public void testInitSlowQueryTheshold_GivenNoUnit_ThenAssumeMilliSecond() {
        // Given
        Properties mockProperties = mock(Properties.class);
        ConfigurationParameters.props = mockProperties;

        when(mockProperties.getProperty("jdbcdslog.slowQueryThreshold")).thenReturn("123"); // 123ms

        // When
        ConfigurationParameters.initSlowQueryThreshold();

        // Then
        assertEquals("slowQueryTheshold", 123000000, ConfigurationParameters.slowQueryThresholdInNano);
    }


    @Test
    public void testInitSlowQueryTheshold_GivenNs_ThenAssumeNanoSecond() {
        // Given
        Properties mockProperties = mock(Properties.class);
        ConfigurationParameters.props = mockProperties;

        when(mockProperties.getProperty("jdbcdslog.slowQueryThreshold")).thenReturn("123ns"); // 123ns

        // When
        ConfigurationParameters.initSlowQueryThreshold();

        // Then
        assertEquals("slowQueryTheshold", 123, ConfigurationParameters.slowQueryThresholdInNano);
    }


    @Test
    public void testInitSlowQueryTheshold_GivenMs_ThenAssumeMilliSecond() {
        // Given
        Properties mockProperties = mock(Properties.class);
        ConfigurationParameters.props = mockProperties;

        when(mockProperties.getProperty("jdbcdslog.slowQueryThreshold")).thenReturn("123ms"); // 123ms

        // When
        ConfigurationParameters.initSlowQueryThreshold();

        // Then
        assertEquals("slowQueryTheshold", 123L * 1000000L, ConfigurationParameters.slowQueryThresholdInNano);
    }


    @Test
    public void testInitSlowQueryTheshold_GivenSecond_ThenAssumeSecond() {
        // Given
        Properties mockProperties = mock(Properties.class);
        ConfigurationParameters.props = mockProperties;

        when(mockProperties.getProperty("jdbcdslog.slowQueryThreshold")).thenReturn("123s"); // 123ns

        // When
        ConfigurationParameters.initSlowQueryThreshold();

        // Then
        assertEquals("slowQueryTheshold", 123L * 1000000L * 1000L, ConfigurationParameters.slowQueryThresholdInNano);
    }
}
