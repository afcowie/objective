all: load extension

ifdef V
else
MAKEFLAGS=-s
endif

.PHONY: clean load reload run extension

reload: clean load

clean:
	@echo "RM\t accounts.db"
	rm -f accounts.db
	@echo "RM\t shared library"
	rm -f ../tmp/native/amount.o
	rm -f ../tmp/native/amount.so


load: accounts.db
accounts.db: create.ddl mock.ddl debug.ddl
	rm -f accounts.db.tmp
	@echo "SQLITE\t create.ddl"
	sqlite3 -bail accounts.db.tmp < create.ddl
	@echo "SQLITE\t mock.ddl"
	sqlite3 -bail accounts.db.tmp < mock.ddl
	mv accounts.db.tmp accounts.db

run: accounts.db ../tmp/native/amount.so
	@echo "SQLITE\t debug.ddl"
	-sqlite3 -init debug.ddl -interactive accounts.db


#
# Build SQLite extension shared library with money() and pad() functions.
#

LINK=/usr/bin/gcc-4.4 -g -shared -Wall -fPIC
CC=/usr/bin/gcc-4.4 -g -O2 -Wall -fPIC -Wno-int-to-pointer-cast -Wno-pointer-to-int-cast  -Werror-implicit-function-declaration -Wfatal-errors


extension: dirs ../tmp/native/amount.so

dirs: ../tmp/native

../tmp/native:
	@echo "MKDIR\t ../tmp/native"
	mkdir ../tmp/native/

../tmp/native/amount.so: ../tmp/native/amount.o
	@echo "LINK\t amount.so"
	$(LINK) -lsqlite3 -lglib-2.0 -o $@ $<

../tmp/native/amount.o: amount.c
	@echo "CC\t amount.o"
	$(CC) -I/usr/include/glib-2.0 -I/usr/lib/glib-2.0/include \
		-o ../tmp/native/amount.o -c amount.c
	

