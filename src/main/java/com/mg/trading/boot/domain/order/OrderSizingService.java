package com.mg.trading.boot.domain.order;


import com.mg.trading.boot.domain.models.OrderAction;
import com.mg.trading.boot.domain.models.TickerQuote;
import com.mg.trading.boot.integrations.BrokerProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * For more details please see <a href="https://www.investopedia.com/terms/p/positionsizing.asp">Position Sizing</a>
 * <p>
 * Todo: Introduce an additional parameter that can allow to trade only with a certain account amount.
 *       for example if account is 50k and we want to limit trades to 25k.
 */
@Log4j2
@Service
public class OrderSizingService {
    private final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private final BigDecimal orderSizePercent;
    private final BigDecimal orderPricePercentAdd;
    private final BrokerProvider broker;

    public OrderSizingService(@Value("${account.order.size-percent}") final BigDecimal orderSizePercent,
                              @Value("${account.order.price-percent-add}") final BigDecimal orderPricePercentAdd,
                              final BrokerProvider broker) {
        this.orderSizePercent = orderSizePercent;
        this.orderPricePercentAdd = orderPricePercentAdd;
        this.broker = broker;
    }

    /**
     * The investor knows that they can risk $500 per trade and is risking $20 per share.
     * To work out the correct position size from this information, the investor simply needs
     * to divide the account risk, which is $500, by the trade risk, which is $20.
     * This means 25 shares can be bought ($500 / $20).
     *
     * @param sharePrice - cost of the share we intend to purchase
     * @return - number of shares we can purchase.
     */
    public BigDecimal getOrderSizeInShares(BigDecimal sharePrice) {
        BigDecimal accountRiskAmount = getAccountAmountToRisk();
        int shares = accountRiskAmount.divide(sharePrice, RoundingMode.CEILING).intValue(); //rounding the value

        return BigDecimal.valueOf(shares);
    }

    /**
     * Get Account Risk amount
     * For example, if we have $25,000 account and maximum account risk of 2%, we cannot risk more than $500
     * per trade (2% x $25,000). Even if we lose 10 consecutive trades in a row,
     * we have only lost 20% of their investment capital.
     *
     * @return amount we are allowed to risk.
     */
    private BigDecimal getAccountAmountToRisk() {
        BigDecimal buyingPower = this.broker.account().getAccount().getBuyingPower();
        return orderSizePercent.multiply(buyingPower).divide(HUNDRED, RoundingMode.CEILING);
    }

    /**
     * For stable stocks with high volume, market orders often execute at a prices that are close to the trader's
     * expected order. However, volatile stocks with low volume experience more rapid price swings, and there's a
     * possibility you could end up paying much more than you expected when you buy, or taking in far less than
     * you anticipated when you sell.
     * <p>
     * In other words price calibration is key when placing limit orders.
     */
    public BigDecimal getCalculateMarketPrice(String symbol, OrderAction action) {
        TickerQuote latestQuote = broker.ticker().getLatestTickerQuote(symbol);
        BigDecimal price = latestQuote.getClosePrice();

        BigDecimal amount = price.multiply(orderPricePercentAdd).divide(HUNDRED, RoundingMode.HALF_EVEN);

        amount = OrderAction.BUY.equals(action) ? amount : amount.negate();
        return price.add(amount);
    }
}
