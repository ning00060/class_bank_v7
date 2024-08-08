package com.tenco.bank.dto;

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
public class TransferDTO {

	private Long amount;// 거래 금액
	private String wAccountNumber;// 출금계좌 번호
	private String dAccountNumber;//입금계좌 번호
	private String password;// 출금계좌비밀번호
}
