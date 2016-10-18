/**
 * The MIT License (MIT) https://opensource.org/licenses/MIT
 * Copyright (c) 2016 Balwinder Sodhi
 */
package in.jwinnow;

import in.jwinnow.Fingerprint;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test case for {@link Fingerprint}.
 * @author Balwinder Sodhi
 */
public class FingerprintTest {
    
    private String token = null;
    private String text = null;
    private String textWithWhiteSpace = null;

    public FingerprintTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        token = "This is a test for MD5 hashing.";
        text = "This is for generating a fingerprint. We will have more than"
                + " one sentence in the text. Text can be such that we are"
                + " able to form n-grams out of it. I think this much of text"
                + " should be sufficient. OK, this is last sentence!";
        textWithWhiteSpace = "A    B\tC \t\nD E F\t\t1 2\n 3 \n\r4\n\n5";
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getHash method, of class Fingerprint.
     */
    @Test
    public void testGetHash() {
        System.out.println("Testing getHash() ...");
        Fingerprint instance = new Fingerprint();
        int expResult = 5709;
        int result = instance.getHash(token);
        assertEquals(expResult, result);
    }

    /**
     * Test of winnowUsingWords method, of class Fingerprint.
     */
    @Test
    public void testWinnowUsingWords() {
        System.out.println("Testing winnowUsingWords() ...");
        Fingerprint instance = new Fingerprint();
        String expResult = "[27, 1200, 1431, 1698, 1722, 1879, 2005, 2205, "
                + "3023, 4198, 5184, 5714, 5826]";
        String result = instance.winnowUsingWords(text).toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of winnowUsingCharacters method, of class Fingerprint.
     */
    @Test
    public void testWinnowUsingCharacters() {
        System.out.println("Testing winnowUsingCharacters() ...");
        Fingerprint instance = new Fingerprint();
        String expResult = "[18, 19, 138, 144, 179, 268, 325, 493, 551, 640, "
                + "765, 767, 769, 882, 930, 934, 1053, 1109, 1180, 1188, "
                + "1208, 1320, 1456, 1469, 1475, 1522, 1535, 1659, 1689, "
                + "1731, 1765, 1766, 1773, 1774, 1787, 1813, 1926, 1951, "
                + "2102, 2145, 2244, 2362, 2406, 3107, 3240, 3263, 3266, "
                + "3312, 3624, 3836, 4272, 4539, 4663, 4876, 4917]";
        String result = instance.winnowUsingCharacters(text).toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of removeWhiteSpaceAndMakeLowercase method, of class Fingerprint.
     */
    @Test
    public void testRemoveWhiteSpaceAndMakeLowercase() {
        System.out.println("Testing removeWhiteSpaceAndMakeLowercase() ...");
        Fingerprint instance = new Fingerprint();
        String expResult = "abcdef12345";
        String result = instance.removeWhiteSpaceAndMakeLowercase(textWithWhiteSpace);
        assertEquals(expResult, result);
    }

    /**
     * Test of getParams method, of class Fingerprint.
     */
    @Test
    public void testGetParams() {
        System.out.println("Testing getParams() ...");
        Fingerprint instance = new Fingerprint(15, 5);
        HashMap expResult = new HashMap();
        expResult.put("minDetectedLength", 15);
        expResult.put("windowSize", 15 - 5 + 1);
        HashMap result = instance.getParams();
        assertEquals(expResult, result);
    }
    
}
