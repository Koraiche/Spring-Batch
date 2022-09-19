package org.id.bankspringbatch.domain;

import lombok.*;

import javax.persistence.*;
import java.util.Date;


@Entity @AllArgsConstructor @NoArgsConstructor @Getter @Setter @ToString
public class BankTransaction {
    @Id //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long accountID;
    private Date transactionDate;
    @Transient
    private String strTransactionDate;
    private String transactionType;
    private double amount;
}
