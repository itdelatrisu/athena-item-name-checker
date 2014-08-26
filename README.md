# Athena Item Name Checker
This tool checks the item names in an Athena item database against a client-side
Ragnarok Online item name table, printing all differences.  Written for
[rAthena](https://github.com/rathena/rathena).

## Usage
```
java AthenaItemNameChecker
- runs using default names
  (item_db.txt, idnum2itemdisplaynametable.txt, output.txt)

java AthenaItemNameChecker server_file client_file output_file
- runs using supplied file names
```

## License
**This software is licensed under GNU GPL version 3.**
You can find the full text of the license [here](LICENSE).
