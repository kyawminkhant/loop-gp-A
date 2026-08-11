# LOOP Group Project

This repository collects seven Java/JavaFX components in independently editable module folders. All components use the single normalized SQLite database at `database/loop.db`.

## Modules

- `modules/customer` - customer management
- `modules/finance` - finance and reporting
- `modules/orders` - order management
- `modules/delivery` - delivery and logistics
- `modules/reviews` - reviews and ratings
- `modules/inventory` - inventory and warehousing
- `modules/product` - product pages and seller product management

The modules retain their original build layouts. The shared Team Hub loads the selected module into the current JavaFX window, so hub buttons switch pages instead of opening additional application windows. Product, Order, Inventory, Finance, and Reviews now share the canonical Product and Order records; prefixes remain for data that is genuinely specific to one component.

## Run the integrated Team Hub

On Windows, double-click `run-hub.cmd` from the repository root. The Java launcher opens the Product Team Hub, whose buttons display each connected module in the same window. Delivery and Reviews provide separate role views; Customer retains its login page.

## Login details

Only the **Customers** component has authentication. A ready-to-use customer and the Customer Super Admin access are included:

| Access | Email | Password |
| --- | --- | --- |
| Demo Customer | `customer@loop.com` | `customer123` |
| Customer Super Admin | No email required | `admin123` |

Additional preference demo accounts use the shared password `demo123`:
`maya@loop.demo`, `noah@loop.demo`, `zara@loop.demo`, `liam@loop.demo`,
`aisha@loop.demo`, and `oliver@loop.demo`. Each is seeded with a unique,
randomized combination of food preferences. Logging in through **Customers**
opens a personalized Product catalogue; the hub's **Products** button remains
the normal, unpersonalized catalogue. Each demo account also includes two
orders and two chef reviews so its dashboard is populated.
The Demo Customer row is an optional sign-in account; it is not the default
identity. Pages opened without a Customer login display **Guest**.

The larger review dataset also includes review personas such as Naruto
Uzumaki, Monkey D. Luffy, Satoru Gojo, Levi Ackerman, and other familiar anime
characters. Customer-side reviewer profiles use `reviewer-001@loop.demo`
through `reviewer-040@loop.demo`, all with password `review123`. They primarily
exist to make the seeded food and chef review histories realistic.

Use **Super Admin** on the Customer login page for administrator access. New customers can also select **Register** and create their own account.

After unlocking the Customer Super Admin panel, **Product Management** opens
the seller catalogue manager in the same window. **Back To Admin** returns to
the Customer administrator page. Review Moderation is available beside it.

Products, Orders, Delivery, Inventory, Reviews, and Finance do not contain
separate login pages. Reviews opens as the active Customer account, or as
**Guest** when no Customer session exists. The **Reviews** breadcrumb on a
food-details page opens that food's reviews, and **Back to Food** returns to the
same item. Customers cannot flag other customers' reviews. A local content
check automatically hides clearly inappropriate submissions from customer
lists and sends them to the administrator's moderation queue. To access moderation, open
**Customers > Super Admin**, unlock with `admin123`, then select
**Review Moderation**; there is no Admin button in the customer Reviews pages.
Helpful and Unhelpful choices are reversible: select the same choice again to
remove it, or select the other choice to switch the vote.
Click the **LOOP logo** on a component page to return to the Team Hub.

Orders and Delivery share `orders_Orders`: Delivery creates one linked
`delivery_Deliveries` record per order, and driver assignment or completion
updates the same order status shown by the Orders component.

For Eclipse development, follow [`ECLIPSE_SETUP.md`](ECLIPSE_SETUP.md) and import the root project using **Maven > Existing Maven Projects**.

## Database

Run modules from the repository root so relative paths resolve to `database/loop.db`. The committed database contains 32 real tables instead of the original 53, plus four compatibility views. The five yearly Inventory tables are now one `inventory_Stock` table with a `stockYear` column. Orders, Finance, Reviews, and Inventory reuse the canonical `product_*` catalogue instead of storing copied Product tables. See `database/SCHEMA.md` for the current structure.

Inventory analytics, warehouse totals, ingredient queries, additions, and
transfers are calculated from the live `inventory_Stock` and
`product_Ingredient` records. A transfer updates both warehouse quantities in
one transaction and records the action in `inventory_stock_TransactionLog`.
The Ingredient Inventory page can search and sort by ingredient name or ID.
**Add Ingredient** saves the new ingredient, its first stock record, and an
optional uploaded image to the shared `database` folder, then refreshes the
grid. Inventory reports are exported as PDF files; the save dialog starts in
`database/reports` and the page displays the final saved path.

The **Warehouse Deliveries** page shows inbound ingredient shipments for ten
London service areas. Deliveries move from Scheduled to In Transit and then
Delivered automatically while the combined app is running. A completed
delivery increases only the destination warehouse's stock and records the
change in the Inventory transaction log. The page also provides manual demo
buttons for creating or advancing a delivery.

Product availability is calculated from each food's required default
ingredient IDs and the live Inventory totals. The normal hub Product page uses
the overall stock view. When a customer logs in, their delivery address is
assigned to a local warehouse and the personalised catalogue checks only that
warehouse. A food remains visible but is inactive when a required ingredient
is unavailable locally, with the missing ingredients shown on its card.
Completing a replenishment delivery makes it available again the next time the
catalogue is loaded.

Finance now includes a **UK Locations** report built from those same Inventory
warehouses. It assigns each order to the warehouse serving the customer's
delivery address and shows revenue, product cost, profit, current stock,
unavailable ingredients, and inbound deliveries for all ten service areas.
Guest or unmatched addresses use the Central London warehouse. The location
report can be exported as CSV from the Finance screen.

The original source archives are not included or modified. Generated `target`, `bin`, class, crash-log, macOS metadata, and duplicate database files are excluded.

`database/loop.sql` is a portable SQL dump of the simplified file. Both `PRAGMA integrity_check` and `PRAGMA foreign_key_check` pass. The data-preserving migrations are kept in `database/migrations`, including `002_location_inventory_deliveries.sql` for the location-based stock and delivery system.
