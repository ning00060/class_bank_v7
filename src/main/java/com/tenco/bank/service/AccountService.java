package com.tenco.bank.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.model.Account;

@Service
public class AccountService {
	

	@Autowired
	private final AccountRepository accountRepository;

	public AccountService(AccountRepository accountRepository) {
		this.accountRepository=accountRepository;
	}
	
	/**
	 * 계좌 생성 기능
	 * @param dto
	 * @param userId
	 */
	@Transactional 
	public void createAccount(SaveDTO dto, Integer principalId) {
		int result = 0;
		System.out.println(principalId+"userid");
		try {
			result = accountRepository.insert(dto.toAccount(principalId));

		} catch (DataAccessException e) {
			throw new DataDeliveryException("잘못된 요청입니다", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException("알수없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
		}

		if (result == 0) {
			throw new DataDeliveryException("정상 처리 되지 않았습니당", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public List<Account> readAccountListByUserId(Integer userId) {
		List<Account> accountList=null ;
		try {
			accountList=accountRepository.findByUserId(userId);
		} catch (DataDeliveryException e) {
			throw new DataDeliveryException("잘못된 처리 입니다", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException("알수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
		}
		return accountList;
	}
}