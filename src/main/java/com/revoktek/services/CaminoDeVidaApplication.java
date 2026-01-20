package com.revoktek.services;

import com.revoktek.services.model.User;
import com.revoktek.services.model.enums.Authority;
import com.revoktek.services.service.CategoryService;
import com.revoktek.services.service.GroupService;
import com.revoktek.services.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@Log4j2
public class CaminoDeVidaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaminoDeVidaApplication.class, args);
	}

	@Bean
	public CommandLineRunner initService(
										 Environment environment, UserService userService, GroupService groupService,
                                        CategoryService categoryService,
										@Value("${spring.datasource.url}") String dataSourceUrl) {
		return args -> {
			log.info("==========INIT LOG==========");
			log.info("database connection: " + dataSourceUrl);
			log.info("environment active: " + Arrays.toString(environment.getActiveProfiles()));
			log.info("checking if exists default users");
			User defaultAdmin = new User();
			defaultAdmin.setUsername("admincaminodevida@gmail.com");

			if (userService.notExistsByUsername(defaultAdmin.getUsername())) {
				defaultAdmin.setPassword("administracion2026");
				defaultAdmin.setEnabled(true);
				List<Authority> permissions = new ArrayList<>();
				permissions.add(Authority.ADMIN);
				defaultAdmin.setAuthorities(permissions);
				userService.save(defaultAdmin);

			}
			log.info("🟢 -- Default Users checked or created successfully");
            categoryService.initDefaultCategories();
            groupService.initializeDefaultGroups();
		};
	}

}
