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

import org.xmlunit.XMLUnitException;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.placeholder.PlaceholderHandler;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.xmlunit.diff.ComparisonResult.DIFFERENT;
import static org.xmlunit.diff.ComparisonResult.EQUAL;

/**
 * Handler for the {@code matchesRegex()} placeholder keyword.
 *
 * @since 2.7.0
 */
public class Stubby4jMatchesRegexPlaceholderHandler implements PlaceholderHandler {
    private static final String PLACEHOLDER_NAME = "matchesRegex";
    private final String tokenTemplateName;
    private final Map<String, String> stubbedRequestRegexGroups;

    public Stubby4jMatchesRegexPlaceholderHandler(final String tokenTemplateName, final Map<String, String> stubbedRequestRegexGroups) {
        this.tokenTemplateName = tokenTemplateName;
        this.stubbedRequestRegexGroups = stubbedRequestRegexGroups;
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
        return pattern.matcher(testText).find();
    }
}
