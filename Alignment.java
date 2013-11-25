/*
 * Copyright (C) 2013 Universidad de Alicante
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.digitisation.distance;

import static eu.digitisation.distance.EdOp.*;
import eu.digitisation.math.BiCounter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Alignment alignments as HTML text
 *
 * @author R.C.C
 */
public class Alignment {

    /**
     * @return 3-wise minimum.
     */
    private static int min(int x, int y, int z) {
        return Math.min(x, Math.min(y, z));
    }

    /**
     * Shows text alignment based on a pseudo-Levenshtein distance where
     * white-spaces are not allowed to be confused with text or vice-versa
     *
     * @param first reference string
     * @param second fuzzy string
     * @param file the output file
     */
    public static void asHTML(String first, String second, File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.write(toHTML(first, second));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Alignment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Shows text alignment based on a pseudo-Levenshtein distance where
     * white-spaces are not allowed to be confused with text or vice-versa
     *
     * @param first
     * @param second
     * @return
     */
    private static String toHTML(String first, String second) {
        int i, j;
        int[][] A;
        EditTable B;
        StringBuilder builder = new StringBuilder();

        // intialize
        A = new int[2][second.length() + 1];
        B = new EditTable(first.length() + 1, second.length() + 1);
        // Compute first row
        A[0][0] = 0;
        B.set(0, 0, EdOp.KEEP);
        for (j = 1; j <= second.length(); ++j) {
            A[0][j] = A[0][j - 1] + 1;
            B.set(0, j, EdOp.INSERT);
        }

        // Compute other rows
        for (i = 1; i <= first.length(); ++i) {
            char c1 = first.charAt(i - 1);
            A[i % 2][0] = A[(i - 1) % 2][0] + 1;
            B.set(i, 0, EdOp.DELETE);
            for (j = 1; j <= second.length(); ++j) {
                char c2 = second.charAt(j - 1);
                boolean notSpaces = !(Character.isSpaceChar(c1)
                        || Character.isSpaceChar(c2));
                if (c1 == c2 && notSpaces) {
                    A[i % 2][j] = A[(i - 1) % 2][j - 1];
                    B.set(i, j, EdOp.KEEP);
                } else {
                    A[i % 2][j] = min(A[(i - 1) % 2][j] + 1,
                            A[i % 2][j - 1] + 1,
                            A[(i - 1) % 2][j - 1] + 1);
                    if (A[i % 2][j] == A[(i - 1) % 2][j] + 1) {
                        B.set(i, j, EdOp.DELETE);
                    } else if (A[i % 2][j] == A[i % 2][j - 1] + 1) {
                        B.set(i, j, EdOp.INSERT);
                    } else {
                        B.set(i, j, EdOp.SUBSTITUTE);
                    }
                }
            }
        }

        i = first.length();
        j = second.length();
        while (i > 0 && j > 0) {
            switch (B.get(i, j)) {
                case KEEP:
                    builder.insert(0, first.charAt(i - 1));
                    --i;
                    --j;
                    break;
                case DELETE:
                    builder.insert(0, "<font color=\"red\">"
                            + first.charAt(i - 1)
                            + "</font>");
                    --i;
                    break;
                case INSERT:
                    builder.insert(0, "<span title=\""
                            + second.charAt(j - 1)
                            + "\"><font color=\"blue\">?</font></span>");
                    --j;
                    break;
                case SUBSTITUTE:
                    builder.insert(0, "<span title=\""
                            + second.charAt(j - 1)
                            + "\"><font color=\"green\">"
                            + first.charAt(i - 1)
                            + "</font></span>");
                    --i;
                    --j;
                    break;
            }
        }
        while (i > 0) {
            builder.insert(0, "<font color=\"red\">"
                    + first.charAt(i - 1)
                    + "</font>");
            --i;
        }
        while (j > 0) {
            builder.insert(0, "<span title=\""
                    + second.charAt(j - 1)
                    + "\"><font color=\"blue\">?</font></span>");
            --j;

        }
        builder.insert(0, "<html><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><body>");
        builder.append("</body></html>");

        return builder.toString();

    }
}