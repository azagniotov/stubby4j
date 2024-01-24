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

import java.util.regex.Pattern;

public final class LanIPv4Validator {

    /**
     * 127.0.0.0    – 127.255.255.255     127.0.0.0 /8
     * 10.0.0.0     –  10.255.255.255      10.0.0.0 /8
     * 172.16.0.0   – 172. 31.255.255    172.16.0.0 /12
     * 192.168.0.0  – 192.168.255.255   192.168.0.0 /16
     */
    private static final String LAN_IPV4_PATTERN =
            "(^192\\.168\\.([0-9]|[0-9][0-9]|[0-2][0-5][0-5])\\.([0-9]|[0-9][0-9]|[0-2][0-5][0-5])$)|(^172\\.([1][6-9]|[2][0-9]|[3][0-1])\\.([0-9]|[0-9][0-9]|[0-2][0-5][0-5])\\.([0-9]|[0-9][0-9]|[0-2][0-5][0-5])$)|(^10\\.([0-9]|[0-9][0-9]|[0-2][0-5][0-5])\\.([0-9]|[0-9][0-9]|[0-2][0-5][0-5])\\.([0-9]|[0-9][0-9]|[0-2][0-5][0-5])$)";

    private static final Pattern PATTERN = Pattern.compile(LAN_IPV4_PATTERN);

    private LanIPv4Validator() {}

    static boolean isPrivateIp(final String privateIp) {
        return PATTERN.matcher(privateIp).matches();
    }
}
