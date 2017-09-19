package com.eastrobot.robotdev.common;

/**
 * MethodDefinition
 *
 * @Author alan.peng
 * @Date 2017-09-11 15:48
 */
public class MethodDefinition {
    private String methodName;
    private String[] argClassNames;

    public MethodDefinition(String methodName, String[] argClassNames) {
        this.methodName = methodName;
        this.argClassNames = argClassNames;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getArgClassNames() {
        return this.argClassNames;
    }

    public void setArgClassNames(String[] argClassNames) {
        this.argClassNames = argClassNames;
    }

    public static String d() {
        String s = String.valueOf(3.141592653589793D).substring(2);
        StringBuilder buf = new StringBuilder();

        for(int i = s.length() - 1; i >= 0; --i) {
            buf.append(s.charAt(i));
        }

        return s + String.valueOf(3.141592653589793D).substring(2, 9) + buf.toString();
    }

}
