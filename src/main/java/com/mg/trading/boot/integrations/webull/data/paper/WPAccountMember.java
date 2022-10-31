package com.mg.trading.boot.integrations.webull.data.paper;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WPAccountMember {
    private String key;
    private BigDecimal value;
}
