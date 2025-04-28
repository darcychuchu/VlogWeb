package org.vlog.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * 视频网站Web前端应用程序的主类
 *
 * 这是整个应用程序的入口点。@SpringBootApplication注解表示这是一个Spring Boot应用程序，
 * 它结合了@Configuration、@EnableAutoConfiguration和@ComponentScan注解的功能。
 *
 * 该应用采用前后端分离架构，但保留服务器端渲染的SEO优势：
 * 1. 不直接连接数据库，通过API获取数据
 * 2. 使用Thymeleaf模板引擎渲染页面
 * 3. 将不常变动的数据直接嵌入HTML
 * 4. 只保留经常变动的数据与API交互
 */
@SpringBootApplication
class VlogWebApplication

/**
 * 应用程序的主入口函数
 *
 * 使用Spring Boot的runApplication函数启动应用程序。
 * 星号(*)操作符用于将数组展开为可变参数。
 *
 * @param args 命令行参数数组
 */
fun main(args: Array<String>) {
    runApplication<VlogWebApplication>(*args)
}
