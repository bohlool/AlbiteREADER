/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.albite.dictionary;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.albite.util.text.TextTools;

/**
 *
 * @author albus
 */
public class LocalDictionary extends Dictionary {

    public static final String FILE_EXTENSION = ".ald";
    public static final int MAGIC_NUMBER = 1095516740;

    private final DataInputStream data;
    private final InputStream in;

    private final int indexPosition;

    private char[][]  indexEntryNames = null;
    private int[]     indexEntryPositions = null;

    public LocalDictionary(final InputStream in)
            throws DictionaryException {

        this.in = in;

        in.mark(Integer.MAX_VALUE);
        data = new DataInputStream(in);

        try {
            /*
             * Check magic number
             */
            if (data.readInt() != MAGIC_NUMBER) {
                throw new DictionaryException();
            }

            /*
             * Read header
             */
            title = data.readUTF();
            language = data.readShort();
            indexPosition = data.readInt();

        } catch (IOException e) {
            throw new DictionaryException("Dictionary is corrupted");
        }
    }

    /**
     * Loads the index
     * @throws DictionaryException
     */
    public final void load()
            throws DictionaryException {

        if (indexEntryNames != null) {
            /*
             * Dict already loaded.
             */
            return;
        }

        System.out.println("Loading dictionary...");

        try {
            in.reset();

//            long l = 0;
//            for (int i = 0; i < indexPosition; i++) {
//                l = in.skip(1);
//
//                if (l != 1) {
//                    System.out.println("#" + i + ":" + l);
//                }
//
//                if (i % 10000 == 0) {
//                    System.out.println(i);
//                }
//            }
//            long l = 0;
//            for (int i = 0; i < indexPosition; i++) {
//                l = in.read();
//
//                if (l == -1) {
//                    System.out.println("#" + i + ":" + l);
//                }
//
//                if (i % 10000 == 0) {
//                    System.out.println(i);
//                }
//            }
            int left = indexPosition;
            while (left > 0) {
                left -= (int) in.skip(left);
                System.out.println(left);
            }
            
            System.out.println("ready");
            
            final int wordsCount = data.readInt();
            System.out.println("Words count: " + wordsCount);
            indexEntryNames = new char[wordsCount][];
            indexEntryPositions = new int[wordsCount];

            for (int i = 0; i < wordsCount; i++) {
                indexEntryNames[i] = TextTools.readUTF(data);
                indexEntryPositions[i] = data.readInt();
            }

            System.out.println("done");
        } catch (IOException e) {
            throw new DictionaryException("Cannot load dictionary index");
        }
    }

    /**
     * Unloads the index, thus freeing memory.
     */
    public final void unload() {
        indexEntryNames = null;
        indexEntryPositions = null;
    }

    public final String[] lookUp(final String lookingFor)
            throws DictionaryException {

        load();

        final char[] text = lookingFor.toCharArray();

        int searchResult = TextTools.binarySearch(indexEntryNames, text);

        if (searchResult >= 0) {
            /*
             * The word was found, so no suggestions neccessary.
             */
            return new String[] {getDefinition(searchResult)};
        }

        /*
         * We need to increment the found index by one
         * as it has been decreased by one by the indexSearch method.
         */
        searchResult = -searchResult + 1;

        System.out.println("index: " + searchResult);

        /*
         * Returns a maximum of 11 suggestions.
         */
        final int offset = NUMBER_OF_SUGGESTIONS / 2;

        int left  = searchResult - offset;
        int right = searchResult + offset;

        /*
         * First check left side, if the "center" (i.e. searchResult)
         * is too much in the left
         */
        if (left < 0) {
            left = 0;
            right = NUMBER_OF_SUGGESTIONS;
        }

        /*
         * Check if the "center" is too much in the right.
         */
        if (right >= indexEntryNames.length) {
            right = indexEntryNames.length - 1;
            left = right - NUMBER_OF_SUGGESTIONS;
        }

        /*
         * This might happen in the extreme case of a dictionary
         * having less number of items than NUMBER_OF_SUGGESTIONS
         * However, this might be quite possible for a simple
         * in-book dictionary.
         */
        if (left < 0) {
            left = 0;
        }

        final int len = right - left + 1;

        final String[] res = new String[len];

        for (int i = 0; i < len; i++) {
            res[i] = new String(indexEntryNames[left + i]);
        }

        return res;
    }

    public final String getDefinition(final String lookingFor)
            throws DictionaryException {

        final char[] text = lookingFor.toCharArray();
        final int searchResult = TextTools.binarySearch(indexEntryNames, text);

        if (searchResult < 0) {
            return WORD_NOT_FOUND;
        }

        return getDefinition(searchResult);
    }

    private String getDefinition(final int index)
            throws DictionaryException {

        load();

        if (index < 0 || index > indexEntryPositions.length) {
            return WORD_NOT_FOUND;
        }

        final int pos = indexEntryPositions[index];

        try {
            data.reset();
            data.skip(pos);
            return data.readUTF();
        } catch (IOException e) {
            return WORD_NOT_FOUND;
        }
    }
}
