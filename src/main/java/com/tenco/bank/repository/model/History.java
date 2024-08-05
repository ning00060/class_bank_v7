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
public class History {
	private Integer id;
	private Long amount;
	private Long wBalance;
	private Long dBalance;
	private Integer wAccount;
	private Integer dAccount;
	private Timestamp craetedAt;
}
