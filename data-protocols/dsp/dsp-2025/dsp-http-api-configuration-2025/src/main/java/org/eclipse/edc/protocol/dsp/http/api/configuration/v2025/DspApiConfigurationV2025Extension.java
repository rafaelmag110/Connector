/*
 *  Copyright (c) 2025 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

package org.eclipse.edc.protocol.dsp.http.api.configuration.v2025;

import jakarta.json.Json;
import org.eclipse.edc.connector.controlplane.transform.edc.from.JsonObjectFromAssetTransformer;
import org.eclipse.edc.connector.controlplane.transform.edc.to.JsonObjectToAssetTransformer;
import org.eclipse.edc.connector.controlplane.transform.odrl.OdrlTransformersFactory;
import org.eclipse.edc.connector.controlplane.transform.odrl.from.JsonObjectFromPolicyTransformer;
import org.eclipse.edc.jsonld.spi.JsonLd;
import org.eclipse.edc.participant.spi.ParticipantIdMapper;
import org.eclipse.edc.protocol.dsp.http.spi.dispatcher.DspHttpRemoteMessageDispatcher;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.protocol.ProtocolWebhookRegistry;
import org.eclipse.edc.spi.system.Hostname;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.transform.transformer.dspace.to.JsonObjectToDataAddressDspaceTransformer;
import org.eclipse.edc.transform.transformer.dspace.v2024.from.JsonObjectFromDataAddressDspace2024Transformer;
import org.eclipse.edc.transform.transformer.edc.from.JsonObjectFromCriterionTransformer;
import org.eclipse.edc.transform.transformer.edc.from.JsonObjectFromQuerySpecTransformer;
import org.eclipse.edc.transform.transformer.edc.to.JsonObjectToCriterionTransformer;
import org.eclipse.edc.transform.transformer.edc.to.JsonObjectToQuerySpecTransformer;
import org.eclipse.edc.transform.transformer.edc.to.JsonValueToGenericTypeTransformer;
import org.eclipse.edc.web.spi.configuration.ApiContext;
import org.eclipse.edc.web.spi.configuration.PortMapping;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.eclipse.edc.jsonld.spi.Namespaces.DSPACE_CONTEXT_2025_1;
import static org.eclipse.edc.jsonld.spi.Namespaces.EDC_DSPACE_CONTEXT;
import static org.eclipse.edc.protocol.dsp.http.spi.types.HttpMessageProtocol.DATASPACE_PROTOCOL_HTTP_V_2025_1;
import static org.eclipse.edc.protocol.dsp.spi.type.DspConstants.DSP_NAMESPACE_V_2025_1;
import static org.eclipse.edc.protocol.dsp.spi.type.DspConstants.DSP_SCOPE_V_2025_1;
import static org.eclipse.edc.protocol.dsp.spi.type.DspConstants.DSP_TRANSFORMER_CONTEXT_V_2025_1;
import static org.eclipse.edc.protocol.dsp.spi.version.DspVersions.V_2025_1_PATH;
import static org.eclipse.edc.spi.constants.CoreConstants.JSON_LD;

/**
 * Configure 'protocol' api context.
 */
@Extension(value = DspApiConfigurationV2025Extension.NAME)
public class DspApiConfigurationV2025Extension implements ServiceExtension {

    public static final String NAME = "Dataspace Protocol 2025/1 API Configuration Extension";

    static final String DEFAULT_PROTOCOL_PATH = "/api/protocol";
    static final int DEFAULT_PROTOCOL_PORT = 8282;

    @Setting(description = "Configures endpoint for reaching the Protocol API in the form \"<hostname:protocol.port/protocol.path>\"", key = "edc.dsp.callback.address", required = false)
    private String callbackAddress;

    @Configuration
    private DspApiConfiguration apiConfiguration;

    @Inject
    private TypeManager typeManager;
    @Inject
    private JsonLd jsonLd;
    @Inject
    private TypeTransformerRegistry transformerRegistry;
    @Inject
    private ParticipantIdMapper participantIdMapper;
    @Inject
    private RemoteMessageDispatcherRegistry dispatcherRegistry;
    @Inject
    private DspHttpRemoteMessageDispatcher dispatcher;

    @Inject
    private ProtocolWebhookRegistry protocolWebhookRegistry;

    @Inject
    private Hostname hostname;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var portMapping = new PortMapping(ApiContext.PROTOCOL, apiConfiguration.port(), apiConfiguration.path());

        // registers ns for DSP 2025/1 scope
        registerNamespaces();
        registerTransformers();
        dispatcherRegistry.register(DATASPACE_PROTOCOL_HTTP_V_2025_1, dispatcher);

        var dspWebhookAddress = ofNullable(callbackAddress).orElseGet(() -> format("http://%s:%s%s", hostname.get(), portMapping.port(), portMapping.path()));

        protocolWebhookRegistry.registerWebhook(DATASPACE_PROTOCOL_HTTP_V_2025_1, () -> dspWebhookAddress + V_2025_1_PATH);

    }

    private void registerNamespaces() {
        jsonLd.registerContext(DSPACE_CONTEXT_2025_1, DSP_SCOPE_V_2025_1);
        jsonLd.registerContext(EDC_DSPACE_CONTEXT, DSP_SCOPE_V_2025_1);
    }

    private void registerTransformers() {
        var jsonBuilderFactory = Json.createBuilderFactory(Map.of());

        // EDC model to JSON-LD transformers
        var dspApiTransformerRegistry = transformerRegistry.forContext(DSP_TRANSFORMER_CONTEXT_V_2025_1);
        dspApiTransformerRegistry.register(new JsonObjectFromAssetTransformer(jsonBuilderFactory, typeManager, JSON_LD));
        dspApiTransformerRegistry.register(new JsonObjectFromQuerySpecTransformer(jsonBuilderFactory));
        dspApiTransformerRegistry.register(new JsonObjectFromCriterionTransformer(jsonBuilderFactory, typeManager, JSON_LD));

        // JSON-LD to EDC model transformers
        // ODRL Transformers
        OdrlTransformersFactory.jsonObjectToOdrlTransformers(participantIdMapper).forEach(dspApiTransformerRegistry::register);

        dspApiTransformerRegistry.register(new JsonValueToGenericTypeTransformer(typeManager, JSON_LD));
        dspApiTransformerRegistry.register(new JsonObjectToAssetTransformer());
        dspApiTransformerRegistry.register(new JsonObjectToQuerySpecTransformer());
        dspApiTransformerRegistry.register(new JsonObjectToCriterionTransformer());
        dspApiTransformerRegistry.register(new JsonObjectToDataAddressDspaceTransformer(DSP_NAMESPACE_V_2025_1));
        dspApiTransformerRegistry.register(new JsonObjectFromPolicyTransformer(jsonBuilderFactory, participantIdMapper, true));
        dspApiTransformerRegistry.register(new JsonObjectFromDataAddressDspace2024Transformer(jsonBuilderFactory, typeManager, JSON_LD, DSP_NAMESPACE_V_2025_1));
    }

    @Settings
    record DspApiConfiguration(
            @Setting(key = "web.http." + ApiContext.PROTOCOL + ".port", description = "Port for " + ApiContext.PROTOCOL + " api context", defaultValue = DEFAULT_PROTOCOL_PORT + "")
            int port,
            @Setting(key = "web.http." + ApiContext.PROTOCOL + ".path", description = "Path for " + ApiContext.PROTOCOL + " api context", defaultValue = DEFAULT_PROTOCOL_PATH)
            String path
    ) {

    }
}
