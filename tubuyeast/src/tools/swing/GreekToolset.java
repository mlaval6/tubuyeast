package tools.swing;

import javax.swing.JTextArea;

public class GreekToolset {

    public enum GreekLetter {
        ALPHA, BETA, GAMMA, DELTA, EPSILON, ZETA, ETA, THETA, IOTA, KAPPA, LAMBDA, MU, NU, XI, OMICRON, PI, RHO, SIGMA1, SIGMA2, TAU, UPSILON, PHI, CHI, PSI, OMEGA;

        /**
         * String representing this Greek letter.
         */
        public final String code;

        GreekLetter() {
            this.code = "" + (char) ('\u03B1' + this.ordinal());
        }
    }

    public static String toString(GreekLetter gl) {
    	return "" + (char) ('\u03B1' + gl.ordinal());
    }
    
    public static String getTestString() {
        String s = "";
        for (GreekLetter l : GreekLetter.values()) s += l.code;
        return s;
    }
}
