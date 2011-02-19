all: load run

.PHONY: clean load reload run

reload: clean load run

clean:
	rm -f accounts.db

load: accounts.db
accounts.db: create.ddl mock.ddl debug.ddl
	sqlite3 accounts.db < create.ddl
	sqlite3 accounts.db < mock.ddl
	sqlite3 accounts.db < debug.ddl

run: accounts.db
	sqlite3 accounts.db
