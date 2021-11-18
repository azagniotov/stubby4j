package io.github.azagniotov.stubby4j.server.ssl;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class LanIPv4ValidatorTest {

    @Test
    public void isPrivateIp() throws Exception {
        assertThat(LanIPv4Validator.isPrivateIp("10.0.0.1")).isTrue();
        assertThat(LanIPv4Validator.isPrivateIp("10.255.255.255")).isTrue();
        assertThat(LanIPv4Validator.isPrivateIp("192.168.0.1")).isTrue();
        assertThat(LanIPv4Validator.isPrivateIp("192.168.255.255")).isTrue();
        assertThat(LanIPv4Validator.isPrivateIp("172.16.0.0")).isTrue();
        assertThat(LanIPv4Validator.isPrivateIp("172.16.0.1")).isTrue();
        assertThat(LanIPv4Validator.isPrivateIp("172.31.255.255")).isTrue();
    }

    @Test
    public void isNotPrivateIp() throws Exception {
        assertThat(LanIPv4Validator.isPrivateIp("9.0.0.1")).isFalse();
        assertThat(LanIPv4Validator.isPrivateIp("10.255.255.256")).isFalse();
        assertThat(LanIPv4Validator.isPrivateIp("192.169.0.1")).isFalse();
        assertThat(LanIPv4Validator.isPrivateIp("192.167.255.255")).isFalse();
        assertThat(LanIPv4Validator.isPrivateIp("172.15.0.0")).isFalse();
        assertThat(LanIPv4Validator.isPrivateIp("172.15.0.1")).isFalse();
        assertThat(LanIPv4Validator.isPrivateIp("172.32.255.255")).isFalse();
    }
}