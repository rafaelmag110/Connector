/*
 *  Copyright (c) 2021 Daimler TSS GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Daimler TSS GmbH - Initial build file
 *
 */

plugins {
    `java-library`
    `java-test-fixtures`
    `maven-publish`
}


dependencies {
    api(project(":spi:common:core-spi"))
    api(project(":spi:common:transaction-spi"))
    implementation(project(":core:common:lib:util-lib"))
    implementation(project(":spi:common:transaction-datasource-spi"))


    testImplementation(project(":core:common:junit"))
    testFixturesImplementation(project(":spi:common:transaction-datasource-spi"))
    testFixturesImplementation(project(":core:common:junit"))
    testFixturesImplementation(libs.junit.jupiter.api)
    testFixturesImplementation(libs.mockito.core)
    testFixturesImplementation(libs.postgres)

    testFixturesImplementation(libs.testcontainers.junit)
    testFixturesImplementation(libs.testcontainers.postgres)

}


