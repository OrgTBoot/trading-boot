package com.mg.trading.boot.integrations.webull.data.trading;

import com.mg.trading.boot.integrations.webull.data.common.WOrderStatus;
import lombok.*;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WTOrdersQuery {
    private String secAccountId;
    private Integer pageSize;
    private Long lastCreateTime0;
    private String startTimeStr;
    private String endTimeStr;
    private String action;
    private WTOrderDateType dateType;
    private WOrderStatus status;
}
