### Table of Contents

* [Technologies](#technologies)
* [App structure](#app-structure)
* [System Architecture](#system-architecture)
* [BlockClock Api](#blockclock-api)
* [License](#license)

## Technologies

* Kotlin
* Jetpack Compose
* Koin for dependency injection
* Retrofit/OkHttp/Json for networking
* Control (MVI) as architectural pattern
* Room for local storage

## App Structure

The app has 3 main screens Watchlists, Feeds and Settings. 
On starting the app, the Overview screen is started, which will start fetching data.

## System Architecture

### Data Flow

All data flow is done via the core DataRepo.

* The TapToolsManager is used to fetch wallet positions, prices for tokens, stats and logos for NFTs.
* The BlockFrostManager helps resolve ada andles and checks validity of ada addresses.
* The LogoManager gets token information from tokens.cardano.org to retrieve the token logo. 
* The ClockManager communicates with the BlockClock.

### Local Storage

The room database stores all reusable data, using serveral Daos.

* Position info for tokens, NFTs and LP.
* Watchlist with config and lists of above positions.
* Feed entries for Tokens and NFTs, with a list of Alert entries.
* Alert entries with alert details.
* NFT Stats and Token price entries.
* FeedTheClock entries with an ordered list of items to feed the BlockClock.

### Automatic Data Fetching

Real-time price and alert updates are done using workers. As these workers can run in the background, 
they will keep running if the app is moved to the background. This ensures relevant price info is 
available when we reopen the app.

* FetchPricesWorker gets and stores prices for Tokens and NFTs, every 15 minutes.
* AlertWorker loops through all enabled alerts and checks if the threshold target price was reached,
* every 15 minutes.

### Feeding the Clock

Running the feed to the BlockClock is done using a FeedCycler.The FeedCycler can be stopped and 
started from the settings screen. If auto feed cycle is activated, it will start on app start. It 
pauses the BlockClocks normal feed. During FeedCycling we retrieve token prices more often than the 
workers 15 minutes.

* Every minute, get the next item to be fed to the clock.
* When done, prepare the next item, by updating price and trend info from the available db price info.
* Get and stores prices for Tokens very 5 minutes! 

## BlockClock Api
The BlockClock Api allows to send custom texts, over/under text, flash a white light, as well as set the LED to a specific colour. As settings text triggers a 1 minute timeout before the next text can be send, the FeedCycler was set to operate every one minute.

See [BLOCKCLOCK mini “Push” API](https://blockclockmini.com/api.html) for more info.

### Formatting:

A lot of text and price formatting is done to be able to use the available space of the BlockClock display.
Especially long NFT names and prices in the 0.00000001 range had to be managed to be displayable.
Thanks Hosky!

### Trend Light Modes: 
* Default Light Mode: Light up red/green, followed by a white flash
* Double Light Mode: Light up red/green 3 times, followed by a white flash
* Alert Light Mode: Light up blue, white flash, blue, white flash

## License

```
Copyright 2024 Tailored Media GmbH.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
