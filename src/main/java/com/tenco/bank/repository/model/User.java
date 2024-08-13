package com.tenco.bank.repository.model;

import java.sql.Timestamp;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class User {
	private Integer id;
	private String username;
	private String password;
	private String fullname;
	private String orginFileName;
	private String uploadFileName;
	private Timestamp createdAt;
	
}
