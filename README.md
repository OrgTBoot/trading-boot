### Prerequisites

You need to have a WB account with Paper Trading enabled.

### Setup:

1. Clone this repo.
2. Create `application-default.yaml`, it will allow to override props from `application.yaml`. Provide your account information (see in browser network calls). Ex:
```
features:
  paper-trading: true
account:
  order:
    size-percent: 50
    stale-ttl-sec: 60

providers:
  webull:
    trade-account:
      id: 10000000
      pin:
        key: t_token
        secret: 111111
    paper-account:
      id: 222222
    auth:
      key: access_token
      secret: dc_us_tech1.bla-bla-bla-bla
```

### Run

1. Run `Application.java` - it's your application entry point.
2. Navigate to 'http://localhost:8087'
3. You should see GQL playground where you can start running queries and mutations

### GQL query and mutation examples

:warning: Beware this API endpoint can change over the time and should be treated only as a reference.
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

query fetchAllTradingRecords($daysRange: Int!) {
  fetchAllTradingRecords(daysRange: $daysRange)
}

query fetchTradingRecords($symbol: String!, $daysRange: Int!) {
  fetchTradingRecords(symbol: $symbol, daysRange: $daysRange)
}

mutation triggerBACKTrackingForSymbols($strategy: TradingStrategies!) {
  triggerSymbolsBackTracking(symbols: ["SQQQ", "TQQQ"], strategy: $strategy)
}

mutation triggerBACKTracking($symbol: String!, $strategy: TradingStrategies!) {
  triggerBackTracking(symbol: $symbol, strategy: $strategy)
}

mutation triggerLiveTrading($symbol: String!, $strategy: TradingStrategies!) {
  triggerLiveTrading(symbol: $symbol, strategy: $strategy)
}

mutation triggerLiveTradingStop($strategyKey: String!) {
  triggerLiveTradingStop(strategyKey: $strategyKey)
}

mutation triggerLogLevelChange {
  triggerLogLevelChange(level: DEBUG, package: RULES)
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
  "strategy": "DEMA_V7",
  "strategyKey":"",
  "daysRange": 0
}
```
