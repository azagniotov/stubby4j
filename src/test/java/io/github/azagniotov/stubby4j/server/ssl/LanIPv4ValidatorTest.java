/*
 * Copyright (c) 2012-2024 Alexander Zagniotov
 *
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

package io.github.azagniotov.stubby4j.server.ssl;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

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
