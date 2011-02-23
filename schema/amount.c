/*
 * Convery long cents to formatted string
 *
 * Copyright © 2011 Operational Dynamics Consulting, Pty Ltd
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted through http://research.operationaldynamics.com/
 */

#include <stdlib.h>
#include <sqlite3ext.h>
#include <glib.h>
#include <string.h>

SQLITE_EXTENSION_INIT1

/*
 * Convet a long number of cents into dollars and pennies.
 */
/*
 * Meets the signature requirements of (*xFunc), the 6th argument to the
 * sqlite3_create_function() function.
 */
static void
convert
(
	sqlite3_context* context,
	int argc,
	sqlite3_value** argv
)
{
	int type;
	int num, required, direction;
	int i;
	const unsigned char* code;
	int dollars, pennies;
	char buf[15];
	char* str;
	char* result;
	
	if (argc == 0) {
		return;
	}

	type = sqlite3_value_type(argv[0]);

	if (argc == 4) {
		direction = sqlite3_value_int(argv[2]);
		required = sqlite3_value_int(argv[3]);
	} else {
		required = 0;
		direction = 0;
	}

	if ((type != SQLITE_INTEGER) || (required != direction)) {
		if (argc == 1) {
			num = 10;	
		} else {
			num = 14;
		}
		for (i = 0; i < num; i++) {
			buf[i] = ' ';
		}
		buf[i] = '\0';
		sqlite3_result_text(context, buf, -1, SQLITE_TRANSIENT);
		return;
	}

	num = sqlite3_value_int(argv[0]);
	
	dollars = num / 100;
	pennies = num % 100;

	/*
	 * Pass a positive number of cents to printf, and then, afterward,
	 * handle the case where -0.xx shows up as 0.xx.
	 */

	if (num < 0) {
		pennies = -pennies;
	}

	result = &buf[0];

	if (argc >= 1) {
		str = &buf[0];
		sqlite3_snprintf(11, str, "%7d.%02d", dollars, pennies);

		if ((num < 0) && (dollars == 0)) {
			buf[5] = '-';
		}
	}

	if (argc >= 2) {
		code = sqlite3_value_text(argv[1]);
		str = &buf[10];
		sqlite3_snprintf(5, str, "%4s", code);
	}

	sqlite3_result_text(context, result, -1, SQLITE_TRANSIENT);
}


/*
 * Pad a string with trailing spaces out to given width.
 */
/*
 * Meets the signature requirements of (*xFunc), the 6th argument to the
 * sqlite3_create_function() function.
 */
static void
pad
(
	sqlite3_context* context,
	int argc,
	sqlite3_value** argv
)
{
	const gchar* text;
	int len, width, actual;
	gchar* dest;
	int i;
	
	text = (const gchar*) sqlite3_value_text(argv[0]);
	width = sqlite3_value_int(argv[1]);

	if (!g_utf8_validate(text, -1, NULL)) {
		return;
	}

	len = strlen(text);
	dest = (gchar*) g_newa(gchar, len + 1);
	dest[len] = '\0';
	g_utf8_strncpy(dest, text, width);
	actual = g_utf8_strlen(dest, -1);

	if (actual < width) {
		i = strlen(dest);
		while (actual < width) {
			dest[i] = ' ';
			i++;
			actual++;
		} ;
		dest[i] = '\0';
	}

	sqlite3_result_text(context, dest, -1, SQLITE_TRANSIENT);
}


/*
 * Meets the signature expectations of (*xEntryPoint) as the specified default
 * of sqlite3_load_extension().
 */
int
sqlite3_extension_init
(
	sqlite3* db,
	const char** err,
	const sqlite3_api_routines* api
)
{
	SQLITE_EXTENSION_INIT2(api)

	sqlite3_create_function(db, "money", -1, SQLITE_ANY, NULL, convert, NULL, NULL);
	sqlite3_create_function(db, "pad", 2, SQLITE_ANY, NULL, pad, NULL, NULL);

	return SQLITE_OK;
}
