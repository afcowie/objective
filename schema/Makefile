all: load run

.PHONY: clean load reload run

reload: clean load run

clean:
	rm -f accounts.db

load: accounts.db
accounts.db: create.ddl mock.ddl debug.ddl
	rm -f accounts.db.tmp
	sqlite3 -bail accounts.db.tmp < create.ddl
	sqlite3 -bail accounts.db.tmp < mock.ddl
	mv accounts.db.tmp accounts.db

run: accounts.db
	sqlite3 -init debug.ddl -interactive accounts.db
