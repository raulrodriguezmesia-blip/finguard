package com.example.finguard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Carga secretos desde AWS Secrets Manager solo en el perfil "prod".
 * Implementa ApplicationRunner para ejecutarse después del arranque de la aplicación.
 */
@Profile("prod")
@Configuration
public class SecretsManagerConfig implements ApplicationRunner {

    private final Environment environment;

    public SecretsManagerConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        String secretName = environment.getProperty("FINNGUARD_SECRET_NAME", "finguard/prod/secrets");
        String region = environment.getProperty("AWS_REGION", "us-east-1");

        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.of(region))
                .build()) {

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse response = client.getSecretValue(request);
            String secretString = response.secretString();

            Map<String, Object> secrets = new HashMap<>();
            // Se espera que el secreto sea un JSON con claves como:
            // { "DATABASE_URL": "...", "DATABASE_USERNAME": "...", "DATABASE_PASSWORD": "..." }
            // Para simplificar, parseamos como pares clave=valor separados por coma
            for (String pair : secretString.split(",")) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    secrets.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }

            MapPropertySource propertySource = new MapPropertySource("aws-secrets-manager", secrets);
            ((org.springframework.core.env.ConfigurableEnvironment) environment).getPropertySources()
                    .addFirst(propertySource);

        } catch (Exception e) {
            // En caso de error, no bloquear el inicio; usar valores por defecto
            System.err.println("No se pudieron cargar secretos desde Secrets Manager: " + e.getMessage());
        }
    }
}
