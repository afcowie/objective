all: load run

ifdef V
else
MAKEFLAGS=-s
endif

.PHONY: clean load reload run extension

reload: clean load

clean:
	@echo "RM accounts.db"
	rm -f accounts.db
	@echo "RM shared library"
	rm -f ../tmp/native/account.o
	rm -f ../tmp/native/account.so


load: accounts.db
accounts.db: create.ddl mock.ddl debug.ddl
	rm -f accounts.db.tmp
	@echo "SQLITE create.ddl"
	sqlite3 -bail accounts.db.tmp < create.ddl
	@echo "SQLITE mock.ddl"
	sqlite3 -bail accounts.db.tmp < mock.ddl
	mv accounts.db.tmp accounts.db

run: accounts.db ../tmp/native/amount.so
	@echo "SQLITE debug.ddl"
	-sqlite3 -init debug.ddl -interactive accounts.db


#
# Build SQLite extension shared library with money() and pad() functions.
#

LINK=/usr/bin/gcc-4.4 -g -shared -Wall -fPIC
CC=/usr/bin/gcc-4.4 -g -O2 -Wall -fPIC -Wno-int-to-pointer-cast -Wno-pointer-to-int-cast  -Werror-implicit-function-declaration -Wfatal-errors


extension: ../tmp/native/amount.so

../tmp/native/amount.so: ../tmp/native/amount.o
	@echo "LINK amount.so"
	$(LINK) -lsqlite3 -lglib-2.0 -o $@ $<

../tmp/native:
	@echo "MKDIR ../tmp/native"
	mkdir $@

../tmp/native/amount.o: ../tmp/native amount.c
	@echo "CC amount.so"
	$(CC) -I/usr/include/glib-2.0 -I/usr/lib/glib-2.0/include \
		-o ../tmp/native/amount.o -c amount.c
	

