# LOOP Group Project

This repository collects the seven Java/JavaFX components in separate, intentionally unconnected module folders. All components use the single SQLite database at `database/loop.db`.

## Modules

- `modules/customer` - customer management
- `modules/finance` - finance and reporting
- `modules/orders` - order management
- `modules/delivery` - delivery and logistics
- `modules/reviews` - reviews and ratings
- `modules/inventory` - inventory and warehousing
- `modules/product` - product pages and seller product management

The modules retain their original build layouts. The shared Team Hub loads the selected module into the current JavaFX window, so hub buttons switch pages instead of opening additional application windows. Domain prefixes prevent incompatible same-named tables from colliding.

## Run the integrated Team Hub

On Windows, double-click `run-hub.cmd` from the repository root. The Java launcher opens the Product Team Hub, whose buttons display each connected module in the same window. Delivery and Reviews provide separate role views; Customer retains its login page.

## Login details

Only the **Customers** component has authentication. A ready-to-use customer and the Customer Super Admin access are included:

| Access | Email | Password |
| --- | --- | --- |
| Demo Customer | `customer@loop.com` | `customer123` |
| Customer Super Admin | No email required | `admin123` |

Use **Super Admin** on the Customer login page for administrator access. New customers can also select **Register** and create their own account.

Products, Orders, Delivery, Inventory, Reviews, and Finance do not require login and contain no login pages. Every component page includes a **Back to Team Hub** button.

For Eclipse development, follow [`ECLIPSE_SETUP.md`](ECLIPSE_SETUP.md) and import the root project using **Maven > Existing Maven Projects**.

## Database

Run modules from the repository root so relative paths resolve to `database/loop.db`. The committed database includes existing data from the supplied SQLite files and empty schemas for components that originally created their database at runtime. See `database/SCHEMA.md` for the table inventory.

The original source archives are not included or modified. Generated `target`, `bin`, class, crash-log, macOS metadata, and duplicate database files are excluded.

`database/loop.sql` is a portable SQL dump of the merged file. The integrity check is clean. `PRAGMA foreign_key_check` reports 33 orphan rows in `product_ProductIngredientOption`; those rows already existed in the supplied product database and were retained unchanged.
