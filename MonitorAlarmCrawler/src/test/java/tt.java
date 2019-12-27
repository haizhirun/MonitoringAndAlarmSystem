public class tt {
    public static void main(String[] args) {
        String table = "rules";
        String sql = String.format("SELECT TABLE_NAME,UPDATE_TIME FROM information_schema.TABLES WHERE information_schema.TABLES.TABLE_NAME = '%s';",table);
        System.out.println(sql);
    }
}
