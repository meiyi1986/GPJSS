package yimei.util.lisp;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yimei on 29/09/16.
 */
public class Tokenizer {

    public String[] parse(String text) {
        ArrayList<String> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile("[\\s ,]*(~@|[\\[\\]{}()'`~@]|\"(?:[\\\\].|[^\\\\\"])*\"|;.*|[^\\s \\[\\]{}()'\"`~@,;]*)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String token = matcher.group(1);
            if (token != null &&
                    !token.equals("") &&
                    !(token.charAt(0) == ';')) {
                tokens.add(token);
            }
        }
        return tokens.toArray(new String[tokens.size()]);
    }

}
