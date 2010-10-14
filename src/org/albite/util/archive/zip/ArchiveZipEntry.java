/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.albite.util.archive.zip;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.InputConnection;
import net.sf.jazzlib.ZipEntry;
import net.sf.jazzlib.ZipFile;

/**
 *
 * @author Svetlin Ankov <galileostudios@gmail.com>
 */
public class ArchiveZipEntry implements InputConnection {

    private final ZipFile zipfile;
    private final ZipEntry zipentry;

    protected ArchiveZipEntry(
            final ZipFile zipfile, final ZipEntry zipentry) {

        if (zipfile == null || zipentry == null) {
            throw new NullPointerException("null input parameters");
        }
        this.zipfile = zipfile;
        this.zipentry = zipentry;
    }

    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    public InputStream openInputStream() throws IOException {
        System.out.println("Opening strem for " + zipentry.getName() + ", CRC: " + Long.toString(zipentry.getCrc(), 16));
        return zipfile.getInputStream(zipentry);
    }

    public int fileSize() {
        return (int) zipentry.getSize();
    }

    public void close() throws IOException {
        /*
         * Does nothing
         */
    }
}