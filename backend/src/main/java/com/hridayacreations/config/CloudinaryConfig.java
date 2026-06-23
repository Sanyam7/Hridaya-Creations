package com.hridayacreations.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a configured {@link Cloudinary} client built from {@code app.cloudinary.*} properties.
 * The client is always created so it can be injected; whether uploads actually hit Cloudinary is
 * governed by {@code app.cloudinary.enabled} in the image service.
 */
@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(AppProperties properties) {
        AppProperties.Cloudinary config = properties.getCloudinary();
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", config.getCloudName(),
                "api_key", config.getApiKey(),
                "api_secret", config.getApiSecret(),
                "secure", true
        ));
    }
}
