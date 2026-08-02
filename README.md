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

Use **Super Admin** on the Customer login page for administrator access. New customers can also select **Register** and create their own account.

Products, Orders, Delivery, Inventory, Reviews, and Finance do not require login and contain no login pages. Click the **LOOP logo** on a component page to return to the Team Hub.

For Eclipse development, follow [`ECLIPSE_SETUP.md`](ECLIPSE_SETUP.md) and import the root project using **Maven > Existing Maven Projects**.

## Database

Run modules from the repository root so relative paths resolve to `database/loop.db`. The committed database contains 28 real tables instead of the original 53, plus four compatibility views. The five yearly Inventory tables are now one `inventory_Stock` table with a `stockYear` column. Orders, Finance, Reviews, and Inventory reuse the canonical `product_*` catalogue instead of storing copied Product tables. See `database/SCHEMA.md` for the current structure.

The original source archives are not included or modified. Generated `target`, `bin`, class, crash-log, macOS metadata, and duplicate database files are excluded.

`database/loop.sql` is a portable SQL dump of the simplified file. Both `PRAGMA integrity_check` and `PRAGMA foreign_key_check` pass. The data-preserving migration is kept at `database/migrations/001_simplify_shared_schema.sql`.
