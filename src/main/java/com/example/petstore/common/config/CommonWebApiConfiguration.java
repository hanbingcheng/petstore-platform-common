package com.example.petstore.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/** 共通WebApi設定クラス. */
@Configuration(proxyBeanMethods = false)
@ComponentScan("com.example.petstore.common")
public class CommonWebApiConfiguration {}
