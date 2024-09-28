package com.myBackup.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.myBackup.about_to_delete.ServerRegistrationFilter;
import com.myBackup.client.services.ServerRegistrationService;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ServerRegistrationFilter> registrationFilter(ServerRegistrationService serverRegistration) {
        FilterRegistrationBean<ServerRegistrationFilter> registrationBean = 
                new FilterRegistrationBean<>();
        
        // Pass the ServerRegistration bean to the filter constructor
        registrationBean.setFilter(new ServerRegistrationFilter(serverRegistration));
        
        registrationBean.addUrlPatterns("/*"); // Apply to all URLs
        registrationBean.setOrder(1); // Set the order of the filter
        return registrationBean;
    }
    
}
