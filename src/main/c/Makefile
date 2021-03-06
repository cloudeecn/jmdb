# Makefile for liblmdb (Lightning memory-mapped database library).

########################################################################
# Configuration. The compiler options must enable threaded compilation.
#
# Preprocessor macros (for CPPFLAGS) of interest...
# Note that the defaults should already be correct for most
# platforms; you should not need to change any of these.
# Read their descriptions in mdb.c if you do:
#
# - MDB_USE_POSIX_SEM
# - MDB_DSYNC
# - MDB_FDATASYNC
# - MDB_USE_PWRITEV
#
# There may be other macros in mdb.c of interest. You should
# read mdb.c before changing any of them.
#
#CC	= gcc
W	= -W -Wall -Wno-unused-parameter -Wbad-function-cast
THREADS = -pthread
OPT = -O2 -g
CFLAGS	= $(THREADS) $(OPT) $(W) $(XCFLAGS)
LDLIBS	=
SOLIBS	=
prefix	= /usr/local

########################################################################

IHDRS	= 
ILIBS	= libjmdb.so
IPROGS	= 
IDOCS	= 
PROGS	= 
all:	$(ILIBS) $(PROGS)

install: $(ILIBS) $(IPROGS) $(IHDRS)
	for f in $(IPROGS); do cp $$f $(DESTDIR)$(prefix)/bin; done
	for f in $(ILIBS); do cp $$f $(DESTDIR)$(prefix)/lib; done
	for f in $(IHDRS); do cp $$f $(DESTDIR)$(prefix)/include; done
	for f in $(IDOCS); do cp $$f $(DESTDIR)$(prefix)/man/man1; done

install-jni: $(ILIBS) $(IPROGS) $(IHDRS)
	mkdir -p $(JNIDESTDIR); for f in $(ILIBS); do cp $$f $(JNIDESTDIR); done

clean:
	rm -rf $(PROGS) *.[ao] *.so *~ testdb

test:	all

libjmdb.so:	mdb.o midl.o jmdb.o
#	$(CC) $(LDFLAGS) -pthread -shared -Wl,-Bsymbolic -o $@ mdb.o midl.o $(SOLIBS)
	$(CC) $(LDFLAGS) -shared -o $@ mdb.o midl.o jmdb.o $(SOLIBS)

mdb.o: mdb.c lmdb.h midl.h
	$(CC) $(CFLAGS) -fPIC $(CPPFLAGS) -c mdb.c

midl.o: midl.c midl.h
	$(CC) $(CFLAGS) -fPIC $(CPPFLAGS) -c midl.c

jmdb.o: jmdb.c jmdb.h jni.h
	$(CC) $(CFLAGS) -fPIC $(CPPFLAGS) -c jmdb.c

%:	%.o
	$(CC) $(CFLAGS) $(LDFLAGS) $^ $(LDLIBS) -o $@

%.o:	%.c lmdb.h
	$(CC) $(CFLAGS) $(CPPFLAGS) -c $<
