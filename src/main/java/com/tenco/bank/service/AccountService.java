package com.tenco.bank.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.dto.TransferDTO;
import com.tenco.bank.dto.WithdrawalDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.interfaces.HistoryRepository;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.History;
import com.tenco.bank.utils.Define;

@Service
public class AccountService {

	private final AccountRepository accountRepository;
	private final HistoryRepository historyRepository;

	@Autowired // 생략 가능 - DI 처리
	public AccountService(AccountRepository accountRepository, HistoryRepository historyRepository) {
		this.accountRepository = accountRepository;
		this.historyRepository = historyRepository;
	}

	/**
	 * 계좌 생성 기능
	 * 
	 * @param dto
	 * @param id
	 */
	// 트랜 잭션 처리
	@Transactional
	public void createAccount(SaveDTO dto, Integer principalId) {
		int result = 0;

		try {
			result = accountRepository.insert(dto.toAccount(principalId));
		} catch (DataAccessException e) {
			throw new DataDeliveryException("잘못된 요청입니다", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException("알 수 없는 오류 입니다", HttpStatus.SERVICE_UNAVAILABLE);
		}

		if (result == 0) {
			throw new DataDeliveryException("정상 처리 되지 않았습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<Account> readAccountListByUserId(Integer userId) {
		List<Account> accountListEntity = null;
		try {
			accountListEntity = accountRepository.findByUserId(userId);

		} catch (DataAccessException e) {
			throw new DataDeliveryException("잘못된 처리 입니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
		}
		return accountListEntity;
	}

	// 한번에 모든 기능을 생각 힘듬
	// 1. 계좌 존재 여부를 확인 -- select
	// 2. 본인 계좌 여부를 확인 -- 객체 상태값에서 비교
	// 3. 계좌 비번 확인 -- 객체 상태값에서 일치 여부 확인
	// 4. 잔액 여부 확인 -- 객체 상태값에서 확인
	// 5. 출금 처리 -- update
	// 6. 거래 내역 등록 -- insert(history)
	// 7. 트랜잭션 처리
	@Transactional
	public void updateAccountWithdraw(WithdrawalDTO dto, Integer principalId) {
		// 1.
		Account accoutEntity = accountRepository.findByNumber(dto.getWAccountNumber());
		if (accoutEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}

		// 2
		accoutEntity.checkOwner(principalId);
		// 3
		accoutEntity.CheckPassword(dto.getWAccountPassword());
		// 4
		accoutEntity.CheckBalance(dto.getAmount());
		// 5
		// accoutEntity 객체의 잔액을 변경하고 업데이트 처리해야 한다.
		accoutEntity.withdraw(dto.getAmount());
		// update 처리
		accountRepository.updateById(accoutEntity);
		// 6 - 거래 내역 등록
		History history = new History();
		history.setAmount(dto.getAmount());
		history.setWBalance(accoutEntity.getBalance());
		history.setDBalance(null);
		history.setWAccountId(accoutEntity.getId());
		history.setDAccountId(null);

		int rowResultCount = historyRepository.insert(history);
		if (rowResultCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public void updateAccountDeposit(DepositDTO dto, Integer principalId) {
		// 1.
		Account accountEntity = accountRepository.findByNumber(dto.getDAccountNumber());
		if (accountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}

		// 2
		accountEntity.checkOwner(principalId);
		accountEntity.CheckBalance(dto.getAmount());
		// update 처리
		// accoutEntity 객체의 잔액을 변경하고 업데이트 처리해야 한다.
		// 3
		accountEntity.deposit(dto.getAmount());
		accountRepository.updateById(accountEntity);
		// 4
        History history = History.builder()
                .amount(dto.getAmount())
                .dAccountId(accountEntity.getId())
                .dBalance(accountEntity.getBalance())
                .wAccountId(null)
                .wBalance(null)
                .build();

		int rowResultCount = historyRepository.insert(history);
		if (rowResultCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	// 이체 기능 만들기
	// 1. 출금 계좌 존재 여부 확인 -- select
	// 2. 입금 계좌 존재 여부 확인 -- select (객체 리턴 받은 상태)
	// 3. 출금 계좌 본인 소유 확인 -- 객체 상태값과 세션 아이디 비교
	// 4. 출금 계좌 비밀번호 확인  -- 객체 상태값과, dto 비밀번호 비교
	// 5. 출금 계좌 잔액 여부 확인 -- 객체 상태값 확인, dto 와 비교
	// 6. 입금 계좌 객체 상태값 변경 처리(거래금액 증가)
	// 7. 입금 계좌 -- update 처리
	// 8. 출금 계좌 객체 상태값 변경 처리( 잔액 - 거래금액)
	// 9. 출금 계좌 -- update 처리
	// 10. 거래 내역 등록 처리
	// 11. 트랜잭션
	
	public void updateAccountTransfer(TransferDTO dto,Integer principalId) {
		// 1.
		Account outAccoutEntity = accountRepository.findByNumber(dto.getWAccountNumber());
		if (outAccoutEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 2.
		Account inAccoutEntity = accountRepository.findByNumber(dto.getDAccountNumber());
		if (inAccoutEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 3.
		outAccoutEntity.checkOwner(principalId);
		
		// 4.
		outAccoutEntity.CheckPassword(dto.getPassword());
		// 5.
		outAccoutEntity.CheckBalance(dto.getAmount());
		
		// 6.
		inAccoutEntity.deposit(dto.getAmount());
		// 7.
		accountRepository.updateById(inAccoutEntity);
		// 8.
		outAccoutEntity.withdraw(dto.getAmount());
		// 9.
		accountRepository.updateById(outAccoutEntity);
		// 10.
		
        History history = History.builder()
                .amount(dto.getAmount())
                .dAccountId(inAccoutEntity.getId())
                .dBalance(inAccoutEntity.getBalance())
                .wAccountId(outAccoutEntity.getId())
                .wBalance(outAccoutEntity.getBalance())
                .build();

		int rowResultCount = historyRepository.insert(history);
		if (rowResultCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
