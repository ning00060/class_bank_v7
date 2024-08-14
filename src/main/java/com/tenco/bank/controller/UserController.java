package com.tenco.bank.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.tenco.bank.dto.KakaoDTO;
import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.UserService;
import com.tenco.bank.utils.Define;

import jakarta.servlet.http.HttpSession;

@Controller // Ioc에 대상(싱글톤 패턴을 관리됨)
@RequestMapping("/user") // 대문처리
public class UserController {

	private UserService userService;
	private final HttpSession session;

	@Autowired
	public UserController(UserService service, HttpSession session) {
		this.userService = service;
		this.session = session;
	}

	/**
	 * 주소설계 -> http://localhost:8080/user/sign-up
	 * 
	 * @return sign.jsp
	 */
	@GetMapping("/sign-up")
	public String signUpPage() {

		return "user/signUp";
	}

	/**
	 * 
	 * 회원 가입 로직 처리 요청 주소 설계: http://localhost:8080/user/sign-up
	 * 
	 * @param dto
	 * @return
	 */
	@PostMapping("/sign-up")
	public String signUpProc(SignUpDTO dto) {

		if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
		}
		if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		if (dto.getFullname() == null || dto.getFullname().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_FULLNAME, HttpStatus.BAD_REQUEST);
		}

		// 서비스 객체로 전달
		userService.createUser(dto);

		// TODO - 추후 수정
		return "redirect:/user/sign-in";
	}

	/**
	 * 로그인 화면 요청 주소설계 :http://localhost:8080/user/sign-in
	 * 
	 * @return
	 */
	@GetMapping("/sign-in")
	public String signInPage() {
		// 인증검사 x
		// 유효성 x

		return "user/signIn";
	}

	/**
	 * 로그인 요청 처리 주소설계: http://localhost:8080/user/sign-in
	 * 
	 * @return
	 */
	@PostMapping("/sign-in")
	public String signProc(SignInDTO dto) {
		// 1. 인증검사 x
		// 2. 유효성 검사
		if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
		}
		if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}

		// 서비스 호출
		User principal = userService.readUser(dto);

		// 세션 메모리에 등록 처리
		session.setAttribute(Define.PRINCIPAL, principal);

		// 새로운 페이지로 이동 처리
		// TODO - > 계좌 목록 페이지 이동처리 예정

		return "redirect:/account/list";
	}

	@GetMapping("/logout")
	public String logout() {
		session.invalidate();// 로그아웃
		return "redirect:/user/sign-in";
	}

	@GetMapping("/kakao")
	@ResponseBody
	public ResponseEntity<?> kakaoPage(@RequestParam(name = "code") String code) {
	    URI uri = UriComponentsBuilder.fromUriString("https://kauth.kakao.com")
	            .path("/oauth/token")
	            .build()
	            .toUri();

	    RestTemplate restTemplate = new RestTemplate();

	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/x-www-form-urlencoded");

	    KakaoDTO kakaoDTO= KakaoDTO.builder()
	    		.ClientId("74ae415425bf1b53dc8f8dcf38efc2d4")
	    		.RedirectUri("http://localhost:8080/user/kakao")
	    		.AuthorizeCode(code)
	    		.build();
	    
	    
	    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	    params.add("grant_type", "authorization_code");
	    params.add("client_id", kakaoDTO.getClientId());
	    params.add("redirect_uri", kakaoDTO.getRedirectUri());
	    params.add("code", kakaoDTO.getAuthorizeCode());

	    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

	    ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);

	    System.out.println("Response Header: " + response.getHeaders());
	    System.out.println("Response Body: " + response.getBody());

	    return ResponseEntity.status(HttpStatus.OK).body(response.getBody());
	}
}
