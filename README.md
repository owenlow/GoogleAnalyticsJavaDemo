# GoogleAnalyticsJavaDemo

---

## Description

Updated example of the Google Analytics Reporting API java quickstart from [here](https://developers.google.com/analytics/devguides/reporting/core/v4/quickstart/service-java)

I found some parts of the code were out of date and nonfunctional (as of April 2021), so I've written this updated sample with some additional comments.

---

## Problem

Goal of this sample code is to retrieve "Item" events with action "Add to Order",
and return their labels (which consists of the name of the item).
On the client, this was logged as:

```js
ReactGA.event({
    category: 'Item',
    action: 'Add to Order',
    label: 'Pizza',
});
```

---

## Building & Testing Google Analytics Queries

I used the following tools to build queries, then adapted them to this code:

Learn about events & dimensions to pick from:
https://ga-dev-tools.appspot.com/dimensions-metrics-explorer/

Test queries here:
https://ga-dev-tools.appspot.com/query-explorer/

General API docs: [Analytics Reporting API](https://developers.google.com/analytics/devguides/reporting/core/v4)

I used the [Account Explorer](https://ga-dev-tools.appspot.com/account-explorer/) to quickly poke around the
accounts/properties, but later discovered that I just needed the viewId that is available on the main
Google Analytics browser in the top-left dropdown.


