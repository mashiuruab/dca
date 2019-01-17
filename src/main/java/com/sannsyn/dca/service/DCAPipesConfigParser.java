package com.sannsyn.dca.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.observables.BlockingObservable;

import java.io.IOException;
import java.util.*;

import static com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipeInstanceType.SPECIFICATION;
import static java.util.Optional.empty;

/**
 * This class is responsible for parsing the chainelement definitions config and schema.
 * <p>
 * Created by jobaer on 4/28/16.
 */
public class DCAPipesConfigParser extends DCAAbstractRestService<String> {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipesConfigParser.class);
    private static final String COMMON_SCHEMA_KEY_NAME = "CHAINELEMENTS_COMMON_SCHEMA";
    private static final String COMMON_DEFAULTS_KEY_NAME = "CHAINELEMENTS_COMMON_DEFAULTS";
    private static final String CONFIG_URL_TEMPLATE = "<endpoint>/recapi/internal/getconfig/<service>/<filename>";
    private Config defaultSchema = ConfigFactory.empty();
    private final Config chainElementDefinitions;
    private Config defaultValues = ConfigFactory.empty();

    public DCAPipesConfigParser() {
        Observable<String> chainElementsDefString = getChainElementsDefString();
        //todo - temporary workaround via blocking call. Need to be fixed later.
        BlockingObservable<String> from = BlockingObservable.from(chainElementsDefString);
        String first = from.first();
        chainElementDefinitions = ConfigFactory.parseString(first);

        Observable<String> chainElementsDefaultsString = getChainElementsDefaultsString();
        String defaultsString = BlockingObservable.from(chainElementsDefaultsString).first();
        Config chainElementDefaults = ConfigFactory.parseString(defaultsString);

        defaultSchema = chainElementDefaults.getConfig(COMMON_SCHEMA_KEY_NAME);
        defaultValues = chainElementDefaults.getConfig(COMMON_DEFAULTS_KEY_NAME);
    }

    /**
     * This will extract the description from the schema object for a given class.
     *
     * @param className - fully qualified class name
     * @return the description present in the schema document
     */
    String extractDescription(String className) {
        if (StringUtils.isBlank(className)) return "";

        String sourceClass = className.replaceAll("\\.", "/");
        if (chainElementDefinitions.hasPath(sourceClass)) {
            Config sourceClassConfig = chainElementDefinitions.getConfig(sourceClass);
            if (sourceClassConfig.hasPath("schema")) {
                Config schema = sourceClassConfig.getConfig("schema");
                if (schema.hasPath("description")) {
                    return schema.getString("description");
                }
            }
        }
        return "";
    }

    public String getTypeFor(String className) {
        if (StringUtils.isBlank(className)) return "";

        String sourceClass = className.replaceAll("\\.", "/");
        if (chainElementDefinitions.hasPath(sourceClass)) {
            Config sourceClassConfig = chainElementDefinitions.getConfig(sourceClass);
            if (sourceClassConfig.hasPath("type")) {
                return sourceClassConfig.getString("type");
            }
        }
        return "";
    }

    private Optional<Config> getSchemaJson(String className) {
        if (StringUtils.isBlank(className)) return empty();

        String sourceClass = className.replaceAll("\\.", "/");
        if (chainElementDefinitions.hasPath(sourceClass)) {
            Config sourceClassConfig = chainElementDefinitions.getConfig(sourceClass);
            if (sourceClassConfig.hasPath("schema")) {
                Config schema1 = sourceClassConfig.getConfig("schema");
                Config schema = schema1.withFallback(defaultSchema);
                return Optional.of(schema);
            }
        }

        return empty();
    }

    public Optional<Config> getDefaultConfig(String className) {
        String sourceClass = className.replaceAll("\\.", "/");
        if (chainElementDefinitions.hasPath(sourceClass)) {
            Config sourceClassConfig = chainElementDefinitions.getConfig(sourceClass);

            Config schema1 = sourceClassConfig.getConfig("defaults");
            Config schema = schema1.withFallback(defaultValues);

            return Optional.of(schema);
        } else {
            return Optional.empty();
        }
    }

    public List<PipeProperty> getAllPropertiesFor(String className) {
        List<PipeProperty> allPropertiesFor = getAllProperties(className);
        Optional<Config> defaultConfig = getDefaultConfig(className);
        if (!defaultConfig.isPresent()) {
            return allPropertiesFor; //return the properties as it is, since we didn't get any default values
        }

        Config defaultValues = defaultConfig.get();
        for (PipeProperty property : allPropertiesFor) {
            populatePropertyWithDefault(defaultValues, property);
        }
        return allPropertiesFor;
    }

    private void populatePropertyWithDefault(Config defaultValues, PipeProperty property) {
        String name = property.getPropertyName();
        if (defaultValues.hasPath(name)) {
            Object defaultValuesAnyRef = defaultValues.getAnyRef(name);
            property.setDefaultValueConfig(defaultValuesAnyRef);
        }
    }

    private List<PipeProperty> getAllProperties(String className) {
        List<PipeProperty> result = new ArrayList<>();

        Optional<Config> schemaJson = getSchemaJson(className);
        if (schemaJson.isPresent()) {
            Config schema = schemaJson.get();
            List<PipeProperty> properties = parseProperties(schema);
            result.addAll(properties);
        }

        return result;
    }

    public static List<PipeProperty> parseOneOfProperties(Config schema) {
        DCAPipesConfigParser configParser = new DCAPipesConfigParser();
        List<PipeProperty> properties = new ArrayList<>();

        if(schema.hasPath("oneOf")) {
            List<? extends Config> childSchemas = schema.getConfigList("oneOf");

            for (Config childSchema : childSchemas) {
                PipeProperty property = new PipeProperty();
                String name = "";
                if(childSchema.hasPath("type")) {
                    property.setSchema(childSchema);
                } else if (childSchema.hasPath("\"$ref\"")) {
                    Optional<Config> config = configParser.resolveRef(childSchema);
                    config.ifPresent(property::setSchema);
                }

                if(property.getSchema() != null && property.getSchema().hasPath("type")) {
                    name = property.getSchema().getString("type");
                }
                property.setPropertyName(name);

                properties.add(property);
            }
        }

        return properties;
    }

    public static List<PipeProperty> parseProperties(Config schema) {
        DCAPipesConfigParser configParser = new DCAPipesConfigParser();
        List<PipeProperty> recommenderComponenetProperties = new ArrayList<>();

        if (schema.hasPath("properties")) {
            Config properties = schema.getConfig("properties");
            Set<String> childKeys = getChildKeys(properties);
            for (String childKey : childKeys) {
                PipeProperty prop = new PipeProperty();
                prop.setPropertyName(childKey);

                Config config = properties.getConfig(childKey);
                if (config.hasPath("description")) {
                    String desc = config.getString("description");
                    prop.setDescription(desc);
                }
                if (config.hasPath("\"$ref\"")) {
                    Optional<Config> configOptional = configParser.resolveRef(config);
                    configOptional.ifPresent(prop::setSchema);
                } else {
                    prop.setSchema(config);
                }
                recommenderComponenetProperties.add(prop);
            }
        }

        return recommenderComponenetProperties;
    }

    private Optional<Config> resolveRef(Config config) {
        String ref = config.getString("\"$ref\"");
        String defaultPropertyName = ref.substring(14, ref.length());
        Config definitions = defaultSchema.getConfig("definitions");
        if (definitions.hasPath(defaultPropertyName)) {
            Config defaultDef = definitions.getConfig(defaultPropertyName);
            return Optional.of(defaultDef);
        } else return Optional.empty();
    }

    private static Set<String> getChildKeys(Config config) {
        Set<String> result = new HashSet<>();

        for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
            String key = entry.getKey();
            if (key.contains(".")) {
                int i = key.indexOf(".");
                result.add(key.substring(0, i));
            }
        }

        return result;
    }

    Observable<List<DCAPipe>> getAllClassDefinitions() {
        return getAllClassNames().map(allClassNames -> {
            List<DCAPipe> allDefs = new ArrayList<>();
            for (String className : allClassNames) {
                String name = getUnqualifiedName(className);
                String type = getTypeFor(className);
                String description = extractDescription(className);
                DCAPipe recommenderComponent = new DCAPipe(name, type, description);
                recommenderComponent.setClazz(className);
                recommenderComponent.setComponentType(SPECIFICATION);
                allDefs.add(recommenderComponent);
            }

            return allDefs;
        });
    }

    private String getUnqualifiedName(String className) {
        if (className.contains(".")) {
            int i = className.lastIndexOf('.');
            return className.substring(i + 1, className.length());
        } else {
            return className;
        }
    }

    private Observable<String> getChainElementsDefString() {
        return getUrl("chainelement_definitions.conf")
            .flatMap(url -> doRxGet(url, response -> response.readEntity(String.class)));
    }

    private Observable<String> getChainElementsDefaultsString() {
        return getUrl("chainelements_definition_defaults.conf")
            .flatMap(url -> doRxGet(url, response -> response.readEntity(String.class)));
    }

    private Observable<String> getUrl(String filename) {
        return DCAUtils.getTargetService().flatMap(dcaSelectedService -> {
            String url = buildUrl(dcaSelectedService.getServiceEndpoint().getEndpointAddress(),
                dcaSelectedService.getServiceIdentifier(), filename);
            return Observable.just(url);
        });
    }

    private String buildUrl(String endpointAddress, String serviceIdentifier, String fileName) {
        return CONFIG_URL_TEMPLATE.replace("<endpoint>", endpointAddress).replace("<service>", serviceIdentifier)
            .replace("<filename>", fileName);
    }

    private Observable<Set<String>> getAllClassNames() {
        return getChainElementsDefString().map(this::parseClassNames);
    }

    private Set<String> parseClassNames(String resp) {
        Config chainElementDefinitions = ConfigFactory.parseString(resp);
        Set<String> allNames = new HashSet<>();
        Set<Map.Entry<String, ConfigValue>> entries = chainElementDefinitions.entrySet();
        for (Map.Entry<String, ConfigValue> entry : entries) {
            String key = entry.getKey();
            if (key.contains(".")) {
                int i = key.indexOf('.');
                String substring = key.substring(0, i);
                if (substring.startsWith("\"com")) {
                    String name = substring.substring(1, substring.length() - 1);
                    String className = name.replaceAll("/", "\\.");
                    allNames.add(className);
                }
            }
        }

        return allNames;
    }

    /**
     * This function will validate a json object against it's schema
     *
     * @param object    the String representation of the json object which will be validated
     * @param className the class name for which the json object will be validated
     * @return a pair containing the boolean - for status, and message - for failed status
     */
    Pair<Boolean, String> validateAgainstSchema(String object, String className) {
        Optional<Config> schemaJson = getSchemaJson(className);
        if (!schemaJson.isPresent()) {
            return new ImmutablePair<>(false, "Could not find the class name for validate");
        }

        Config config = schemaJson.get();
        String schemaJsonString = config.root().render(ConfigRenderOptions.concise());

        try {
            JsonNode schemaObject = JsonLoader.fromString(schemaJsonString);
            JsonNode jsonObject = JsonLoader.fromString(object);

            final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            final JsonSchema schema = factory.getJsonSchema(schemaObject);

            ProcessingReport validate = schema.validate(jsonObject);

            StringBuilder msgBuilder = new StringBuilder();
            validate.forEach(msgBuilder::append);
            return new ImmutablePair<>(validate.isSuccess(), msgBuilder.toString());
        } catch (IOException | ProcessingException ioe) {
            logger.error("Exception while validation", ioe);
            return new ImmutablePair<>(false, "Error occurred. Cannot proceed with validation. " + ioe.getMessage());
        }
    }
}
