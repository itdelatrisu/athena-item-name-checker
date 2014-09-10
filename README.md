# Athena Item Name Checker
This tool checks the item names in an Athena item database against a client-side
Ragnarok Online item name table, finding all differences and generating a new
database.  Written for [rAthena](https://github.com/rathena/rathena).

## Required Files
* **item_db.txt**: Athena item database, usually located inside the `db/` or 
`db/(pre-)re/` directory.
* **idnum2itemdisplaynametable.txt**: Ragnarok Online identified item display name
table, located inside either a GRF file (*data.grf*, *rdata.grf*) or the `data/`
directory.

## Usage
```
java AthenaItemNameChecker help
- shows usage information

java AthenaItemNameChecker
- runs using default names
  (item_db.txt, idnum2itemdisplaynametable.txt, output_diff.txt, output_diff_formatted.txt, output_db.txt)

java AthenaItemNameChecker server_file client_file output_diff_file output_diff_formatted_file output_db_file
- runs using supplied file names

java AthenaItemNameChecker diff_file
- generates new database file using default names
  (item_db.txt, output_db.txt)

java AthenaItemNameChecker server_file diff_file output_db_file
- generates new database file using supplied file names
```

## License
**This software is licensed under GNU GPL version 3.**
You can find the full text of the license [here](LICENSE).
