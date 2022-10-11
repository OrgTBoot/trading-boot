### Prerequisites

You need to have a WB account with Paper Trading enabled.

### Setup:

1. Clone this repo.
2. Edit `application-default.yaml`, provide your account information.

### Run

1. Run `Application.java` - it's your application entry point.
2. Navigate to 'http://localhost:8087'
3. You should see GQL playground where you can start running queries and mutations

### GQL query and mutation examples

```
# This will execute screening and provide a list of stocks recommended for day trading.
query runScreening {
  runScreening {
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
}

# This will run a simultation of the trading strategy.
query runBackTracking {
  runBackTrackingStrategy(symbol: "IMVT")
}

# This will start the trading strategy for a specific ticker. 
# Note that currently it execute all the BUY / SELL orders in your paper trading account.
mutation startTrading {
  startTradingStrategy(symbol: "IMVT")
}

```
