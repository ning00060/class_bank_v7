package com.tenco.bank.dto;

import org.springframework.web.multipart.MultipartFile;

import com.tenco.bank.repository.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class SignUpDTO {

	private String username;
	private String password;
	private String fullname;
	private MultipartFile mFile;
	private String orginFileName;
	private String uploadFileName;

	
	// 2단계 로직 - User Object 반환
	public User toUser() {
		return User.builder().username(this.username)
							 .password(this.password)
							 .fullname(this.fullname)
							 .orginFileName(orginFileName)
							 .uploadFileName(uploadFileName)
							 .build();
	}
	// todo -추후 사진 업로드 기능 추가 예정
}
