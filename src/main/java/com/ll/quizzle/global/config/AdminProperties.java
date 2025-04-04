package com.ll.quizzle.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "admin")
public class AdminProperties {
	private String adminEmail;
	private String adminPasswordHash;

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public String getAdminPasswordHash() {
		return adminPasswordHash;
	}

	public void setAdminPasswordHash(String adminPasswordHash) {
		this.adminPasswordHash = adminPasswordHash;
	}
}
