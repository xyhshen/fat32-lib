
package com.meetwise.fs.fat;

import com.meetwise.fs.BlockDevice;
import com.meetwise.fs.FSDirectory;
import com.meetwise.fs.FSDirectoryEntry;
import com.meetwise.fs.fat.FatLfnDirectory.LfnEntry;
import com.meetwise.fs.util.RamDisk;
import java.io.IOException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public class FatLfnDirectoryTest {

    private BlockDevice dev;
    private BootSector bs;
    private Fat16RootDirectory rootDirStore;
    private Fat fat;
    private FatLfnDirectory dir;

    @Before
    public void setUp() throws IOException {
        this.dev = new RamDisk(1024 * 1024);
        SuperFloppyFormatter sff = new SuperFloppyFormatter(dev);
        sff.format();
        
        this.bs = BootSector.read(dev);
        this.rootDirStore = Fat16RootDirectory.read(
                (Fat16BootSector) bs, false);
        this.fat = Fat.read(bs, 0);
        this.dir = new FatLfnDirectory(rootDirStore, fat);
    }

    @Test
    @Ignore
    public void testSubDirectoryTimeStamps() throws IOException {
        System.out.println("subDirectoryTimeStamps");
        
        final LfnEntry subDirEntry = dir.addDirectory("testDir");
        assertTrue(subDirEntry.isDirectory());
        
        final FSDirectory subDir = subDirEntry.getDirectory();
        final FSDirectoryEntry dot = subDir.getEntry(".");

        assertNotNull(dot);
        assertEquals(subDirEntry.getCreated(), dot.getCreated());
        assertEquals(subDirEntry.getLastModified(), dot.getLastModified());
        assertEquals(subDirEntry.getLastAccessed(), dot.getLastAccessed());
    }

    @Test
    public void testAddTooManyDirectories() throws IOException {
        System.out.println("addTooManyDirectories");

        int count = 0;
        
        do {
            int freeBeforeAdd = fat.getFreeClusterCount();
            try {
                dir.addDirectory("this is test directory with index " + count);
            } catch (RootDirectoryFullException ex) {
                assertEquals(freeBeforeAdd, fat.getFreeClusterCount());
                return;
            }
        } while (true);
    }

    @Test
    public void testGeneratedEntries() throws IOException {
        System.out.println("generatedEntries");

        final int orig = rootDirStore.getEntryCount();
        System.out.println("orig=" + orig);
        dir.flush();
        assertEquals(orig, rootDirStore.getEntryCount());
        dir.addFile("hallo");
        dir.flush();
        assertTrue(orig < rootDirStore.getEntryCount());
    }
    
    @Test
    public void testGetStorageDirectory() {
        System.out.println("getStorageDirectory");
        
        assertEquals(rootDirStore, dir.getStorageDirectory());
    }
    
    @Test
    public void testIsDirty() throws IOException {
        System.out.println("isDirty");
        
        assertFalse(dir.isDirty());
        dir.addFile("a file");
        assertTrue(dir.isDirty());
    }
    
    @Test
    @Ignore
    public void testGetLabel() {
        System.out.println("getLabel");
        
        fail("The test case is a prototype.");
    }
    
    @Test
    @Ignore
    public void testSetLabel() throws Exception {
        System.out.println("setLabel");
        
        dir.setLabel("a file system label");
    }
    
    @Test
    public void testAddFile() throws Exception {
        System.out.println("addFile");
        
        assertNotNull(dir.addFile("A good file"));
    }
    
    @Test
    public void testAddDirectory() throws Exception {
        System.out.println("addDirectory");
        
        final String name = "A nice directory";
        final LfnEntry newDir = dir.addDirectory(name);
        assertNotNull(newDir);
        assertTrue(newDir == dir.getEntry(name));
        assertTrue(newDir.getDirectory() == dir.getEntry(name).getDirectory());
    }
    
    @Test
    public void testGetEntry() throws IOException {
        System.out.println("getEntry");
        
        final String NAME = "A fine File";
        final LfnEntry file = dir.addFile(NAME);
        assertEquals(file, dir.getEntry(NAME));
    }
    
    @Test
    public void testFlush() throws Exception {
        System.out.println("flush");
        
        dir.addFile("The perfect File");
        assertTrue(dir.isDirty());

        dir.flush();
        assertFalse(dir.isDirty());
    }
    
}
