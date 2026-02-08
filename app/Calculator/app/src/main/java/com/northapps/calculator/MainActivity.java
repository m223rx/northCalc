package com.northapps.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private TextView textExpression, textResult;
    private final StringBuilder expression = new StringBuilder();
    private int openParentheses = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        textExpression = findViewById(R.id.textExpression);
        textResult = findViewById(R.id.textResult);

        ImageButton btnBackspace = findViewById(R.id.btnBackspace);
        GridLayout keypad = findViewById(R.id.keypad);

        for (int i = 0; i < keypad.getChildCount(); i++) {
            View view = keypad.getChildAt(i);

            if (view instanceof Button) {
                Button button = (Button) view;
                button.setOnClickListener(v ->
                        handleInput(button.getText().toString())
                );
            }
        }

        btnBackspace.setOnClickListener(v -> handleBackspace());
    }

    private void handleInput(String value) {

        switch (value) {

            case "C":
                clearAll();
                break;

            case "=":
                calculate();
                break;

            case "()":
                handleParentheses();
                break;

            default:
                appendValue(value);
                break;
        }

        textExpression.setText(expression.toString());
    }

    private void appendValue(String value) {

        if (isOperator(value)) {
            if (expression.length() == 0) return;

            char last = expression.charAt(expression.length() - 1);
            if (isOperator(String.valueOf(last))) {
                expression.setCharAt(expression.length() - 1, value.charAt(0));
                return;
            }
        }

        expression.append(value);
    }

    private void handleParentheses() {
        if (expression.length() == 0 ||
                isOperator(String.valueOf(expression.charAt(expression.length() - 1))) ||
                expression.charAt(expression.length() - 1) == '(') {

            expression.append("(");
            openParentheses++;
        } else if (openParentheses > 0) {
            expression.append(")");
            openParentheses--;
        }
    }

    private void handleBackspace() {
        if (expression.length() == 0) return;

        char removed = expression.charAt(expression.length() - 1);

        if (removed == '(') openParentheses--;
        if (removed == ')') openParentheses++;

        expression.deleteCharAt(expression.length() - 1);
        textExpression.setText(expression.toString());
    }

    private void clearAll() {
        expression.setLength(0);
        openParentheses = 0;
        textExpression.setText("");
        textResult.setText("0");
    }

    private void calculate() {
        try {
            while (openParentheses > 0) {
                expression.append(")");
                openParentheses--;
            }

            String exp = expression.toString()
                    .replace("×", "*")
                    .replace("÷", "/");

            double result = eval(exp);
            textResult.setText(formatResult(result));

        } catch (Exception e) {
            textResult.setText("Error");
        }
    }

    private String formatResult(double value) {
        DecimalFormat df = new DecimalFormat("0.########");
        return df.format(value);
    }

    private boolean isOperator(String s) {
        return "+-*/".contains(s);
    }

    private double eval(String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                return parseExpression();
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;

                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                }

                return x;
            }
        }.parse();
    }
}
