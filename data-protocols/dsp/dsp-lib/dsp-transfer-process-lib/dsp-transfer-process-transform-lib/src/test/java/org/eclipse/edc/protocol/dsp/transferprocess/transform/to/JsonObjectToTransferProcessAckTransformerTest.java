/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.edc.protocol.dsp.transferprocess.transform.to;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import org.eclipse.edc.jsonld.spi.JsonLdNamespace;
import org.eclipse.edc.protocol.dsp.transferprocess.transform.type.to.JsonObjectToTransferProcessAckTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.protocol.dsp.spi.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_CONSUMER_PID_TERM;
import static org.eclipse.edc.protocol.dsp.spi.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_PROVIDER_PID_TERM;
import static org.eclipse.edc.protocol.dsp.spi.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_STATE_TERM;
import static org.eclipse.edc.protocol.dsp.spi.type.DspTransferProcessPropertyAndTypeNames.DSPACE_TYPE_TRANSFER_PROCESS_TERM;
import static org.eclipse.edc.protocol.dsp.transferprocess.transform.to.TestInput.getExpanded;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class JsonObjectToTransferProcessAckTransformerTest {

    private static final JsonLdNamespace DSP_NAMESPACE = new JsonLdNamespace("http://www.w3.org/ns/dsp#");
    private final TransformerContext context = mock();
    private final JsonBuilderFactory jsonFactory = Json.createBuilderFactory(Map.of());
    private final JsonObjectToTransferProcessAckTransformer transformer = new JsonObjectToTransferProcessAckTransformer(DSP_NAMESPACE);

    @Test
    void shouldTransform() {
        var message = jsonFactory.createObjectBuilder()
                .add(TYPE, DSP_NAMESPACE.toIri(DSPACE_TYPE_TRANSFER_PROCESS_TERM))
                .add(DSP_NAMESPACE.toIri(DSPACE_PROPERTY_CONSUMER_PID_TERM), "consumerPid")
                .add(DSP_NAMESPACE.toIri(DSPACE_PROPERTY_PROVIDER_PID_TERM), "providerPid")
                .add(DSP_NAMESPACE.toIri(DSPACE_PROPERTY_STATE_TERM), "STATE")
                .build();

        var result = transformer.transform(getExpanded(message), context);

        assertThat(result).isNotNull();
        assertThat(result.getConsumerPid()).isEqualTo("consumerPid");
        assertThat(result.getProviderPid()).isEqualTo("providerPid");
        assertThat(result.getState()).isEqualTo("STATE");
        verifyNoInteractions(context);
    }

}
