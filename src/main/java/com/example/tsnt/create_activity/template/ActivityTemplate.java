package com.example.tsnt.create_activity.template;

public class ActivityTemplate {
    public static final String template = "package %1$s;" + "\n" +
            "\n" +
            "import android.support.annotation.Nullable;" + "\n" +
            "import android.support.v7.app.AppCompatActivity;" + "\n" +
            "import android.os.Bundle;" + "\n" +
            "\n" +
            "import %2$s.R;" + "\n" +
            "\n" +
            "public class %3$s extends AppCompatActivity {" + "\n" +
            "\n" +
            "    @Override" + "\n" +
            "    protected void onCreate(@Nullable Bundle savedInstanceState) {" + "\n" +
            "        super.onCreate(savedInstanceState);" + "\n" +
            "        setContentView(R.layout.%4$s);" + "\n" +
            "    }" + "\n" +
            "}";
}
