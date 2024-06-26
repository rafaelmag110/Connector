/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

plugins {
    `java-library`
    `java-test-fixtures`
}


dependencies {
    api(project(":spi:common:core-spi"))

    testImplementation(project(":tests:junit-base"))
    testImplementation(project(":core:common:lib:json-lib"))

    // needed by the abstract test spec located in testFixtures
    testFixturesImplementation(libs.bundles.jupiter)
    testFixturesImplementation(libs.mockito.core)
    testFixturesImplementation(libs.assertj)
    testFixturesRuntimeOnly(libs.junit.jupiter.engine)
}


