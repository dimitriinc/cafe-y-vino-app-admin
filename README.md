# Cafe y Vino Admin App

The administrator side of the Cafe y Vino system.
An Android app for administrators of the restaurant.

## Functionality

- _Menu managment_ - the menu is stored in the Cloud Firestore. [The client side](https://github.com/dimitriinc/cafe-y-vino-app-client-kotlin) displays only the products, marked as 'present'. The admin app can update the 'present' field of a product, executing this way the control over what version of the menu is displayed to a customer.

- _Order management_ - all the orders yet to be served are displayed on the screen to admin. Admin can manage the content of an order: change the quantity of a product, delete it, or add a custom product (something that's not on the official menu).
  When an order is served to the table, its products are moved by admin to the bill collection.
  Orders can arrive in two ways:

  - **customer creates one in the client app** - on the event, a notification arrives to all the admins, where admin can confirm the order to the customer by an FCM message, and also check the content of the order in a separate fragment dialog.

  - **admin creates a custom one** - thus, the admin app can process not only the orders of the clients who have the Cafe y Vino Client app, but all the orders.
    To create a custom order, admin must complete three steps:
    - _choose the table_ - the table is chosen from an SQLite DB. On initiation the table database is populated with an array of fixed tables, but admin can create a new, custom one,
    - _add products to the order_ - at this stage the order is represented by another SQLite DB, where admin can add or remove either the products from the menu, or the products created in a custom manner,
    - _publish the order_ - the order is stored in the Cloud Firestore, an FCM message is sent, notifying all the admins about a new order.

- _Bill management_ - all the orders are identified either by the UID of the customer (in case the order is created by the customer side), or by the table to which they are assigned (in case the order is created by admin). When marked as served, all the orders of the same identifier accumulate inside one bill. The collection of such open bills is displayed to admin on the screen; where admin can manipulate the content of the bill, adding or removing products, and adding custom products. All the changes made by admin are displayed to the customer in his app almost instantly. On cancelation of a bill, admin must assign it a payment method; admin can divide the total price between different payment methods. After this, the bill is stored in the collection of paid bills inside the Cloud Firestore.

- _Reservation management_ - admin chooses a date and can see the set of reservations for that date: one fragment for the daytime, another for the nighttime.
  In this activity, admin can:

  - accept or reject reservation requests,
  - in case of accepted reservations, admin can mark the reservation as 'arrived', which will open new functionality on the customer side, and send a welcoming FCM message there (i.e. the customer with a reservation doesn't need to send a request to enter (see the functionality of the [customer side](https://github.com/dimitriinc/cafe-y-vino-app-client-kotlin))),
  - create a new reservation (e.g. to store a reservation, that arrived not from the customer side, or to block a certain table from being reserved).

- _User management_ - a list of the customers, present at the moment in the restaurant is displayed; admin can change the number of the table assigned to the customer, send a personalized FCM message to the customer, or change their state to 'not present', removing them from the list.
  Also admin can execute a customer search: putting in the customer's name admin can find a reference to their account. Admin can send a personalized message to the customer, and also see their personal statistics:

  - all their paid bills
  - how much money the customer has spent during their visits
  - the list of all the ordered products, organized by quantity

- _Other statistics_
  - the list of all the consumed products, organized by quantity
  - admin can also access the collection of paid bills for the chosen day

## Messaging

The app employs a FirebaseMessagingService to receive downstream FCM messages sent from the customer side. On receiving a message, the app displays the appropriate notification, based on the type of action, stored in the message's data payload.

## Technologies

- Java 1.8
- Room API 2.4.2
- Lifecycle API 2.4.0
- Firebase BOM 29.1.0

## Deployment

The app is permanently in the state of internal testing on Google Play, accessible only for the administrators of the restaurant.

## License

This project is licensed under the terms of the MIT license.
