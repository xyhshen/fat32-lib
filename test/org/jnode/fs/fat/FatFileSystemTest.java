
package org.jnode.fs.fat;

import java.io.InputStream;
import java.util.Iterator;
import org.jnode.driver.block.RamDisk;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public class FatFileSystemTest {

    /**
     * $ cat fat16-test.img.gz | gunzip | hexdump -C
     *
     * @throws Exception
     */
    @Test
    public void testFat16Read() throws Exception {
        System.out.println("testFat16Read");

        final InputStream is = getClass().getResourceAsStream(
                "/data/fat16-test.img.gz");
        
        final RamDisk rd = RamDisk.readGzipped(is);
        final FatFileSystem fatFs = new FatFileSystem(rd, false);
        assertEquals(2048, fatFs.getClusterSize());
        
        final BootSector bs = fatFs.getBootSector();
        assertEquals("mkdosfs", bs.getOemName());
        assertEquals(512, bs.getBytesPerSector());
        assertEquals(4, bs.getSectorsPerCluster());
        assertEquals(1, bs.getNrReservedSectors());
        assertEquals(2, bs.getNrFats());
        assertEquals(512, bs.getNrRootDirEntries());
        assertEquals(20000, bs.getNrLogicalSectors());
        assertEquals(0xf8, bs.getMediumDescriptor());
        assertEquals(20, bs.getSectorsPerFat());
        assertEquals(32, bs.getSectorsPerTrack());
        assertEquals(64, bs.getNrHeads());
        assertEquals(0, bs.getNrHiddenSectors());
        assertEquals(0, bs.getNrTotalSectors());
        assertEquals(0x200, FatUtils.getFatOffset(bs, 0));
        assertEquals(0x2a00, FatUtils.getFatOffset(bs, 1));
        assertEquals(0x5200, FatUtils.getRootDirOffset(bs));
        
        final FatDirectory fatRootDir = fatFs.getRootDir();
        assertEquals(512, fatRootDir.getSize());

        FSEntry entry = fatRootDir.getEntry("testFile");
        assertTrue(entry.isFile());
        assertFalse(entry.isDirectory());

        FSFile file = entry.getFile();
        assertEquals(8, file.getLength());
        
        final FatRootEntry rootEnt = fatFs.getRootEntry();
        assertTrue(rootEnt.isDirectory());
        assertNull(rootEnt.getParent());

        final FSDirectory rootDir = rootEnt.getDirectory();
        System.out.println("   rootDir = " + rootDir);

        Iterator<FSEntry> i = rootDir.iterator();
        assertTrue (i.hasNext());
        
        while (i.hasNext()) {
            final FSEntry e = i.next();
            System.out.println("     - " + e);
        }

        entry = rootDir.getEntry("TESTDIR");
        System.out.println("   testEnt = " + entry);
        assertTrue(entry.isDirectory());
        assertFalse(entry.isFile());

        final FSDirectory testDir = entry.getDirectory();
        System.out.println("   testDir = " + testDir);
        
        i = testDir.iterator();
        
        while (i.hasNext()) {
            final FSEntry e = i.next();
            System.out.println("     - " + e);
        }


    }

    public static void main(String[] args) throws Exception {
        FatFileSystemTest test = new FatFileSystemTest();
        test.testFat16Read();
    }
}
