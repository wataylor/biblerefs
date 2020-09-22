# A web site for generating parallel Bible passages for teaching English

It also generates parallel Bible passages in various languages to help missionaries teach Engilsh as a means of
spreading the gospel.

## Usage ##

**index.html** has radio buttons to select a language and a text box to enter a Bible reference such as Ge. 1:1-10.  The program displays the selected passage.

The checkboxes select additional languages for parallel passage display.  If you select the English radio button and check the Spanish checkbox, you get the passage in English and in Spanish.  This is useful for teaching English as a means of sperading the gospel.

## Bible Reference Database Notes

The Verses table accidently repeated verse references which should have been unique keys.  This MySQL query identifies repeated combinations:

**SELECT Code, Chapter, Verse, COUNT(1) rowcount FROM Verses GROUP BY Code, Chapter, Verse HAVING COUNT(1) > 1;**

Once duplicates were eliminated, this created the key:

**alter table Verses add constraint `col` unique key(Code, Chapter, Verse);**

Adding the key made the program to load verses run about twice as fast.

Determining default character set  and collation settings

** show variables like 'char%'; **
** show variables like '%coll%';  **

## Adding a new language

These tables need a new column for each language.  These alter table statements were used for adding Thai:

** alter table BookNames add column ThaiBook varchar(30); **

** alter table FormalBookNames add column ThaiBook varchar(30); **

** alter table Verses add column ThaiContent mediumtext; **

The database has been set to default to TUF8 so most of these columns should be stored in UTF-8.

###Writing UTF8

Add the following after the MySQL JDBC URL:
** ?useUnicode=yes&characterEncoding=UTF-8 **
