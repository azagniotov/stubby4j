package io.github.azagniotov.stubby4j.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

public enum SnakeYaml {

    INSTANCE;

    private final Yaml snakeYaml;

    SnakeYaml() {
        snakeYaml = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new YamlParserResolver());
    }

    public Yaml getSnakeYaml() {
        return snakeYaml;
    }


    private static final class YamlParserResolver extends Resolver {
        YamlParserResolver() {
            super();
        }

        @Override
        protected void addImplicitResolvers() {
            // no implicit resolvers - resolve everything to String
        }
    }
}
