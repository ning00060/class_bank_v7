package com.tenco.bank.service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.UserRepository;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.utils.Define;

import lombok.RequiredArgsConstructor;

@Service // IoC 대상(싱글톤으로 관리)
@RequiredArgsConstructor
public class UserService {

	// DI - 의존 주입
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	// 회원 가입 처리

	/**
	 * 회원 등록 서비스 기능 트랜잭션 처리
	 * 
	 * @param dto
	 */
	@Transactional // 트랜잭션 처리는 반드시 습관화
	public void createUser(SignUpDTO dto) {
		int result = 0;

		System.out.println(dto.getMFile().getOriginalFilename());

		if (!dto.getMFile().isEmpty()) {
			String[] fileNames = uploadFile(dto.getMFile());
			dto.setOrginFileName(fileNames[0]);
			dto.setUploadFileName(fileNames[1]);
		}

		try {
			String hashPwd = passwordEncoder.encode(dto.getPassword());
			dto.setPassword(hashPwd);
			result = userRepository.insert(dto.toUser());

		} catch (DataAccessException e) {
			throw new DataDeliveryException("중복된 이름을 사용할 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}

		if (result != 1) {
			throw new DataDeliveryException(Define.FAIL_TO_CREATE_USER, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private String[] uploadFile(MultipartFile mFile) {
		if (mFile.getSize() > Define.MAX_FILE_SIZE) {
			throw new DataDeliveryException("파일의 크기가 20MB을 초과할수없습니다", HttpStatus.BAD_REQUEST);
		}
		String saveDirectory = Define.UPLOAD_FILE_DERECTORY;
		File directory = new File(saveDirectory);
		if (!directory.exists()) {
			directory.mkdirs();
		}

		String uploadFileName = UUID.randomUUID() + "_" + mFile.getOriginalFilename();
		String uploadPath = saveDirectory + File.separator + uploadFileName;
		System.out.println("-------------");
		System.out.println(uploadPath);
		System.out.println("------------");
		File destination = new File(uploadPath);

		try {
			mFile.transferTo(destination);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
			throw new DataDeliveryException("파일 업로드 실패", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new String[] { mFile.getOriginalFilename(), uploadFileName };
	}

	public User readUser(SignInDTO dto) {
		// 유효성 검사는 controller 에서 먼저 하자
		User userEntity = null;
		try {
			userEntity = userRepository.findByUsername(dto.getUsername());

		} catch (DataAccessException e) {
			throw new DataDeliveryException("잘못된 처리 입니다", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException("알수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
		}

		if (userEntity == null) {
			throw new DataDeliveryException("존재 하지않는 아이디 입니다", HttpStatus.BAD_REQUEST);
		}
		boolean isPwdMatched = passwordEncoder.matches(dto.getPassword(), userEntity.getPassword());
		if (!isPwdMatched) {
			throw new DataDeliveryException("비밀번호 오류", HttpStatus.BAD_REQUEST);
		}

		return userEntity;
	}
}
