public class CPSudokuSolver {
    static final String rows = "ABCDEFGHI";
    static final String cols = "123456789";
    static String[] boxes = new String[81];
    static String[][] unitlist = new String[27][9];
    static String[][] peers = new String[81][];
    static int steps = 0;

    static {
        int index = 0;
        for (char r : rows.toCharArray()) {
            for (char c : cols.toCharArray()) {
                boxes[index++] = "" + r + c;
            }
        }

        index = 0;
        for (int i = 0; i < 9; i++) {
            String[] rowUnit = new String[9];
            String[] colUnit = new String[9];
            for (int j = 0; j < 9; j++) {
                rowUnit[j] = "" + rows.charAt(i) + cols.charAt(j);
                colUnit[j] = "" + rows.charAt(j) + cols.charAt(i);
            }
            unitlist[index++] = rowUnit;
            unitlist[index++] = colUnit;
        }

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                String[] square = new String[9];
                int idx = 0;
                for (int dr = 0; dr < 3; dr++) {
                    for (int dc = 0; dc < 3; dc++) {
                        square[idx++] = "" + rows.charAt(r * 3 + dr) + cols.charAt(c * 3 + dc);
                    }
                }
                unitlist[index++] = square;
            }
        }

        for (int i = 0; i < boxes.length; i++) {
            java.util.HashSet<String> peerSet = new java.util.HashSet<>();
            for (String[] unit : unitlist) {
                for (String s : unit) {
                    if (s.equals(boxes[i])) {
                        for (String b : unit) {
                            if (!b.equals(boxes[i])) {
                                peerSet.add(b);
                            }
                        }
                        break;
                    }
                }
            }
            peers[i] = peerSet.toArray(new String[0]);
        }
    }

    public static void main(String[] args) {
        String board =
                "530070000" +
                "600195000" +
                "098000060" +
                "800060003" +
                "400803001" +
                "700020006" +
                "060000280" +
                "000419005" +
                "000080079";

        long startTime = System.nanoTime();
        String[] values = new String[81];
        for (int i = 0; i < 81; i++) {
            char c = board.charAt(i);
            values[i] = (c >= '1' && c <= '9') ? String.valueOf(c) : "123456789";
        }

        if (search(values)) {
            print(values);
        } else {
            System.out.println("No solution.");
        }

        long endTime = System.nanoTime();
        System.out.println("Steps: " + steps);
        System.out.println("Time: " + (endTime - startTime) / 1_000_000.0 + " ms");
    }

    static boolean search(String[] values) {
        steps++;
        if (!reduce(values)) return false;
        int min = 10, minIndex = -1;
        for (int i = 0; i < 81; i++) {
            if (values[i].length() > 1 && values[i].length() < min) {
                min = values[i].length();
                minIndex = i;
            }
        }
        if (minIndex == -1) return true;

        String original = values[minIndex];
        for (int i = 0; i < original.length(); i++) {
            String[] copied = values.clone();
            copied[minIndex] = String.valueOf(original.charAt(i));
            if (search(copied)) {
                System.arraycopy(copied, 0, values, 0, 81);
                return true;
            }
        }
        return false;
    }

    static boolean reduce(String[] values) {
        boolean stalled;
        do {
            stalled = true;
            int solvedBefore = countSolved(values);
            if (!eliminate(values)) return false;
            if (!onlyChoice(values)) return false;
            if (!nakedTwins(values)) return false;
            if (countSolved(values) != solvedBefore) stalled = false;
        } while (!stalled);

        for (String v : values) {
            if (v.length() == 0) return false;
        }
        return true;
    }

    static int countSolved(String[] values) {
        int count = 0;
        for (String v : values) if (v.length() == 1) count++;
        return count;
    }

    static boolean eliminate(String[] values) {
        for (int i = 0; i < 81; i++) {
            if (values[i].length() == 1) {
                String d = values[i];
                for (String peer : peers[i]) {
                    int pi = indexOf(peer);
                    if (values[pi].contains(d)) {
                        values[pi] = values[pi].replace(d, "");
                        steps++;
                        if (values[pi].length() == 0) return false;
                    }
                }
            }
        }
        return true;
    }

    static boolean onlyChoice(String[] values) {
        for (int u = 0; u < unitlist.length; u++) {
            for (char d = '1'; d <= '9'; d++) {
                String foundBox = null;
                int count = 0;
                for (int i = 0; i < 9; i++) {
                    int bi = indexOf(unitlist[u][i]);
                    if (values[bi].indexOf(d) != -1) {
                        count++;
                        foundBox = unitlist[u][i];
                    }
                }
                if (count == 1 && foundBox != null) {
                    int idx = indexOf(foundBox);
                    if (values[idx].length() > 1) {
                        values[idx] = String.valueOf(d);
                        steps++;
                    }
                }
            }
        }
        return true;
    }

    static boolean nakedTwins(String[] values) {
        for (int u = 0; u < unitlist.length; u++) {
            for (int i = 0; i < 9; i++) {
                int i1 = indexOf(unitlist[u][i]);
                if (values[i1].length() == 2) {
                    for (int j = i + 1; j < 9; j++) {
                        int i2 = indexOf(unitlist[u][j]);
                        if (values[i2].equals(values[i1])) {
                            for (int k = 0; k < 9; k++) {
                                if (k == i || k == j) continue;
                                int ik = indexOf(unitlist[u][k]);
                                for (char c : values[i1].toCharArray()) {
                                    if (values[ik].contains(String.valueOf(c))) {
                                        values[ik] = values[ik].replace(String.valueOf(c), "");
                                        steps++;
                                        if (values[ik].length() == 0) return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    static void print(String[] values) {
        for (int i = 0; i < 81; i++) {
            System.out.print(values[i] + " ");
            if ((i + 1) % 9 == 0) System.out.println();
        }
    }

    static int indexOf(String box) {
        for (int i = 0; i < 81; i++) {
            if (boxes[i].equals(box)) return i;
        }
        return -1;
    }
}