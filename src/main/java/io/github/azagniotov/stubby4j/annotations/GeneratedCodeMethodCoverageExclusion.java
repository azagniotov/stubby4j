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

package io.github.azagniotov.stubby4j.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for exclusion of method from JaCoCo code coverage instrumentation.
 * <p>
 * Required by JaCoCo starting from v0.83
 * https://github.com/jacoco/jacoco/pull/822
 * https://www.jacoco.org/jacoco/trunk/doc/changes.html (Release 0.8.3 (2019/01/23))
 * https://github.com/jacoco/jacoco/blob/f72c2c865fa7c975debb1b3156120501843f5c74/org.jacoco.core/src/org/jacoco/core/internal/analysis/filter/AnnotationGeneratedFilter.java#L51
 * By default, annotation retention policy is RUNTIME, makes your annotation available to reflection
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface GeneratedCodeMethodCoverageExclusion {}
