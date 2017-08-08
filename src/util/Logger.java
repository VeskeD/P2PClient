package util;

public class Logger {


    public static class log {
        public static void info(String msg) {
            System.out.println("INFO > " + getTrace() + " :: " + msg);
        }

        public static void error(String msg) {
            System.err.println("ERROR> " + getTrace() + " :: " + msg);
        }

        public static void error(String msg, Exception e) {
            System.err.println("ERROR> " + getTrace() + " :: " + msg + " : " + e.getMessage());
            e.printStackTrace();
        }

        private static String getTrace() {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();

            for (int i = elements.length - 1; i >= 0; i--) {
                if (!elements[i].getClassName().contains("Thread"))
                    return elements[i].toString();
            }

            return "";
        }
    }
}
