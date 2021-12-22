/*
  This file is licensed to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package io.github.azagniotov.stubby4j.stubs.matching;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeClassCoverageExclusion;
import org.xmlunit.XMLUnitException;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.placeholder.PlaceholderHandler;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static io.github.azagniotov.stubby4j.utils.StringUtils.buildToken;
import static org.xmlunit.diff.ComparisonResult.DIFFERENT;
import static org.xmlunit.diff.ComparisonResult.EQUAL;

/**
 * Handler for the {@code matchesRegex()} placeholder keyword.
 *
 * @since 2.7.0
 */
@GeneratedCodeClassCoverageExclusion
public class Stubby4jMatchesRegexPlaceholderHandler implements PlaceholderHandler {
    private static final String PLACEHOLDER_NAME = "matchesRegex";
    private final String templateTokenName;
    private final Map<String, String> stubbedRequestRegexGroups;
    private AtomicInteger contentRegexGroupCounter;

    public Stubby4jMatchesRegexPlaceholderHandler(final String templateTokenName, final Map<String, String> stubbedRequestRegexGroups) {
        this.templateTokenName = templateTokenName;
        this.stubbedRequestRegexGroups = stubbedRequestRegexGroups;
        this.contentRegexGroupCounter = new AtomicInteger(1);
    }

    @Override
    public String getKeyword() {
        return PLACEHOLDER_NAME;
    }

    @Override
    public ComparisonResult evaluate(String testText, String... param) {
        if (param.length > 0 && param[0] != null && !param[0].equals("")) {
            try {
                final Pattern pattern = Pattern.compile(param[0].trim());
                if (testText != null && evaluate(testText.trim(), pattern)) {
                    return EQUAL;
                }
            } catch (PatternSyntaxException e) {
                throw new XMLUnitException(e.getMessage(), e);
            }
        }
        return DIFFERENT;
    }

    private boolean evaluate(final String testText, final Pattern pattern) {
        final Matcher matcher = pattern.matcher(testText);
        final boolean isMatch = matcher.find();
        if (isMatch) {
            final int groupId = contentRegexGroupCounter.getAndIncrement();
            this.stubbedRequestRegexGroups.put(buildToken(templateTokenName, groupId), matcher.group(0));
        }
        return isMatch;
    }
}
