package com.tenco.bank.repository.model;

import java.sql.Timestamp;

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
	private String originFileName;
	private String uploadFileName;
	private String mFile;
	private Timestamp createdAt;
	
	public String setUpUserImage() {
		
		return uploadFileName ==null ? "https://picsum.photos/id/1/350" :"/images/uploads/" + uploadFileName;
	}
}
