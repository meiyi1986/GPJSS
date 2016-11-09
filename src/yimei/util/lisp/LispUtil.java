package yimei.util.lisp;

import java.util.ArrayList;
import java.util.List;

/**
 * The utility functions to process LISP expressions.
 *
 * Created by YiMei on 1/10/16.
 */
public class LispUtil {

    public static List<String> splitArguments(String argsString) {
        List<String> args = new ArrayList<>();
        int head = 0;

        while (head < argsString.length()) {
            if (argsString.charAt(head) == '(') {
                int unbalance = 1;
                for (int i = head + 1; i < argsString.length(); i++) {
                    if (argsString.charAt(i) == '(') {
                        unbalance ++;
                    }

                    if (argsString.charAt(i) == ')') {
                        unbalance --;

                        if (unbalance == 0) {
                            args.add(argsString.substring(head, i + 1));
                            head = i + 2;
                        }
                    }
                }
            }
            else {
                int tail = argsString.indexOf(' ', head);
                if (tail == -1)
                    tail = argsString.length();
                args.add(argsString.substring(head, tail));
                head = tail + 1;
            }
        }

        return args;
    }
}
