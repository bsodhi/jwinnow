/**
 * The MIT License (MIT) https://opensource.org/licenses/MIT
 * Copyright (c) 2016 Balwinder Sodhi
 */
package in.jwinnow;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Helper class for calculating "document fingerprint" for input text.
 * Fingerprints are computed using algorithm based on approach as described
 * in this paper:
 * https://theory.stanford.edu/~aiken/publications/papers/sigmod03.pdf
 * 
 * This class is not thread-safe. A separate instance of this class should be
 * used in each thread.
 * 
 * @author Balwinder Sodhi
 */
public class Fingerprint {
    
    /**
     * If there is a substring match at least as long as the guarantee
     * threshold, t, then this match is detected.
     */
    private final int minDetectedLength;
    
    /**
     * Window size for winnowing.
     */
    private int windowSize;

    /**
     * Initializes the instance with supplied minimum threshold for detecting
     * substring matches and the noise threshold. It also initializes the
     * window size for winnowing to:
     * minDetectedLength - noiseThreshold + 1
     * 
     * @param minDetectedLength All substring matches at least this long will
     * be detected.
     * @param noiseThreshold We do not detect any matches shorter than 
     * the noise threshold.
     */
    public Fingerprint(int minDetectedLength, int noiseThreshold) {
        this.minDetectedLength = minDetectedLength;
        if (noiseThreshold > minDetectedLength) {
            throw new IllegalArgumentException("Noise threshold, k, "
                    + "should not be greater than minimum match "
                    + "guarantee threshold, t.");
        }
        this.windowSize = minDetectedLength - noiseThreshold + 1;
    }

    /**
     * Initializes using Fingerprint(8, 4).
     */
    public Fingerprint() {
        this(8, 4);
    }
    
    /**
     * We compute hash for each ngram of the input string. Size of ngram is
     * {@literal minDetectedLength}. N-Grams are formed from characters in the
     * input text.
     * 
     * @param text
     * @return A list of integer hashes for ngrams of the input text.
     */
    private List<Integer> getHashesForNGramsOfChars(String text) {

        List<Integer> hashes = new ArrayList<>();
        if (text.length() < this.minDetectedLength) {
            int h = getHash(text);
            hashes.add(h);
        } else {
            for (int i=0;i<text.length() - this.minDetectedLength + 1; i++) {
                hashes.add(
                    getHash(
                        text.substring(i, i+this.minDetectedLength)
                    ));
            }
        }
        return hashes;
    }
    
    /**
     * We first tokenize the given text using given delimiter to get a list of
     * words. Then we calculate an integer hash of each ngrams/shingle which is
     * formed from these words.
     * @param text
     * @param delim
     * @return A list of integer hashes for ngrams of the input text.
     */
    private List<Integer> getHashesForNGramsOfWords(String text, String delim) {

        Iterator<String> tok = Splitter.on(delim).trimResults()
                .omitEmptyStrings().split(text).iterator();

        List<Integer> ngrams = new ArrayList<>();
        List<String> list = new ArrayList<>();
        while (tok.hasNext()) {
            list.add(tok.next());
            if (list.size() == this.minDetectedLength) {
                ngrams.add(getHash(String.join(" ", list)));
                list.remove(0);
            }
        }
        /**
         * When there are fewer tokens than minDetectedLength
         */
        if (ngrams.isEmpty() && list.size() > 0) {
            ngrams.add(getHash(String.join(" ", list)));
        }
        return ngrams;
    }

    /**
     * A hash function based on MD5. The returned value is the positive integer
     * value obtained by dividing MD5 hash (int value) of input string by 10000.
     * We use {@link Hasher} to compute MD5 hash. You should override this
     * method in case you want to use a different hash function.
     * 
     * @param token
     * @return 
     */
    protected int getHash(String token) {
        Hasher hasher = Hashing.md5().newHasher();
        hasher.putString(token, Charset.defaultCharset());
        int h = hasher.hash().asInt();
        return Math.abs(h%10000);
    }
    
    /**
     * Calculates the fingerprint of input text by using space delimited "words"
     * for making n-grams.
     * 
     * @param text
     * @return A set of Integer hash values representing the fingerprint.
     */
    public Set<Integer> winnowUsingWords(String text) {
        List<Integer> nh = getHashesForNGramsOfWords(text, " ");
        Set<Integer> fp = new TreeSet();
        for (int i=0; i<nh.size()-this.windowSize+1; i++) {
            List<Integer> s = new ArrayList(nh.subList(i, i+this.windowSize));
            fp.add(Collections.min(s));
        }
        return fp;
    }
    
    /**
     * Calculates the fingerprint of input text by using n-grams of characters
     * in the input text. Before computing n-grams, the input text is converted
     * to lowercase and cleaned of all whitespace occurring anywhere in it.
     * 
     * @param text
     * @return A set of Integer hash values representing the fingerprint.
     */
    public Set<Integer> winnowUsingCharacters(String text) {
        text = removeWhiteSpaceAndMakeLowercase(text);
        List<Integer> nh = getHashesForNGramsOfChars(text);
        Set<Integer> fp = new TreeSet();
        for (int i=0; i<nh.size()-this.windowSize+1; i++) {
            List<Integer> s = new ArrayList(nh.subList(i, i+this.windowSize));
            fp.add(Collections.min(s));
        }
        return fp;
    }
    
    /**
     * 
     * @param text
     * @return 
     */
    protected String removeWhiteSpaceAndMakeLowercase(String text) {
        return text.replaceAll("\\s+","").toLowerCase();
    }
    
    /**
     * Returns the currently used parameter values for winnowing.
     * Keys in returned HashMap are: minDetectedLength and windowSize.
     * @return 
     */
    public HashMap getParams() {
        HashMap p = new HashMap();
        p.put("minDetectedLength", this.minDetectedLength);
        p.put("windowSize", this.windowSize);
        return p;
    }

    /**
     * For testing only.
     * @param args 
     */
    public static void main(String[] args) {
        Fingerprint fh = new Fingerprint();
        Set<String> animals = ImmutableSet.of("duck", "monkey");
        Set<String> fruits = ImmutableSet.of("apple", "orange", "banana");        
        Set<List<String>> product = Sets.cartesianProduct(animals, fruits);
        
        String str = product.stream().map(e->e.get(0)+" and "+e.get(1)).
                collect(Collectors.toList()).toString();
        str = str.substring(1, str.length()-1);
        System.out.println("Winnowing params: "+fh.getParams());
        System.out.println("Input string: \""+str+"\"");
        System.out.println("Fingerprint: "+fh.winnowUsingCharacters(str));
    }
}
