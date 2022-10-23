### Prerequisites

You need to have a WB account with Paper Trading enabled.

### Setup:

1. Clone this repo.
2. Edit `application-default.yaml`, provide your account information (see in browser network calls).

### Run

1. Run `Application.java` - it's your application entry point.
2. Navigate to 'http://localhost:8087'
3. You should see GQL playground where you can start running queries and mutations

### GQL query and mutation examples

:warning: Beaware this API endpoint can change over the time and should be treated only as a reference.
For more details on the API endpoint check GQL API documentation that is available at `http://localhost:8087` `Docs`
tab.

```
query fetchScreenedTickers {
  fetchScreenedTickers {
    ...TickerFragment
  }
}

query fetchRunningStrategyKeys {
  fetchRunningStrategyKeys
}

query fetchTradingRecords($symbol: String!, $daysRange: Int!) {
  fetchTradingRecords(symbol: $symbol, daysRange: $daysRange)
}

mutation triggerBackTracking($symbol: String!, $strategy: TradingStrategies!) {
  triggerBackTracking(symbol: $symbol, strategy: $strategy)
}

mutation triggerLiveTrading($symbol: String!, $strategy: TradingStrategies!) {
  triggerLiveTrading(symbol: $symbol, strategy: $strategy)
}

mutation triggerLiveTradingStop($strategyKey: String!) {
  triggerLiveTradingStop(strategyKey: $strategyKey)
}

fragment TickerFragment on Ticker {
  symbol
  change
  company
  country
  externalId
  industry
  marketCap
  peRatio
  price
  volume
}

```

Variables for the above queries - simplifies query management.

```
{
  "symbol": "AMD",
  "strategy": "DEMA",
  "strategyKey":"CYXT_DEMA_CYXT",
  "daysRange": 30
}
```
