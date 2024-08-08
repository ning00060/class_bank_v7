package com.tenco.bank.repository.interfaces;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tenco.bank.repository.model.Account;

//인터페이스와 account.xml 파일을 매칭 시킨다
@Mapper
public interface AccountRepository {
	
	public int insert(Account account);
	public int updateById(Account account);
	public int deleteById(Account account);
	
	// interface 파라미터 명과 xml 에 사용할 변수명을 다르게 사용해야 된다먀 @param 애노테이션을
	// 사용할수있다 . 그리고 2개 이상에 파라미터를 사용할 경우 반드시 사용하자
	// 고민 - 계좌 조회
	// --> 한사람에 유저는 여러개의 계좌 번호를 가질 수 있다.
	public List<Account> findByUserId(@Param("userId") Integer userId);
	
	public Account findByNumber(@Param("number") String number);
	
	// --> account id 값으로 계좌 정보 조회
}
