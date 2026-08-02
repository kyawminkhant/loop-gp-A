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

The modules have not been wired together and retain their original build layouts. Only database paths and SQL table references were changed. Domain prefixes prevent incompatible same-named tables from colliding.

## Database

Run modules from the repository root so relative paths resolve to `database/loop.db`. The committed database includes existing data from the supplied SQLite files and empty schemas for components that originally created their database at runtime. See `database/SCHEMA.md` for the table inventory.

The original source archives are not included or modified. Generated `target`, `bin`, class, crash-log, macOS metadata, and duplicate database files are excluded.

`database/loop.sql` is a portable SQL dump of the merged file. The integrity check is clean. `PRAGMA foreign_key_check` reports 33 orphan rows in `product_ProductIngredientOption`; those rows already existed in the supplied product database and were retained unchanged.
