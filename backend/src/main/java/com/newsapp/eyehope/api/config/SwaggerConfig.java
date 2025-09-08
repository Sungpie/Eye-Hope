package com.newsapp.eyehope.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI newsAppOpenAPI() {
        Server httpsServer = new Server();
        httpsServer.setUrl("https://eyehope.site");
        httpsServer.setDescription("HTTPS Server");

        Server httpLocalServer = new Server();
        httpLocalServer.setUrl("http://localhost:8080");
        httpLocalServer.setDescription("Local HTTP Server");

        return new OpenAPI()
                .addServersItem(httpsServer)
                .addServersItem(httpLocalServer)
                .info(new Info()
                        .title("뉴스 요약 앱 API")
                        .description("뉴스앱 백엔드 API 문서")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("EyeHope")
                                .url("https://github.com/Sungpie/Eye-Hope"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
