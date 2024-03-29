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

package io.github.azagniotov.stubby4j.cli;

import static com.google.common.truth.Truth.assertThat;

import java.util.Map;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CommandLineInterpreterTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testIsHelpWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-h"});
        final boolean isHelp = commandLineInterpreter.isHelp();

        assertThat(isHelp).isTrue();
    }

    @Test
    public void testIsHelpWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--help"});
        final boolean isHelp = commandLineInterpreter.isHelp();

        assertThat(isHelp).isTrue();
    }

    @Test
    public void testIsVersionWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-v"});
        final boolean isVersion = commandLineInterpreter.isVersion();

        assertThat(isVersion).isTrue();
    }

    @Test
    public void testIsVersionWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--version"});
        final boolean isVersion = commandLineInterpreter.isVersion();

        assertThat(isVersion).isTrue();
    }

    @Test
    public void testIsDebugWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--debug"});
        final boolean isDebug = commandLineInterpreter.isDebug();

        assertThat(isDebug).isTrue();
    }

    @Test
    public void testIsDebugWhenShortgOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-o"});
        final boolean isDebug = commandLineInterpreter.isDebug();

        assertThat(isDebug).isTrue();
    }

    @Test
    public void testIsCacheDisabledWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--disable_stub_caching"});
        final boolean isCacheDisabled = commandLineInterpreter
                .getCommandlineParams()
                .containsKey(CommandLineInterpreter.OPTION_DISABLE_STUB_CACHING);

        assertThat(isCacheDisabled).isTrue();
    }

    @Test
    public void testIsCacheDisabledWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-dc"});
        final boolean isCacheDisabled = commandLineInterpreter
                .getCommandlineParams()
                .containsKey(CommandLineInterpreter.OPTION_DISABLE_STUB_CACHING);

        assertThat(isCacheDisabled).isTrue();
    }

    @Test
    public void testIsAdminPortalDisabledWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--disable_admin_portal"});
        final boolean isAdminDisabled =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_DISABLE_ADMIN);

        assertThat(isAdminDisabled).isTrue();
    }

    @Test
    public void testIsAdminPortalDisabledWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-da"});
        final boolean isAdminDisabled =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_DISABLE_ADMIN);

        assertThat(isAdminDisabled).isTrue();
    }

    @Test
    public void testIsSslDisabledWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--disable_ssl"});
        final boolean isSslDisabled =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_DISABLE_SSL);

        assertThat(isSslDisabled).isTrue();
    }

    @Test
    public void testIsSslDisabledWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-ds"});
        final boolean isSslDisabled =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_DISABLE_SSL);

        assertThat(isSslDisabled).isTrue();
    }

    @Test
    public void testIsMuteWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-m"});
        final boolean isMuteProvided = commandLineInterpreter.isMute();

        assertThat(isMuteProvided).isTrue();
    }

    @Test
    public void testIsMuteWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--mute"});
        final boolean isMuteProvided = commandLineInterpreter.isMute();

        assertThat(isMuteProvided).isTrue();
    }

    @Test
    public void testIsYamlProvidedWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-d", "somefilename.yaml"});
        final boolean isYamlProvided = commandLineInterpreter.isYamlProvided();

        assertThat(isYamlProvided).isTrue();
    }

    @Test
    public void testIsYamlProvidedWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--data", "somefilename.yaml"});
        final boolean isYamlProvided = commandLineInterpreter.isYamlProvided();

        assertThat(isYamlProvided).isTrue();
    }

    @Test
    public void testtHasAdminPortWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-a", "888"});
        final boolean isAdmin =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ADMINPORT);

        assertThat(isAdmin).isTrue();
    }

    @Test
    public void testHasAdminPortWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--admin", "8888"});
        final boolean isAdmin =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ADMINPORT);

        assertThat(isAdmin).isTrue();
    }

    @Test
    public void testtHasStubsPortWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-s", "888"});
        final boolean isAdmin =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_CLIENTPORT);

        assertThat(isAdmin).isTrue();
    }

    @Test
    public void testtHasStubsPortWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--stubs", "8888"});
        final boolean isAdmin =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_CLIENTPORT);

        assertThat(isAdmin).isTrue();
    }

    @Test
    public void testtIsWatchWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-w"});
        final boolean isWatch =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_WATCH);

        assertThat(isWatch).isTrue();
    }

    @Test
    public void testIsWatchWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--watch"});
        final boolean isWatch =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_WATCH);

        assertThat(isWatch).isTrue();
    }

    @Test
    public void testtHasKeystoreLocationWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-k", "some/path/to/key"});
        final boolean isKeystore =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_KEYSTORE);

        assertThat(isKeystore).isTrue();
    }

    @Test
    public void testHasKeystoreLocationWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--keystore", "some/path/to/key"});
        final boolean isKeystore =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_KEYSTORE);

        assertThat(isKeystore).isTrue();
    }

    @Test
    public void testtHasLocationWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-l", "hostname"});
        final boolean isLocation =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ADDRESS);

        assertThat(isLocation).isTrue();
    }

    @Test
    public void testHasLocationWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--location", "hostname"});
        final boolean isLocation =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ADDRESS);

        assertThat(isLocation).isTrue();
    }

    @Test
    public void testHasPasswordWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-p", "very-complex-password"});
        final boolean isPassword =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_KEYPASS);

        assertThat(isPassword).isTrue();
    }

    @Test
    public void testHasPasswordWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--password", "very-complex-password"});
        final boolean isPassword =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_KEYPASS);

        assertThat(isPassword).isTrue();
    }

    @Test
    public void testHasSslPortWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-t", "2443"});
        final boolean isSslGiven =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_TLSPORT);

        assertThat(isSslGiven).isTrue();
    }

    @Test
    public void testHasSslPortWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--tls", "2443"});
        final boolean isSslGiven =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_TLSPORT);

        assertThat(isSslGiven).isTrue();
    }

    @Test
    public void enabledTlsWithAlpnHttp2WhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-ta"});
        final boolean isTlsWithAlpnEnabled = commandLineInterpreter
                .getCommandlineParams()
                .containsKey(CommandLineInterpreter.OPTION_ENABLE_TLS_WITH_ALPN_AND_HTTP_2);

        assertThat(isTlsWithAlpnEnabled).isTrue();
    }

    @Test
    public void enabledTlsWithAlpnHttp2WhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--enable_tls_with_alpn_and_http_2"});
        final boolean isTlsWithAlpnEnabled = commandLineInterpreter
                .getCommandlineParams()
                .containsKey(CommandLineInterpreter.OPTION_ENABLE_TLS_WITH_ALPN_AND_HTTP_2);

        assertThat(isTlsWithAlpnEnabled).isTrue();
    }

    @Test
    public void shouldRemoveOptionTlsWithAlpnHttp2WhenDisableSslGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--enable_tls_with_alpn_and_http_2", "--disable_ssl"});
        final boolean isTlsWithAlpnEnabled = commandLineInterpreter
                .getCommandlineParams()
                .containsKey(CommandLineInterpreter.OPTION_ENABLE_TLS_WITH_ALPN_AND_HTTP_2);
        assertThat(isTlsWithAlpnEnabled).isFalse();

        final boolean disableSsl =
                commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_DISABLE_SSL);
        assertThat(disableSsl).isTrue();
    }

    @Test
    public void shouldBeFalseThatYamlIsNotProvided() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"cheburashka", "zagniotov"});
        final boolean isYamlProvided = commandLineInterpreter.isYamlProvided();

        assertThat(isYamlProvided).isFalse();
    }

    @Test
    public void shouldFailOnInvalidCommandlineLongOptionString() throws Exception {
        expectedException.expect(ParseException.class);

        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--cheburashka"});
    }

    @Test
    public void shouldFailOnInvalidCommandlineShortOptionString() throws Exception {
        expectedException.expect(ParseException.class);

        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-z"});
    }

    @Test
    public void shouldFailOnMissingArgumentForExistingShortOption() throws Exception {
        expectedException.expect(MissingArgumentException.class);

        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"-a"});
    }

    @Test
    public void shouldFailOnMissingArgumentForExistingLongOption() throws Exception {
        expectedException.expect(MissingArgumentException.class);

        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--data"});
    }

    @Test
    public void shouldReturnOneCommandlineParamWhenHelpArgPresent() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {"--help"});
        final Map<String, String> params = commandLineInterpreter.getCommandlineParams();

        assertThat(params.size()).isEqualTo(1);
    }

    @Test
    public void shouldReturnEmptyCommandlineParams() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {});
        final Map<String, String> params = commandLineInterpreter.getCommandlineParams();

        assertThat(params).isEmpty();
    }

    @Test
    public void shouldReturnCommandlineParams() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(
                new String[] {"--data", "somefilename.yaml", "-s", "12345", "--admin", "567"});
        final Map<String, String> params = commandLineInterpreter.getCommandlineParams();

        assertThat(params.size()).isEqualTo(3);
    }
}
