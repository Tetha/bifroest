package com.goodgame.profiling.commons.output;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AtomicWriteFileOutputStream extends OutputStream {
    private static final Logger log = LogManager.getLogger();
    
    private final Path atomicallyUpdatedFile;
    private final Path temporaryFile;
    private final OutputStream toTemporaryFile;

    public AtomicWriteFileOutputStream( Path atomicallyUpdatedFile ) throws IOException {
        this( atomicallyUpdatedFile,
              p -> new BufferedOutputStream( new FileOutputStream( p.toString() ) ) );
    }

    public AtomicWriteFileOutputStream( Path atomicallyUpdatedFile, OutputStreamFactory factory ) throws IOException {
        this.atomicallyUpdatedFile = atomicallyUpdatedFile;
        this.temporaryFile = Files.createTempFile(
                atomicallyUpdatedFile.getParent(),
                "." + atomicallyUpdatedFile.getFileName().toString(),
                ".temp",
                PosixFilePermissions.asFileAttribute( EnumSet.<PosixFilePermission>of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.GROUP_READ,
                        PosixFilePermission.OTHERS_READ
                ) )
        );
        this.toTemporaryFile = factory.create( this.temporaryFile );
    }


    @Override
    public void close() throws IOException {
        log.debug( "Moving {} to {}", temporaryFile, atomicallyUpdatedFile );
        Files.move( temporaryFile, atomicallyUpdatedFile, StandardCopyOption.REPLACE_EXISTING );
        toTemporaryFile.close();
    }

    // Forward all calls, in case the used outputstream does something smart
    @Override
    public void flush() throws IOException {
        toTemporaryFile.flush();    
    }

    @Override
    public void write( byte[] b ) throws IOException {
        toTemporaryFile.write( b );
    }

    @Override
    public void write( byte[] b, int off, int len ) throws IOException {
        toTemporaryFile.write( b, off, len );
    }

    @Override
    public void write( int b ) throws IOException {
        toTemporaryFile.write( b );
    }

    @FunctionalInterface
    public interface OutputStreamFactory {
        OutputStream create( Path p ) throws IOException;
    }
}
