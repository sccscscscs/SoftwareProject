package com.myapp;

public class Main {
    public static void main(String[] args) {
        CodeStatsService svc = new CodeStatsService();

        CodeStatsService.AnalyzeRequest req = new CodeStatsService.AnalyzeRequest();
        req.language = CodeStatsCore.Language.PYTHON; // 或 Language.JAVA

        // 内存方式
        CodeStatsService.InMemoryFile f = new CodeStatsService.InMemoryFile();
        f.path = "demo.py";
        f.code = """
                def f(x):
                    return x + 1

                class A:
                    def m(self):
                        def inner():
                            pass
                        return 42
                """;
        req.files = java.util.List.of(f);

        CodeStatsCore.AnalyzeResult res = svc.analyze(req);
        System.out.println("统计结果：");
        System.out.println("函数数量 = " + res.summary.count);
        System.out.println("平均长度 = " + res.summary.mean);
        System.out.println("最大长度 = " + res.summary.max);
        System.out.println("最小长度 = " + res.summary.min);
        System.out.println("中位数   = " + res.summary.median);
    }
}
