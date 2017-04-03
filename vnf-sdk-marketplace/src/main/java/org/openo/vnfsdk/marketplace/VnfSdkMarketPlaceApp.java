/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
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

package org.openo.vnfsdk.marketplace;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.openo.vnfsdk.marketplace.msb.Config;
import org.openo.vnfsdk.marketplace.msb.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;

public class VnfSdkMarketPlaceApp extends Application<VnfSdkMarketPlaceAppConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VnfSdkMarketPlaceApp.class);

    public static void main(String[] args) throws Exception {
        new VnfSdkMarketPlaceApp().run(args);
    }

    @Override
    public String getName() {
        return "OPENO-VNFSDK-MarketPlace";
    }

    @Override
    public void initialize(Bootstrap<VnfSdkMarketPlaceAppConfig> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/api-doc", "/api-doc", "index.html", "api-doc"));

    }

    private void initService() {
        Thread registerExtsysService = new Thread(new ServiceRegistration());
        registerExtsysService.setName("Register vnfsdk-MarketPlace service to Microservice Bus");
        registerExtsysService.start();
    }

    @Override
    public void run(VnfSdkMarketPlaceAppConfig configuration, Environment environment) {
        LOGGER.info("Start to initialize vnfsdk MarketPlace.");
        environment.jersey().packages("org.openo.vnfsdk.marketplace.resource");
        environment.jersey().register(MultiPartFeature.class);
        initSwaggerConfig(environment, configuration);
        Config.setConfigration(configuration);
        initService();
        LOGGER.info("Initialize vnfsdk function test finished.");
    }

    private void initSwaggerConfig(Environment environment, VnfSdkMarketPlaceAppConfig configuration) {
        environment.jersey().register(new ApiListingResource());
        environment.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

        BeanConfig config = new BeanConfig();
        config.setTitle("Open-o VnfSdk MarketPlace Service rest API");
        config.setVersion("1.0.0");
        config.setResourcePackage("org.openo.vnfsdk.marketplace.resource");

        SimpleServerFactory simpleServerFactory = (SimpleServerFactory)configuration.getServerFactory();
        String basePath = simpleServerFactory.getApplicationContextPath();
        String rootPath = simpleServerFactory.getJerseyRootPath();
        rootPath = rootPath.substring(0, rootPath.indexOf("/*"));
        basePath =
                ("/").equals(rootPath) ? rootPath : (new StringBuilder()).append(basePath).append(rootPath).toString();
        config.setBasePath(basePath);
        config.setScan(true);
    }

}
